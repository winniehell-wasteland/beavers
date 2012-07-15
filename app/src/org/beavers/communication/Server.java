package org.beavers.communication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * server for game communication
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class Server extends Service {

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = Server.class.getSimpleName();
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

		implementation.loadPlayerMap();

		dtn = new DTNService.Connection();
		final Intent intent = new Intent(Server.this, DTNService.class);
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

		implementation.savePlayerMap();

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

	public static class Connection implements ServiceConnection
	{
		public IServer getService() {
			return service;
		}

		@Override
		public void onServiceConnected(final ComponentName pName,
		                               final IBinder pService) {
			service = IServer.Stub.asInterface(pService);
		}

		@Override
		public void onServiceDisconnected(final ComponentName pName) {
			service = null;
		}

		private IServer service;
	}

	/** communication service connection */
	private DTNService.Connection dtn;

	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Implementation implementation = new Implementation();

	/** implementation of the {@link IServer} interface */
	private class Implementation extends IServer.Stub {

		@Override
		public synchronized void addPlayer(final Game pGame, final Player pPlayer)
		{
			Log.d(TAG, pPlayer.getName() + " joins "+pGame.getName());

			// we already joined this game
			if(!pGame.isInState(Server.this, GameState.JOINED)) {
				Log.e(TAG, getString(R.string.error_wrong_state,
				                     pGame, pGame.getState(Server.this)));
				return;
			}

			final HashSet<Player> gamePlayers = playerMap.get(pGame);

			if(gamePlayers.size() < getSettings().getMaxPlayers())
			{
				gamePlayers.add(pPlayer);

				if(gamePlayers.size() == getSettings().getMaxPlayers())
				{
					startPlanningPhase(pGame);
				}
			}
		}

		@Override
		public void distributeOutcome(final Game pGame,
		                              final OutcomeContainer pOutcome)
		{
			final Message message = new OutcomeMessage(pGame, pOutcome);

			try {
				dtn.getService().sendToClients(message.saveToFile(Server.this));
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * receive a DTN message
		 *
		 * @param pData file descriptor of payload file
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

			if(json.has(Game.JSON_TAG) && json.has(Player.JSON_TAG))
			{
				final Gson gson = CustomGSON.getInstance();

				final Game game =
					gson.fromJson(json.get(Game.JSON_TAG), Game.class);

				// are we the server?
				if(!game.isServer(getSettings().getPlayer()))
				{
					return false;
				}

				if(game.isInState(Server.this, GameState.UNKNOWN))
				{
					Log.e(TAG, getString(R.string.error_not_hosted, game));
					return false;
				}

				final Player player =
					gson.fromJson(json.get(Player.JSON_TAG), Player.class);

				if(!json.has(GameState.JSON_TAG))
				{
					Log.e(TAG, "JSON object does not contain game state!");
					return false;
				}

				final GameState state =
					gson.fromJson(json.get(GameState.JSON_TAG), GameState.class);


				switch (state) {
				case JOINED:
				{
					addPlayer(game, player);

					return true;
				}
				case PLANNING_PHASE:
				{
					if(!json.has(Soldier.JSON_TAG_COLLECTION))
					{
						Log.e(TAG,
						      getString(R.string.error_incomplete_decisions,
						                game, player));
						return true;
					}

					final JsonElement soldiers =
						json.get(Soldier.JSON_TAG_COLLECTION);

					onReceiveDecisions(game, player, soldiers.getAsString());

					return true;
				}
				case ABORTED:
				{
					onPlayerAborts(game, player);

					return true;
				}
				default:
					return false;
				}
			}

			return false;
		}

		@Override
		public void initiateGame(final Game pGame)
		{
			if(!pGame.isInState(Server.this, GameState.UNKNOWN))
			{
				Log.e(TAG, getString(R.string.error_game_exists, pGame));
				return;
			}

			try {
				final File dir = pGame.getDirectory(Server.this);

				if(dir.exists()) {
					Log.e(TAG, "Game directory already exists!");
					return;
				}

				dir.mkdirs();

				final String map = getSettings().getDefaultMapName();

				final GameInfo info = new GameInfo(map, 0);
				info.setState(GameState.ANNOUNCED);
				info.saveToFile(Server.this, pGame);

				final Message message = new AnnouncementMessage(pGame, map);

				dtn.getService().sendToClients(message.saveToFile(Server.this));
			} catch (final Exception e) {
				Log.e(TAG, "Could not announce!", e);
			}
		}

		@Override
		public void loadPlayerMap() {
			JsonReader reader;
			try {
				reader = CustomGSON.getReader(Server.this, getListFileName());
			} catch (final FileNotFoundException e) {
				// file does not exist
				return;
			}

			synchronized (playerMap) {
				final Gson gson = CustomGSON.getInstance();
				playerMap = gson.fromJson(reader, playerMap.getClass());
			}
		}

		@Override
		public void savePlayerMap() {
			final JsonWriter writer =
				CustomGSON.getWriter(Server.this, getListFileName());

			if(writer == null) {
				return;
			}

			synchronized(playerMap) {
				final Gson gson = CustomGSON.getInstance();
				gson.toJson(playerMap, playerMap.getClass(), writer);
			}
		}

		/**
		 * player quits game
		 *
		 * @param pGame game
		 * @param pPlayer player
		 */
		public void onPlayerAborts(final Game pGame, final Player pPlayer)
		{
			if(pGame.getServer().equals(pPlayer))
			{
				// TODO remove player from map and stop game
			}
		}

		/**
		 * @name messages
		 * @{
		 */
		class AnnouncementMessage extends Message
		{
			public AnnouncementMessage(final Game pGame,
			                           final String pMapName) {
				super(pGame, GameState.ANNOUNCED);
				map = pMapName;
			}

			@SerializedName(GameInfo.JSON_TAG_MAP)
			private final String map;
		}

		class OutcomeMessage extends Message
		{
			public OutcomeMessage(final Game pGame,
			                      final OutcomeContainer pOutcome) {
				super(pGame, GameState.EXECUTION_PHASE);
				outcome = pOutcome;
			}

			@SerializedName(OutcomeContainer.JSON_TAG)
			private final OutcomeContainer outcome;
		}

		class PlanningPhaseMessage extends Message
		{
			public PlanningPhaseMessage(final Game pGame) {
				super(pGame, GameState.PLANNING_PHASE);
				players = playerMap.get(pGame);
			}

			@SerializedName(Player.JSON_TAG_COLLECTION)
			private final HashSet<Player> players;
		}
		/**
		 * @}
		 */

		private PlayerMap playerMap = new PlayerMap();

		private String getListFileName() {
			return getFilesDir() + "/hosted_games.json";
		}

		/**
		 * get decisions from player
		 *
		 * @param game
		 * @param player
		 * @param decisions
		 */
		private void onReceiveDecisions(final Game pGame, final Player pPlayer,
		                                final String pSoldiers)
		{
			if(!pGame.isInState(Server.this, GameState.PLANNING_PHASE))
			{
				Log.e(TAG, getString(R.string.error_wrong_state,
				                     pGame, pGame.getState(Server.this)));
				return;
			}

			// TODO handle decisions
		}

		/**
		 * inform clients about planning phase
		 *
		 * @param pContext activity context
		 * @param pGame game
		 */
		private void startPlanningPhase(final Game pGame)
		{
			Log.d(TAG, "Starting planning phase...");

			if(!pGame.isInState(Server.this, GameState.JOINED))
			{
				Log.e(TAG, getString(R.string.error_wrong_state,
				                     pGame, pGame.getState(Server.this)));
				return;
			}

			try {
				pGame.setState(Server.this, GameState.PLANNING_PHASE);
			} catch (final IOException e) {
				Log.e(TAG, "Could not store game info!", e);
				return;
			}

			final Message message = new PlanningPhaseMessage(pGame);

			try {
				dtn.getService().sendToClients(message.saveToFile(Server.this));
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private Settings getSettings()
	{
		return ((App) getApplication()).getSettings();
	}
}
