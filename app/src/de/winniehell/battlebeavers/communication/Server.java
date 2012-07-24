/*
	(c) winniehell (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers.communication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

import de.winniehell.battlebeavers.App;
import de.winniehell.battlebeavers.R;
import de.winniehell.battlebeavers.Settings;
import de.winniehell.battlebeavers.communication.DTNService.Message;
import de.winniehell.battlebeavers.gameplay.Game;
import de.winniehell.battlebeavers.gameplay.GameInfo;
import de.winniehell.battlebeavers.gameplay.GameState;
import de.winniehell.battlebeavers.gameplay.Player;
import de.winniehell.battlebeavers.storage.CustomGSON;
import de.winniehell.battlebeavers.storage.Outcome;
import de.winniehell.battlebeavers.storage.SoldierList;

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

		executor = Executors.newSingleThreadExecutor();

		try {
			implementation.loadPlayerMap();
		} catch (final ServerRemoteException e) {
			e.log();
		}

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

		try {
			implementation.savePlayerMap();
		} catch (final ServerRemoteException e) {
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

        	Log.d(TAG, "scheduling handleData()");
			executor.execute(new Runnable() {

				@Override
				public void run() {
					try {
			        	Log.d(TAG, "running handleData()");
						implementation.handleData(fileName);
					} catch (final ServerRemoteException e) {
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

	public class ServerRemoteException extends RemoteException {

		/** @see {@link Serializable} */
		private static final long serialVersionUID = 5092163419629856671L;

		public ServerRemoteException(final int pResId,
		                             final Object... pFormatArgs) {
			this(pResId, null, pFormatArgs);
		}


		public ServerRemoteException(final int pResId,
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
			Log.e(TAG, this.getClass().getSimpleName());
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

	private ExecutorService executor;
	private final Implementation implementation = new Implementation();

	/** implementation of the {@link IServer} interface */
	private class Implementation extends IServer.Stub {

		public Implementation() {
			Log.d(TAG, "Create player map...");

			playerMap = new PlayerMap();
		}

		@Override
		public synchronized void addPlayer(final Game pGame,
		                                   final Player pPlayer)
		                         throws ServerRemoteException
		{
			Log.d(TAG, pPlayer.getName() + " joins "+pGame.getName());

			// game is unjoinable
			if(!pGame.isInState(Server.this, GameState.JOINED)) {
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
		public void deleteGames() throws RemoteException {
			playerMap.clear();
		}

		@Override
		public void distributeOutcome(final Game pGame)
		            throws ServerRemoteException
		{
			Outcome outcome;
			
			try {
				outcome = pGame.getOutcome(Server.this);
			} catch (final IOException e) {
				throw new ServerRemoteException(R.string.error_game_load_outcome, e, pGame);
			}
			
			final Message message = new OutcomeMessage(pGame, outcome);
			
			try {
				dtn.getService().sendToClients(message.saveToFile(Server.this));
			} catch (final RemoteException e) {
				throw new ServerRemoteException(R.string.error_dtn_sending, e);
			}
			
			if(!pGame.isInState(Server.this, GameState.WON, GameState.LOST)) {
				startPlanningPhase(pGame);
			}
		}

		/**
		 * receive a DTN message
		 *
		 * @param pData file descriptor of payload file
		 * @return true if handled
		 * @throws ServerRemoteException
		 */
		public boolean handleData(final String pFileName)
		               throws ServerRemoteException {
			final File input = new File(pFileName);

			Log.i(TAG, "Processing "+input.length()+" bytes...");

			JsonObject json = new JsonObject();

			try {
				final JsonParser parser = new JsonParser();
				final FileReader reader = new FileReader(input);

				json = (JsonObject) parser.parse(reader);
			} catch (final Exception e) {
				throw new ServerRemoteException(
					R.string.error_dtn_receiving, e
				);
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
					throw new ServerRemoteException(
						R.string.error_game_not_hosted, game
					);
				}

				final Player player =
					gson.fromJson(json.get(Player.JSON_TAG), Player.class);

				if(!json.has(GameState.JSON_TAG))
				{
					throw new ServerRemoteException(
						R.string.error_json_missing_element, GameState.JSON_TAG
					);
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
					if(!json.has(SoldierList.JSON_TAG))
					{
						throw new ServerRemoteException(
							R.string.error_json_missing_element,
							game, SoldierList.JSON_TAG
						);
					}

					final JsonElement decisions =
						json.get(SoldierList.JSON_TAG);

					onReceiveDecisions(game, player, decisions);

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
		public void initiateGame(final Game pGame) throws ServerRemoteException
		{
			if(!pGame.isInState(Server.this, GameState.UNKNOWN))
			{
				throw new ServerRemoteException(R.string.error_game_exists,
				                                pGame);
			}

			final File dir = new File(pGame.getDirectory(Server.this));

			if(dir.exists()) {
				throw new ServerRemoteException(
					R.string.error_game_dir_exists, pGame
				);
			}

			dir.mkdirs();

			final String map = getSettings().getDefaultMapName();

			try {
				final GameInfo info = new GameInfo(map, 0);
				info.setState(GameState.ANNOUNCED);
				info.saveToFile(Server.this, pGame);
			} catch (final IOException e) {
				throw new ServerRemoteException(
					R.string.error_game_save_state, e, pGame
				);
			}

			try {
				final Message message = new AnnouncementMessage(pGame, map);
				dtn.getService().sendToClients(message.saveToFile(Server.this));
			} catch (final RemoteException e) {
				throw new ServerRemoteException(R.string.error_dtn_sending, e);
			}
		}

		@Override
		public void loadPlayerMap() throws ServerRemoteException {

			/*
			try {
				Log.d(TAG, "loading: "+StreamUtils.readFully(new FileInputStream(getListFileName())));
			} catch (final Exception e) {
				Log.e(TAG, e.getMessage());
				return;
			}
			*/

			final JsonReader reader;
			try {
				reader = CustomGSON.getReader(Server.this, getListFileName());
			} catch (final FileNotFoundException e) {
				// file does not exist
				return;
			}

			synchronized (playerMap) {
				final Gson gson = CustomGSON.getInstance();
				playerMap.clear();

				try {
					reader.beginArray();

					while(reader.hasNext()) {
						reader.beginObject();

						CustomGSON.assertElement(reader, Game.JSON_TAG);
						final Game game = gson.fromJson(reader, Game.class);

						CustomGSON.assertElement(
							reader, Player.JSON_TAG_COLLECTION
						);
						final PlayerSet players = new PlayerSet();

						reader.beginArray();
						while(reader.hasNext()) {
							players.add(
								(Player) gson.fromJson(reader, Player.class)
							);
						}
						reader.endArray();

						reader.endObject();

						playerMap.put(game, players);
					}

					reader.endArray();
				} catch(final Exception e) {
					throw new ServerRemoteException(
						R.string.error_json_reading, e
					);
				} finally {
					try {
						reader.close();
					} catch (final IOException e) {
						throw new ServerRemoteException(
							R.string.error_json_reading, e
						);
					}
				}
			}
		}

		@Override
		public void savePlayerMap() throws ServerRemoteException {
			JsonWriter writer;

			try {
				writer = CustomGSON.getWriter(Server.this, getListFileName());
			} catch (final FileNotFoundException e) {
				throw new ServerRemoteException(R.string.error_json_writing, e);
			}

			synchronized(playerMap) {
				final Gson gson = CustomGSON.getInstance();

				try {
					writer.beginArray();

					for(final Game game : playerMap.keySet()) {
						writer.beginObject();

						writer.name(Game.JSON_TAG);
						gson.toJson(game, Game.class, writer);

						writer.name(Player.JSON_TAG_COLLECTION);
						gson.toJson(playerMap.get(game), PlayerSet.class, writer);

						writer.endObject();
					}

					writer.endArray();
				} catch (final Exception e) {
					throw new ServerRemoteException(
						R.string.error_json_writing, e
					);
				} finally {
					try {
						writer.close();
					} catch (final IOException e) {
						throw new ServerRemoteException(
							R.string.error_json_writing, e
						);
					}
				}
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
			                      final Outcome pOutcome) {
				super(pGame, GameState.EXECUTION_PHASE);
				outcome = pOutcome;
			}

			@SerializedName(Outcome.JSON_TAG)
			private final Outcome outcome;
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

		private final PlayerMap playerMap;

		private String getListFileName() {
			return getFilesDir() + "/player_map.json";
		}

		/**
		 * player quits game
		 *
		 * @param pGame game
		 * @param pPlayer player
		 */
		private void onPlayerAborts(final Game pGame, final Player pPlayer)
		{
			// TODO remove player from map and stop game
		}

		/**
		 * get decisions from player
		 *
		 * @param pGame
		 * @param pPlayer
		 * @param pDecisions
		 * @throws ServerRemoteException
		 */
		private void onReceiveDecisions(final Game pGame, final Player pPlayer,
		                                final JsonElement pDecisions)
		             throws ServerRemoteException
		{
			if(!pGame.isInState(Server.this, GameState.PLANNING_PHASE))
			{
				throw new ServerRemoteException(
					R.string.error_game_wrong_state,
					pGame, pGame.getState(Server.this)
				);
			}

			try {
				pGame.writeDecisions(
					Server.this, pGame.isServer(pPlayer)?0:1, pDecisions
				);
			} catch (final IOException e) {
				throw new ServerRemoteException(
					R.string.error_game_write_decisions, e, pGame
				);
			}

			for(int team = 0; team < getSettings().getMaxPlayers(); ++team) {
				if(!pGame.hasDecisions(Server.this, team)) {
					// wait for the rest
					return;
				}
			}

			try {
				pGame.setState(Server.this, GameState.EXECUTION_PHASE);
			} catch (final IOException e) {
				throw new ServerRemoteException(
					R.string.error_game_save_state, e, pGame
				);
			}

			final Intent update_intent = new Intent(Game.STATE_CHANGED_INTENT);
			update_intent.putExtra(Game.PARCEL_NAME, pGame);
			sendBroadcast(update_intent);
		}

		/**
		 * inform clients about planning phase
		 *
		 * @param pContext activity context
		 * @param pGame game
		 * @throws ServerRemoteException
		 */
		private void startPlanningPhase(final Game pGame)
		             throws ServerRemoteException
		{
			Log.d(TAG, "Starting planning phase...");

			// we are not part of our game
			if(!pGame.isInState(Server.this, GameState.JOINED, GameState.EXECUTION_PHASE))
			{
				throw new ServerRemoteException(
					R.string.error_game_wrong_state,
					pGame, pGame.getState(Server.this)
				);
			}

			final Message message = new PlanningPhaseMessage(pGame);

			try {
				dtn.getService().sendToClients(message.saveToFile(Server.this));
			} catch (final RemoteException e) {
				throw new ServerRemoteException(R.string.error_dtn_sending, e);
			}
		}
	};

	private Settings getSettings()
	{
		return ((App) getApplication()).getSettings();
	}
}
