package org.beavers.communication;

import java.util.HashSet;

import org.beavers.R;
import org.beavers.Settings;
import org.beavers.communication.CustomDTNDataHandler.Message;
import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;
import org.beavers.gameplay.GameState;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.Player;
import org.beavers.storage.CustomGSON;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import de.tubs.ibr.dtn.api.GroupEndpoint;

/**
 * server for game communication
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public enum Server {
	; // prevent instantiation

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = Server.class.getName();
	/**
	 * @}
	 */

	/**
	 * @name DTN
	 * @{
	 */
	public static final GroupEndpoint GROUP_EID =
			new GroupEndpoint("dtn://beavergame.dtn/server");
	/**
	 * @}
	 */

	/**
	 * game container
	 */
	public static GameList hostedGames = new GameList();

	/**
	 * become server of an existing game
	 *
	 * @param pContext activity context
	 * @param pGame game
	 *
	 */
	public static void acquireGame(final Context pContext,
	                               final GameInfo pGame) {
		if(hostedGames.contains(pGame))
		{
			Log.e(TAG, pContext.getString(R.string.error_game_exists,
					pGame.toString()));
			return;
		}

		hostedGames.add(pGame);
	}

	/**
	 * player tries to join game
	 *
	 * @param pContext activity context
	 * @param pGame game
	 * @param pPlayer player
	 *
	 */
	public static synchronized void addPlayer(final Context pContext,
	                                          GameInfo pGame,
	                                          final Player pPlayer)
	{
		Log.d(TAG, "Somebody joins "+pGame.toString());

		if(!hostedGames.contains(pGame))
		{
			Log.e(TAG, pContext.getString(R.string.error_not_hosted,
			                              pGame.toString()));
			return;
		}

		pGame = hostedGames.find(pGame);

		// TODO allow more than 2 players
		final int MAX_PLAYERS = 2;

		final HashSet<Player> gamePlayers = players.get(pGame);

		if(gamePlayers.size() < MAX_PLAYERS)
		{
			gamePlayers.add(pPlayer);

			if(gamePlayers.size() == MAX_PLAYERS)
			{
				startPlanningPhase(pContext, pGame);
			}
		}
	}

	/**
	 * receive a DTN message
	 *
	 * @param pContext activity context
	 * @param pJSON payload in JSON format
	 */
	public static void handlePayload(final Context pContext,
			final JsonObject pJSON) {
		if(pJSON.has("game") && pJSON.has("player"))
		{
			final Gson gson = CustomGSON.getInstance();

			GameInfo game = gson.fromJson(pJSON.get("game"), GameInfo.class);

			if(!game.isServer(Settings.player))
			{
				return;
			}

			if(!hostedGames.contains(game))
			{
				Log.e(TAG, pContext.getString(R.string.error_not_hosted,
						game.toString()));
				return;
			}

			game = hostedGames.find(game);

			final Player player =
				gson.fromJson(pJSON.get("player"), Player.class);

			switch (game.getState()) {
			case JOINED:
			{
				addPlayer(pContext, game, player);

				break;
			}
			case PLANNING_PHASE:
			{
				onReceiveDecisions(pContext, game, player,
				                   gson.fromJson(pJSON.get("decisions"),
				                		         DecisionContainer.class));

				break;
			}
			case ABORTED:
			{
				onPlayerAborts(pContext, game, player);

				break;
			}
			default:
				break;
			}
		}
	}

	/**
	 * level select, inform possible clients
	 *
	 * @param pContext activity context
	 * @param pGame new game
	 */
	public static void initiateGame(final Context pContext,
			final GameInfo pGame)
	{
		if(hostedGames.contains(pGame))
		{
			Log.e(TAG, pContext.getString(R.string.error_game_exists,
					pGame.toString()));
			return;
		}

		pGame.setState(GameState.ANNOUNCED);
		hostedGames.add(pGame);

		final Message message = new Message(pGame);
		CustomDTNDataHandler.sendToClients(pContext, message.toJsonObject());
	}

	/**
	 * distribute outcome to clients
	 */
	public static void distributeOutcome(final Context pContext,
	                                     final GameInfo pGame,
	                                     final OutcomeContainer pOutcome)
	{
		final Message message = new OutcomeMessage(pGame, pOutcome);
		CustomDTNDataHandler.sendToClients(pContext, message.toJsonObject());
	}

	/**
	 * player quits game
	 *
	 * @param pContext activity context
	 * @param pGame game
	 * @param pPlayer player
	 */
	public static void onPlayerAborts(final Context pContext,
			final GameInfo pGame, final Player pPlayer)
	{
		if(pGame.getServer().equals(pPlayer))
		{
			// TODO find new server
			final Player newServer = null;
			announceNewServer(pContext, pGame, newServer);
		}
	}

	private static PlayerMap players = new PlayerMap();

	/**
	 * @name messages
	 * @{
	 */
	static class NewServerMessage extends Message
	{
		Player new_server;

		NewServerMessage(final GameInfo pGame, final Player pServer) {
			super(pGame);
			new_server = pServer;
		}
	}

	static class OutcomeMessage extends Message
	{
		OutcomeContainer outcome;

		OutcomeMessage(final GameInfo pGame, final OutcomeContainer pOutcome) {
			super(pGame);
			outcome = pOutcome;
		}
	}

	static class PlanningPhaseMessage extends Message
	{
		HashSet<Player> players;

		PlanningPhaseMessage(final GameInfo pGame) {
			super(pGame);
			players = Server.players.get(pGame);
		}
	}
	/**
	 * @}
	 */

	/**
	 * server has quit, inform clients about new server
	 *
	 * @param pContext activity context
	 * @param pGame old game info
	 * @param pServer new server
	 */
	private static void announceNewServer(final Context pContext,
	                                      final GameInfo pGame,
	                                      final Player pServer)
	{
		final Message message = new NewServerMessage(pGame, pServer);
		CustomDTNDataHandler.sendToClients(pContext, message.toJsonObject());
	}

	/**
	 * get decisions from player
	 *
	 * @param pContext activity context
	 * @param game
	 * @param player
	 * @param decisions
	 */
	private static void onReceiveDecisions(final Context pContext,
			GameInfo pGame, final Player pPlayer,
			final DecisionContainer pDecisions)
	{
		if(!hostedGames.contains(pGame))
		{
			Log.e(TAG, pContext.getString(R.string.error_not_hosted,
					pGame.toString()));
			return;
		}

		pGame = hostedGames.find(pGame);

		if(!pGame.isInState(GameState.PLANNING_PHASE))
		{
			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));
			return;
		}

		// TODO handle decisions
		distributeOutcome(pContext, pGame, new OutcomeContainer());
	}

	/**
	 * inform clients about planning phase
	 *
	 * @param pContext activity context
	 * @param pGame game
	 */
	private static void startPlanningPhase(final Context pContext,
			final GameInfo pGame)
	{
		if(!pGame.isInState(GameState.ANNOUNCED))
		{
			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));
			return;
		}

		pGame.setState(GameState.PLANNING_PHASE);

		final Message message = new PlanningPhaseMessage(pGame);
		CustomDTNDataHandler.sendToClients(pContext, message.toJsonObject());
	}
}
