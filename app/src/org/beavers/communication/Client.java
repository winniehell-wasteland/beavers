package org.beavers.communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.anddev.andengine.util.StreamUtils;
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

	@Override
	public IBinder onBind(final Intent pIntent) {
		Log.d(TAG, "onBind()");
		return implementation;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();

		try {
			implementation.loadGameList();
		} catch (final ClientRemoteException e) {
			e.log();
		}

		implementation.addDummyGames();

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

		try {
			implementation.saveGameList();
		} catch (final ClientRemoteException e) {
			e.log();
		}

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
					try {
						implementation.handleData(fileName);
					} catch (final ClientRemoteException e) {
						Log.e(TAG, getString(R.string.error_dtn_receiving), e);
					}

					(new File(fileName)).delete();
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

	public class ClientRemoteException extends RemoteException {

		/** @see {@link Serializable} */
		private static final long serialVersionUID = 5092163419629856671L;

		public ClientRemoteException(final int pResId,
		                             final Object... pFormatArgs) {
			this(pResId, null, pFormatArgs);
		}


		public ClientRemoteException(final int pResId,
		                             final Exception pInnerException,
		                             final Object... pFormatArgs) {
			if(pFormatArgs.length > 0) {
				message = getString(pResId, pFormatArgs);
			} else {
				message = getString(pResId);
			}

			innerException = pInnerException;
		}

		public Exception getInnerException() {
			return innerException;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public StackTraceElement[] getStackTrace() {
			final ArrayList<StackTraceElement> stack =
				new ArrayList<StackTraceElement>();

			stack.addAll(Arrays.asList(super.getStackTrace()));
			stack.addAll(Arrays.asList(innerException.getStackTrace()));

			return stack.toArray(new StackTraceElement[0]);
		}

		public void log() {
			if(innerException == null) {
				Log.e(TAG, message);
			}
			else {
				Log.e(TAG, message, innerException);
			}
		}

		@Override
		public void printStackTrace() {
			super.printStackTrace();
			innerException.printStackTrace();
		}

		private final Exception innerException;
		private final String message;
	}

	/** communication service connection */
	private DTNService.Connection dtn;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Implementation implementation = new Implementation();

	private class Implementation extends IClient.Stub {

		public Implementation() {
			Log.d(TAG, "Creating game lists...");

			announcedGames = Collections.synchronizedSet(new HashSet<Game>());
			runningGames = Collections.synchronizedSet(new HashSet<Game>());
		}

		/** just for debugging */
		@Override
		public void addDummyGames() {
			final Game game = new Game(new Player(UUID.randomUUID(), "playa"), UUID.randomUUID(), "foooooooo");

			game.getDirectory(Client.this).mkdirs();

			try {
				final GameInfo info = new GameInfo("test", 0);
				info.saveToFile(Client.this, game);
			} catch (final IOException e) {
				Log.e(TAG, "saving game info failed!", e);
			}

			announcedGames.add(game);
		}

		@Override
		public void abortGame(final Game pGame) throws ClientRemoteException {
			if (!runningGames.contains(pGame)) {
				throw new ClientRemoteException(
					R.string.error_game_not_running, pGame
				);
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
				throw new ClientRemoteException(R.string.error_dtn_sending, e);
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
		 * @throws ClientRemoteException
		 */
		public boolean handleData(final String pFileName)
		               throws ClientRemoteException {
			final File input = new File(pFileName);

			Log.i(TAG, "Processing "+input.length()+" bytes...");

			final Gson gson = CustomGSON.getInstance();
			JsonObject json = new JsonObject();

			try {
				final JsonParser parser = new JsonParser();
				final FileReader reader = new FileReader(input);

				json = (JsonObject) parser.parse(reader);
			} catch (final FileNotFoundException e) {
				throw new ClientRemoteException(
					R.string.error_dtn_receiving, e
				);
			}

			if(!json.has(Game.JSON_TAG))
			{
				throw new ClientRemoteException(
					R.string.error_json_missing_element, Game.JSON_TAG
				);
			}

			final Game game =
				gson.fromJson(json.get(Game.JSON_TAG), Game.class);
			Log.e(TAG, "game: "+game);

			if(!json.has(GameState.JSON_TAG))
			{
				throw new ClientRemoteException(
					R.string.error_json_missing_element, GameState.JSON_TAG
				);
			}

			final GameState state =
				gson.fromJson(json.get(GameState.JSON_TAG), GameState.class);

			switch (state) {
			case ANNOUNCED:
			{
				if(announcedGames.contains(game)) {
					throw new ClientRemoteException(
						R.string.error_already_announced, game
					);
				}
				else {
					if(!json.has(GameInfo.JSON_TAG_MAP)) {
						throw new ClientRemoteException(
							R.string.error_json_missing_element,
							GameInfo.JSON_TAG_MAP
						);
					}

					final String map =
						json.get(GameInfo.JSON_TAG_MAP).getAsString();

					onReceiveAnnouncedGame(game, map);
				}

				return true;
			}
			case EXECUTION_PHASE:
			{
				if(runningGames.contains(game))
				{
					if(!json.has(OutcomeContainer.JSON_TAG)) {
						throw new ClientRemoteException(
							R.string.error_json_missing_element,
							OutcomeContainer.JSON_TAG
						);
					}

					final OutcomeContainer outcome =
						gson.fromJson(json.get(OutcomeContainer.JSON_TAG),
						              OutcomeContainer.class);
					onReceiveOutcome(game, outcome);
				}

				return true;
			}
			case PLANNING_PHASE:
			{

				if(runningGames.contains(game) || announcedGames.contains(game))
				{
					if(!json.has(Player.JSON_TAG_COLLECTION)) {
						throw new ClientRemoteException(
							R.string.error_json_missing_element,
							OutcomeContainer.JSON_TAG
						);
					}

					final PlayerSet players =
						gson.fromJson(json.get(Player.JSON_TAG_COLLECTION),
						PlayerSet.class
					);

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
		public void joinGame(final Game pGame) throws ClientRemoteException {
			if (!announcedGames.contains(pGame)) {
				throw new ClientRemoteException(
					R.string.error_game_not_announced, pGame
				);
			}

			if (!pGame.isInState(Client.this, GameState.ANNOUNCED)) {
				throw new ClientRemoteException(
					R.string.error_game_wrong_state,
					pGame, pGame.getState(Client.this)
				);
			}

			final Message message =
				new ClientMessage(pGame, getSettings().getPlayer(),
				                  GameState.JOINED);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.saveToFile(Client.this));
			} catch (final RemoteException e) {
				throw new ClientRemoteException(R.string.error_dtn_sending, e);
			}

			try {
				pGame.setState(Client.this, GameState.JOINED);
			} catch (final IOException e) {
				throw new ClientRemoteException(
					R.string.error_game_save_state, e, pGame
				);
			}

			broadcastGameInfo(pGame);
		}

		@Override
		public void loadGameList() throws ClientRemoteException {
			try {
				Log.d(TAG, "loading: "+StreamUtils.readFully(new FileInputStream(getListFileName())));
			} catch (final Exception e) {
				Log.e(TAG, e.getMessage());
				return;
			}

			JsonReader reader;
			try {
				reader = CustomGSON.getReader(Client.this, getListFileName());
			} catch (final FileNotFoundException e1) {
				// file does not exist
				return;
			}

			synchronized (runningGames) {
				final Gson gson = CustomGSON.getInstance();
				runningGames.clear();

				try {
					reader.beginArray();
					while(reader.hasNext()) {
						final Game game = gson.fromJson(reader, Game.class);
						runningGames.add(game);
					}
					reader.endArray();
				} catch (final Exception e) {
					throw new ClientRemoteException(
						R.string.error_json_reading, e
					);
				}
			}
		}

		@Override
		public void saveGameList() throws ClientRemoteException {
			JsonWriter writer;

			try {
				writer = CustomGSON.getWriter(Client.this, getListFileName());
			} catch (final FileNotFoundException e) {
				throw new ClientRemoteException(R.string.error_json_writing, e);
			}

			synchronized (runningGames) {
				final Gson gson = CustomGSON.getInstance();

				try {
					gson.toJson(runningGames, runningGames.getClass(), writer);
				} catch (final Exception e) {
					throw new ClientRemoteException(
						R.string.error_json_writing, e
					);
				} finally {
					try {
						writer.close();
					} catch (final IOException e) {
						throw new ClientRemoteException(
							R.string.error_json_writing, e
						);
					}
				}
			}
		}

		@Override
		public void sendDecisions(final Game pGame, final String pSoldiers)
		            throws ClientRemoteException {
			if (!runningGames.contains(pGame)) {
				throw new ClientRemoteException(
					R.string.error_game_not_running, pGame
				);
			}

			if (!pGame.isInState(Client.this, GameState.PLANNING_PHASE)) {
				throw new ClientRemoteException(
					R.string.error_game_wrong_state,
					pGame, pGame.getState(Client.this)
				);
			}

			final Message message =
				new DecissionMessage(getSettings().getPlayer(),
				                     pGame, pSoldiers);

			try {
				dtn.getService().sendToServer(pGame.getServer(),
				                              message.saveToFile(Client.this));
			} catch (final RemoteException e) {
				throw new ClientRemoteException(R.string.error_dtn_sending, e);
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
		private final Set<Game> announcedGames;
		private final Set<Game> runningGames;
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

			final Intent update_intent = new Intent(Game.STATE_CHANGED_INTENT);

			update_intent.putExtra(Game.PARCEL_NAME, pGame);

			sendBroadcast(update_intent);
		}

		private String getListFileName() {
			return getFilesDir() + "/running_games.json";
		}

		/**
		 * server has announced a new game
		 *
		 * @param pGame new game
		 * @param pMapName name of the game's map
		 * @throws ClientRemoteException
		 */
		private void onReceiveAnnouncedGame(final Game pGame,
		                                    final String pMapName)
		             throws ClientRemoteException {
			if(!pGame.isServer(getSettings().getPlayer()))
			{
				final File dir = pGame.getDirectory(Client.this);

				if(dir.exists()) {
					throw new ClientRemoteException(
						R.string.error_game_dir_exists, pGame
					);
				}

				dir.mkdirs();

				try {
					final GameInfo info = new GameInfo(pMapName, 1);
					info.setState(GameState.ANNOUNCED);
					info.saveToFile(Client.this, pGame);
				} catch (final IOException e) {
					throw new ClientRemoteException(
						R.string.error_game_save_state, e, pGame
					);
				}
			}

			announcedGames.add(pGame);
			broadcastGameInfo(pGame);

			// auto join own game
			if(pGame.isServer(getSettings().getPlayer()))
			{
				joinGame(pGame);
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
		 * @throws ClientRemoteException
		 */
		private void onReceiveStartPlanningPhase(final Game pGame,
		                                         final PlayerSet pPlayers)
		             throws ClientRemoteException {

			if(!pGame.isInState(Client.this, GameState.JOINED,
			                                 GameState.EXECUTION_PHASE)) {

				throw new ClientRemoteException(
					R.string.error_game_wrong_state,
					pGame, pGame.getState(Client.this)
				);
			}

			if(pGame.isInState(Client.this, GameState.JOINED))
			{
				announcedGames.remove(pGame);

				if(pPlayers.contains(getSettings().getPlayer()))
				{
					runningGames.add(pGame);
				}
			}
			else if(!pPlayers.contains(getSettings().getPlayer()))
			{
				// TODO do something, we got kicked out!
			}

			// start planning phase
			try {
				pGame.setState(Client.this, GameState.PLANNING_PHASE);
			} catch (final IOException e) {
				throw new ClientRemoteException(
					R.string.error_game_save_state, pGame, e
				);
			}

			broadcastGameInfo(pGame);
		}
	};

	private Settings getSettings()
	{
		return ((App) getApplication()).getSettings();
	}
}
