package org.beavers.communication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.communication.DTNService.Message;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameState;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.Player;
import org.beavers.ingame.Soldier;
import org.beavers.storage.CustomGSON;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * client service for game communication
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class Client extends Service {

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = Client.class.getSimpleName();
	/**
	 * @}
	 */

	/**
	 * @name intents
	 * @{
	 */
	public static final String GAME_STATE_CHANGED_INTENT =
		Client.class.getName() + ".GAME_STATE_CHANGED";
	/**
	 * @}
	 */

	@Override
	public IBinder onBind(final Intent pIntent) {
		Log.d(TAG, "onBind()");
		return implementation;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();

		implementation.loadGameList();

		dtn = new DTNService.Connection();
		final Intent intent = new Intent(Client.this, DTNService.class);
		bindService(intent, dtn, Service.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");

		unbindService(dtn);

		try {
			// stop executor
			executor.shutdown();

			// ... and wait until all jobs are done
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (final InterruptedException e) {
			Log.e(TAG, "Interrupted while processing executor queue!", e);
		}

		implementation.saveGameList();

		super.onDestroy();
	}

	@Override
	public int onStartCommand(final Intent pIntent, final int pFlags, final int pStartId) {

		Log.d(TAG, "onStartCommand()");

		if(pIntent.getAction().equals(de.tubs.ibr.dtn.Intent.RECEIVE))
		{
        	final int stopId = pStartId;
        	final String fileName = pIntent.getStringExtra("file");

			executor.execute(new Runnable() {

				@Override
				public void run() {
					implementation.handleData(fileName);
				}
			});

			stopSelfResult(stopId);

        	return START_STICKY;
		}

        return START_NOT_STICKY;
	}

	/**
	 * service connection
	 */
	public static class Connection implements ServiceConnection
	{
		public IClient getService() {
			return service;
		}

		@Override
		public void onServiceConnected(final ComponentName pName,
		                               final IBinder pService) {
			service = IClient.Stub.asInterface(pService);
		}

		@Override
		public void onServiceDisconnected(final ComponentName pName) {
			service = null;
		}

		private IClient service;
	}

	/** communication service connection */
	private DTNService.Connection dtn;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Implementation implementation = new Implementation();

	private class Implementation extends IClient.Stub {

		@Override
		public void abortGame(final Game pGame) {
			if (!runningGames.contains(pGame)) {
				Log.e(TAG, getString(R.string.error_not_running, pGame));
				return;
			}

			runningGames.remove(pGame);
			broadcastGameInfo(pGame);

			final Message message =
				new ClientMessage(pGame, getSettings().getPlayer(),
				                  GameState.ABORTED);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.saveToFile(Client.this));
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public Game[] getAnnouncedGames() {
			return announcedGames.toArray(new Game[0]);
		};

		@Override
		public Game[] getRunningGames() {
			return runningGames.toArray(new Game[0]);
		};

		/**
		 * receive a DTN message
		 *
		 * @param fileName file descriptor of payload file
		 * @return true if handled
		 */
		public boolean handleData(final String pFileName) {
			final File input = new File(pFileName);

			Log.i(TAG, "Processing "+input.length()+" bytes...");

			JsonObject json = new JsonObject();

			try {
				final JsonParser parser = new JsonParser();
				final FileReader reader = new FileReader(input);

				json = (JsonObject) parser.parse(reader);
			} catch (final FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(!json.has(GameInfo.JSON_TAG))
			{
				Log.e(TAG, "JSON object does not contain game info!");
				return false;
			}

			final Gson gson = CustomGSON.getInstance();

			final Game game =
				gson.fromJson(json.get(Game.JSON_TAG), Game.class);
			Log.e(TAG, "game: "+game);

			final GameInfo info = GameInfo.fromFile(Client.this, game);

			switch (info.getState()) {
			case ANNOUNCED:
			{
				if(announcedGames.contains(game)) {
					Log.e(TAG, getString(R.string.error_already_announced,
						                 game));
				}
				else {
					if(!json.has("map")) {
						Log.e(TAG, "Map name is missing!");
					}
					else {
						onReceiveAnnouncedGame(game,
						                       json.get("map").getAsString());
					}
				}

				return true;
			}
			case EXECUTION_PHASE:
			{
				if(runningGames.contains(game))
				{
					onReceiveOutcome(game,
					                 gson.fromJson(json.get("outcome"),
					                               OutcomeContainer.class));
				}

				return true;
			}
			case PLANNING_PHASE:
			{

				if(runningGames.contains(game) || announcedGames.contains(game))
				{
					final Type type =
						new TypeToken<HashSet<Player>>() {}.getType();

					final HashSet<Player> players =
						gson.fromJson(json.get("players"), type);

					onReceiveStartPlanningPhase(game, players);
				}

				return true;
			}
			default:
				break;
			}

			return false;
		}

		@Override
		public void joinGame(final Game pGame) {
			if (!announcedGames.contains(pGame)) {
				Log.e(TAG, getString(R.string.error_not_announced, pGame));
				return;
			}

			final GameInfo info = GameInfo.fromFile(Client.this, pGame);

			if (!info.isInState(GameState.ANNOUNCED)) {
				Log.e(TAG, getString(R.string.error_wrong_state, pGame,
				                     info.getState()));
				return;
			}

			info.setState(GameState.JOINED);
			info.saveToFile(Client.this, pGame);

			broadcastGameInfo(pGame);

			final Message message =
				new ClientMessage(pGame, getSettings().getPlayer(),
				                  GameState.JOINED);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.saveToFile(Client.this));
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void loadGameList() {

			final Gson gson = CustomGSON.getInstance();
			final JsonReader reader = CustomGSON.getReader(Client.this,
			                                               getListFileName());

			// file does not exist
			if(reader == null) {
				return;
			}

			try {
				reader.beginObject();

				synchronized (runningGames) {
					runningGames.clear();

					CustomGSON.assertElement(reader, "games");
					reader.beginArray();
					while (reader.hasNext()) {
						final Game game =
							(Game) gson.fromJson(reader, Game.class);
						runningGames.add(game);
					}
					reader.endArray();
				}

				reader.endObject();

				reader.close();
			} catch (final Exception e) {
				Log.e(TAG, "Could not read JSON file!", e);
				return;
			}
		}

		@Override
		public void saveGameList() {

			final Gson gson = CustomGSON.getInstance();
			final JsonWriter writer = CustomGSON.getWriter(Client.this,
			                                               getListFileName());

			if(writer == null) {
				return;
			}

			try {
				writer.beginObject();

				synchronized (runningGames) {

					writer.name("games");
					writer.beginArray();
					for(final Game game : runningGames) {
						gson.toJson(game, GameInfo.class, writer);
					}
					writer.endArray();
				}

				writer.endObject();

				writer.close();
			} catch (final Exception e) {
				Log.e(TAG, "Could not write JSON file!", e);
				return;
			}
		}

		@Override
		public void sendDecisions(final Game pGame, final String pSoldiers) {
			if (!runningGames.contains(pGame)) {
				Log.e(TAG, getString(R.string.error_not_running, pGame));
				return;
			}

			final GameInfo info = GameInfo.fromFile(Client.this, pGame);

			if (!info.isInState(GameState.PLANNING_PHASE)) {
				Log.e(TAG, getString(R.string.error_wrong_state, pGame,
				                     info.getState()));
				return;
			}

			final Message message =
				new DecissionMessage(getSettings().getPlayer(),
				                     pGame, pSoldiers);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.saveToFile(Client.this));
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * @name messages
		 * @{
		 */

		class ClientMessage extends Message
		{
			public ClientMessage(final Game pGame, final Player pPlayer,
			                     final GameState pState) {
				super(pGame, pState);

				player = pPlayer;
			}

			@SerializedName(Player.JSON_TAG)
			private final Player player;
		}

		class DecissionMessage extends ClientMessage
		{
			public DecissionMessage(final Player pPlayer, final Game pGame,
			                        final String pSoldiers) {
				super(pGame, pPlayer, GameState.PLANNING_PHASE);

				soldiers = pSoldiers;
			}

			@SerializedName(Soldier.JSON_TAG_COLLECTION)
			private final String soldiers;
		}
		/**
		 * @}
		 */

		/**
		 * @name game containers
		 * @{
		 */
		private final Set<Game> announcedGames =
			Collections.synchronizedSet(new HashSet<Game>());
		private final Set<Game> runningGames =
			Collections.synchronizedSet(new HashSet<Game>());
		/**
		 * @}
		 */

		/**
		 * inform activities about a changed game
		 *
		 * @param pGame changed game
		 */
		private void broadcastGameInfo(final Game pGame) {
			Log.d(TAG, "Broadcasting new game info...");

			final Intent update_intent = new Intent(GAME_STATE_CHANGED_INTENT);

			update_intent.putExtra(Game.PARCEL_NAME, pGame);

			sendBroadcast(update_intent);
		}

		private String getListFileName() {
			return "running_games.json";
		}

		/**
		 * server has announced a new game
		 *
		 * @param pGame new game
		 * @param pMapName name of the game's map
		 */
		private void onReceiveAnnouncedGame(final Game pGame,
		                                    final String pMapName) {
			try {
				final File dir = pGame.getDirectory(Client.this);

				if(dir.exists()) {
					Log.e(TAG, "Game directory already exists!");
					return;
				}

				dir.mkdirs();

				final GameInfo info = new GameInfo(pMapName, 1);
				info.setState(GameState.ANNOUNCED);
				info.saveToFile(Client.this, pGame);

				announcedGames.add(pGame);
				broadcastGameInfo(pGame);

				if(pGame.isServer(getSettings().getPlayer()))
				{
					// auto join own game
					joinGame(pGame);
				}
			} catch (final Exception e) {
				Log.e(TAG, "Could not create storage!");
			}
		}

		/**
		 * receive outcome from server
		 *
		 * @param pGame game
		 * @param pOutcome
		 */
		private void onReceiveOutcome(final Game pGame,
		                              final OutcomeContainer pOutcome) {
			// TODO handle outcome
		}

		/**
		 * start planning phase
		 *
		 * @param pGame running game
		 */
		private void onReceiveStartPlanningPhase(
			final Game pGame, final HashSet<Player> players) {

			final GameInfo info = GameInfo.fromFile(Client.this, pGame);

			if(!info.isInState(GameState.JOINED)
			   && !info.isInState(GameState.EXECUTION_PHASE)) {

				Log.e(TAG, getString(R.string.error_wrong_state, pGame,
				                     info.getState()));

				return;
			}

			if(info.isInState(GameState.JOINED))
			{
				announcedGames.remove(pGame);

				if(players.contains(getSettings().getPlayer()))
				{
					runningGames.add(pGame);
				}
			}
			else if(!players.contains(getSettings().getPlayer()))
			{
				// TODO do something, we got kicked out!
			}

			// start planning phase
			info.setState(GameState.PLANNING_PHASE);
			info.saveToFile(Client.this, pGame);

			broadcastGameInfo(pGame);
		}
	};

	private Settings getSettings()
	{
		return ((App) getApplication()).getSettings();
	}
}
