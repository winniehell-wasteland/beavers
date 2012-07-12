package org.beavers.communication;

import java.io.FileReader;
import java.util.HashSet;

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
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

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
		return stub;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();

		client = new Client.Connection();
		bindService(new Intent(Server.this, Client.class), client,
		            Service.BIND_AUTO_CREATE);

		dtn = new DTNService.Connection();
		bindService(new Intent(Server.this, DTNService.class), dtn,
		            Service.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		unbindService(client);
		unbindService(dtn);

		super.onDestroy();
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

	/**
	 * @name service connections
	 * @{
	 */
	private Client.Connection client;
	private DTNService.Connection dtn;
	/**
	 * @}
	 */

	/** implementation of the {@link IServer} interface */
	private final IServer.Stub stub = new IServer.Stub() {

		@Override
		public void acquireGame(final GameInfo pGame) {
			if(hostedGames.contains(pGame))
			{
				Log.e(TAG, getString(R.string.error_game_exists, pGame));
				return;
			}

			hostedGames.add(pGame);
		}

		@Override
		public synchronized void addPlayer(GameInfo pGame, final Player pPlayer)
		{
			Log.d(TAG, "Somebody joins "+pGame.toString());

			if(!hostedGames.contains(pGame))
			{
				Log.e(TAG, getString(R.string.error_not_hosted, pGame));
				return;
			}

			pGame = hostedGames.find(pGame);

			// TODO allow more than 2 players
			final int MAX_PLAYERS = 2;

			final HashSet<Player> gamePlayers = playerMap.get(pGame);

			if(gamePlayers.size() < MAX_PLAYERS)
			{
				gamePlayers.add(pPlayer);

				if(gamePlayers.size() == MAX_PLAYERS)
				{
					startPlanningPhase(pGame);
				}
			}
		}

		@Override
		public boolean handleData(final ParcelFileDescriptor pData) {
			final JsonParser parser = new JsonParser();
			final FileReader reader = new FileReader(pData.getFileDescriptor());

			final JsonObject json = (JsonObject) parser.parse(reader);

			if(json.has(GameInfo.JSON_TAG) && json.has(Player.JSON_TAG))
			{
				final Gson gson = CustomGSON.getInstance();

				GameInfo game =
					gson.fromJson(json.get(GameInfo.JSON_TAG), GameInfo.class);

				if(!game.isServer(getSettings().getPlayer()))
				{
					return false;
				}

				if(!hostedGames.contains(game))
				{
					Log.e(TAG, getString(R.string.error_not_hosted, game));
					return false;
				}

				game = hostedGames.find(game);

				final Player player =
					gson.fromJson(json.get(Player.JSON_TAG), Player.class);

				switch (game.getState()) {
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
		public void initiateGame(final GameInfo pGame)
		{
			if(hostedGames.contains(pGame))
			{
				Log.e(TAG, getString(R.string.error_game_exists, pGame));
				return;
			}

			pGame.setState(GameState.ANNOUNCED);
			hostedGames.add(pGame);

			final Message message = new Message(Server.this, pGame);

			try {
				dtn.getService().sendToClients(message.getFile());
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void distributeOutcome(final GameInfo pGame,
		                              final OutcomeContainer pOutcome)
		{
			final Message message =
				new OutcomeMessage(Server.this, pGame, pOutcome);

			try {
				dtn.getService().sendToClients(message.getFile());
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * player quits game
		 *
		 * @param pGame game
		 * @param pPlayer player
		 */
		public void onPlayerAborts(final GameInfo pGame, final Player pPlayer)
		{
			if(pGame.getServer().equals(pPlayer))
			{
				// TODO find new server
				final Player newServer = null;
				announceNewServer(pGame, newServer);
			}
		}

		/**
		 * @name messages
		 * @{
		 */
		@SuppressWarnings("unused")
		class NewServerMessage extends Message
		{
			public NewServerMessage(final Context pContext,
			                        final GameInfo pGame,
			                        final Player pServer) {
				super(pContext, pGame);
				new_server = pServer;
			}

			private final Player new_server;
		}

		class OutcomeMessage extends Message
		{
			public OutcomeMessage(final Context pContext, final GameInfo pGame,
			                      final OutcomeContainer pOutcome) {
				super(pContext, pGame);
				outcome = pOutcome;
			}

			@SerializedName(OutcomeContainer.JSON_TAG)
			private final OutcomeContainer outcome;
		}

		class PlanningPhaseMessage extends Message
		{
			public PlanningPhaseMessage(final Context pContext,
			                            final GameInfo pGame) {
				super(pContext, pGame);
				players = playerMap.get(pGame);
			}

			@SerializedName(Player.JSON_TAG_COLLECTION)
			private final HashSet<Player> players;
		}
		/**
		 * @}
		 */

		/** game container */
		private final GameList hostedGames = new GameList();

		private final PlayerMap playerMap = new PlayerMap();

		/**
		 * server has quit, inform clients about new server
		 *
		 * @param pGame old game info
		 * @param pServer new server
		 */
		private void announceNewServer(final GameInfo pGame,
		                               final Player pServer)
		{
			final Message message =
				new NewServerMessage(Server.this, pGame, pServer);

			try {
				dtn.getService().sendToClients(message.getFile());
			} catch (final RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * get decisions from player
		 *
		 * @param game
		 * @param player
		 * @param decisions
		 */
		private void onReceiveDecisions(GameInfo pGame, final Player pPlayer,
		                                final String pSoldiers)
		{
			if(!hostedGames.contains(pGame))
			{
				Log.e(TAG, getString(R.string.error_not_hosted,
						pGame.toString()));
				return;
			}

			pGame = hostedGames.find(pGame);

			if(!pGame.isInState(GameState.PLANNING_PHASE))
			{
				Log.e(TAG, getString(R.string.error_wrong_state,
						pGame.toString(), pGame.getState().toString()));
				return;
			}

			// TODO handle decisions
			distributeOutcome(pGame, new OutcomeContainer());
		}

		/**
		 * inform clients about planning phase
		 *
		 * @param pContext activity context
		 * @param pGame game
		 */
		private void startPlanningPhase(final GameInfo pGame)
		{
			if(!pGame.isInState(GameState.ANNOUNCED))
			{
				Log.e(TAG, getString(R.string.error_wrong_state,
						pGame.toString(), pGame.getState().toString()));
				return;
			}

			pGame.setState(GameState.PLANNING_PHASE);

			final Message message =
				new PlanningPhaseMessage(Server.this, pGame);

			try {
				dtn.getService().sendToClients(message.getFile());
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
