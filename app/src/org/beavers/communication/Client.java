package org.beavers.communication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.communication.DTNService.Message;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;
import org.beavers.gameplay.GameState;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.Player;
import org.beavers.ingame.Soldier;
import org.beavers.storage.CustomGSON;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
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
		public void abortGame(GameInfo pGame) {
			if (!runningGames.contains(pGame)) {
				Log.e(TAG, getString(R.string.error_not_running, pGame));
				return;
			}

			pGame = runningGames.find(pGame);

			runningGames.remove(pGame);
			broadcastGameInfo(pGame);

			final Message message =
				new ClientMessage(Client.this, getSettings().getPlayer(), pGame);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.getFile());
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public GameInfo getAnnouncedGame(final String pKey) {
			return announcedGames.get(pKey);
		};

		@Override
		public String[] getAnnouncedGames() {
			return announcedGames.getKeys();
		};

		@Override
		public int getAnnouncedGamesCount() {
			return announcedGames.size();
		};

		@Override
		public GameInfo getRunningGame(final String pKey) {
			return runningGames.get(pKey);
		};

		@Override
		public String[] getRunningGames() {
			return runningGames.getKeys();
		};

		@Override
		public int getRunningGamesCount() {
			return runningGames.size();
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

			final GameInfo game =
				gson.fromJson(json.get(GameInfo.JSON_TAG), GameInfo.class);
			Log.e(TAG, "game: "+game);

			switch (game.getState()) {
			case ANNOUNCED:
			{
				if(announcedGames.contains(game)) {
					Log.e(TAG, getString(R.string.error_already_announced,
						                 game));
				}
				else
				{
					onReceiveGameInfo(game);
				}

				return true;
			}
			case EXECUTION_PHASE:
			{
				if(runningGames.contains(game))
				{
					onReceiveOutcome(runningGames.find(game),
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

					onReceiveStartPlanningPhase(runningGames.find(game),
					                            players);
				}

				return true;
			}
			default:
				break;
			}

			return false;
		}

		@Override
		public void joinGame(GameInfo pGame) {
			if (!announcedGames.contains(pGame)) {
				Log.e(TAG, getString(R.string.error_not_announced, pGame));
				return;
			}

			pGame = announcedGames.find(pGame);

			if (!pGame.isInState(GameState.ANNOUNCED)) {
				Log.e(TAG, getString(R.string.error_wrong_state, pGame,
				                     pGame.getState()));
				return;
			}

			pGame.setState(GameState.JOINED);
			broadcastGameInfo(pGame);

			final Message message =
				new ClientMessage(Client.this, getSettings().getPlayer(), pGame);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.getFile());
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
						final GameInfo game =
							(GameInfo) gson.fromJson(reader, GameInfo.class);
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
					for(final GameInfo game : runningGames) {
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
		public void sendDecisions(GameInfo pGame, final String pSoldiers) {
			if (!runningGames.contains(pGame)) {
				Log.e(TAG, getString(R.string.error_not_running, pGame));
				return;
			}

			pGame = runningGames.find(pGame);

			if (!pGame.isInState(GameState.PLANNING_PHASE)) {
				Log.e(TAG, getString(R.string.error_wrong_state, pGame,
				                     pGame.getState()));
				return;
			}

			final Message message =
				new DecissionMessage(Client.this, getSettings().getPlayer(),
				                     pGame, pSoldiers);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.getFile());
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
			public ClientMessage(final Context pContext, final Player pPlayer,
			                     final GameInfo pGame) {
				super(pContext, pGame);

				player = pPlayer;
			}

			@SerializedName(Player.JSON_TAG)
			private final Player player;
		}

		class DecissionMessage extends ClientMessage
		{
			public DecissionMessage(final Context pContext,
									final Player pPlayer, final GameInfo pGame,
			                        final String pSoldiers) {
				super(pContext, pPlayer, pGame);

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
		private final GameList announcedGames = new GameList();
		private final GameList runningGames = new GameList();
		/**
		 * @}
		 */

		/**
		 * inform activities about a changed game
		 *
		 * @param pGame changed game
		 */
		private void broadcastGameInfo(final GameInfo pGame) {
			Log.d(TAG, "Broadcasting new game info...");

			final Intent update_intent = new Intent(GAME_STATE_CHANGED_INTENT);

			update_intent.putExtra(GameInfo.PARCEL_NAME, pGame);

			sendBroadcast(update_intent);
		}

		private String getListFileName() {
			return "running_games.json";
		}

		/**
		 * server has announced new game
		 *
		 * @param pGame new game
		 */
		private void onReceiveGameInfo(final GameInfo pGame) {
			announcedGames.add(pGame);
			broadcastGameInfo(pGame);

			if(pGame.isServer(getSettings().getPlayer()))
			{
				// auto join own game
				joinGame(pGame);
			}
		}

		/**
		 * receive outcome from server
		 *
		 * @param pGame game
		 * @param pOutcome
		 */
		private void onReceiveOutcome(final GameInfo pGame,
		                              final OutcomeContainer pOutcome) {
			// TODO handle outcome
		}

		/**
		 * start planning phase
		 *
		 * @param pGame running game
		 */
		private void onReceiveStartPlanningPhase(
			final GameInfo pGame, final HashSet<Player> players) {

			if(!pGame.isInState(GameState.JOINED)
			   && !pGame.isInState(GameState.EXECUTION_PHASE)) {

				Log.e(TAG, getString(R.string.error_wrong_state, pGame,
				                     pGame.getState()));

				return;
			}

			if(pGame.isInState(GameState.JOINED))
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
			pGame.setState(GameState.PLANNING_PHASE);
			broadcastGameInfo(pGame);
		}
	};

	private Settings getSettings()
	{
		return ((App) getApplication()).getSettings();
	}
}
