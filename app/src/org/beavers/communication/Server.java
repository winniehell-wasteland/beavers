package org.beavers.communication;

import org.beavers.R;
import org.beavers.Settings;
import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;
import org.beavers.gameplay.GameState;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.Player;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
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
	 * @name intents
	 * @{
	 */
	public static final String ANNOUNCED_INTENT =
			Server.class.getName()+".ANNOUNCED";
	public static final String PLANNING_PHASE_INTENT =
			Server.class.getName()+".PLANNING_PHASE";
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
	public static void acquireGame(final Context pContext, final GameInfo pGame) {
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
			GameInfo pGame, final Player pPlayer)
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

		final PlayerSet gamePlayers = players.get(pGame);

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
			final JSONObject pJSON) {
		if(pJSON.has("state"))
		{
			GameInfo game = GameInfo.fromJSON(pJSON.opt("game"));

			if(!game.isServer(Settings.playerID))
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

			switch (GameState.valueOf(pJSON.optString("state"))) {
			case JOINED:
			{
				addPlayer(pContext, game,
						Player.fromJSON(pJSON.opt("player")));

				break;
			}
			case PLANNING_PHASE:
			{
				onReceiveDecisions(pContext, game,
						Player.fromJSON(pJSON.opt("player")),
						DecisionContainer.fromJSON(pJSON.opt("decisions")));

				break;
			}
			case ABORTED:
			{
				onPlayerAborts(pContext, game,
						Player.fromJSON(pJSON.opt("player")));

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

		Log.d(TAG, "Add "+pGame.toString());

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.ANNOUNCED.toJSON());
			json.put("game", pGame.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, pContext.getString(R.string.error_json), e);
		}

		CustomDTNDataHandler.sendToClients(pContext, json);
	}

	/**
	 * distribute outcome to clients
	 */
	public static void distributeOutcome(final Context pContext,
			final GameInfo pGame,
			final OutcomeContainer outcome)
	{
		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.EXECUTION_PHASE.toJSON());
			json.put("game", pGame.toJSON());
			json.put("outcome", outcome.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, pContext.getString(R.string.error_json), e);
		}

		CustomDTNDataHandler.sendToClients(pContext, json);
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
	 * server has quit, inform clients about new server
	 *
	 * @param pContext activity context
	 * @param pGame old game info
	 * @param pServer new server
	 */
	private static void announceNewServer(final Context pContext, final GameInfo pGame, final Player pServer)
	{
		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.ABORTED.toJSON());
			json.put("game", pGame.toJSON());
			json.put("new_server", pServer.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, pContext.getString(R.string.error_json), e);
		}

		CustomDTNDataHandler.sendToClients(pContext, json);
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
			GameInfo pGame)
	{
		if(!hostedGames.contains(pGame))
		{
			Log.e(TAG, pContext.getString(R.string.error_not_hosted,
					pGame.toString()));
			return;
		}

		pGame = hostedGames.find(pGame);

		if(!pGame.isInState(GameState.ANNOUNCED))
		{
			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));
			return;
		}

		pGame.setState(GameState.PLANNING_PHASE);

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.PLANNING_PHASE.toJSON());
			json.put("game", pGame.toJSON());
			json.put("players", players.get(pGame).toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, pContext.getString(R.string.error_json), e);
		}

		CustomDTNDataHandler.sendToClients(pContext, json);
	}
}
