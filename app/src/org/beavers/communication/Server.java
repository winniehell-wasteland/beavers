package org.beavers.communication;

import org.beavers.Settings;
import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;
import org.beavers.gameplay.GameState;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.PlayerID;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import de.tubs.ibr.dtn.api.GroupEndpoint;

/**
 * server for game communication
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class Server {

	private static final String TAG = "Server";
	public static final GroupEndpoint GROUP_EID = new GroupEndpoint("dtn://beavergame.dtn/server");

	/**
	 * @name intents
	 * @{
	 */
	public static final String ANNOUNCED_INTENT = Server.class.getName()+".ANNOUNCED";
	public static final String PLANNING_PHASE_INTENT = Server.class.getName()+".PLANNING_PHASE";
	/**
	 * @}
	 */

	public static GameList hostedGames = new GameList();

	public static void handlePayload(final Context pContext, final JSONObject json) {
		if(json.has("state"))
		{
			switch (GameState.valueOf(json.optString("state"))) {
			case JOINED:

				final GameInfo game = GameInfo.fromJSON(json.opt("game"));
				final PlayerID player = PlayerID.fromJSON(json.opt("player"));

				if(game.getServer().equals(Settings.playerID))
				{
					addPlayer(pContext, game, player);
				}

				break;
			default:
				break;
			}
		}
	}

	/**
	 * level select, inform possible clients
	 */
	public static void initiateGame(final Context pContext, final GameInfo pGame)
	{
		if(hostedGames.contains(pGame))
		{
			Log.e("Server", "Game "+pGame+" already exists!");
			return;
		}

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.ANNOUNCED.toJSON());
			json.put("game", pGame.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, "Could not generate JSON!", e);
		}

		CustomDTNDataHandler.sendToClient(pContext, json);

		pGame.setState(GameState.ANNOUNCED);
		hostedGames.add(pGame);

		Client.joinGame(pContext, pGame);
	}

	/**
	 * player tries to join game
	 * @param player
	 */
	public static void addPlayer(final Context pContext, final GameInfo pGame, final PlayerID pPlayer)
	{
		// TODO wait for all players, check if game still available

		gameReady(pContext, pGame);
	}

	/**
	 * server tries to start game
	 */
	public static void gameReady(final Context pContext, final GameInfo pGame)
	{
		if(!hostedGames.contains(pGame))
		{
			Log.e("Server", "Could not find game "+pGame+"!");
			return;
		}

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.STARTED.toJSON());
			json.put("game", pGame.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, "Could not generate JSON!", e);
		}

		CustomDTNDataHandler.sendToClient(pContext, json);

		hostedGames.find(pGame).setState(GameState.STARTED);
	}

	/**
	 * inform clients about planning phase
	 */
	public static void startPlanningPhase(final Context pContext, final GameInfo pGame)
	{
		if(!hostedGames.contains(pGame))
		{
			Log.e("Server", "Could not find game "+pGame+"!");
			return;
		}

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.PLANNING_PHASE.toJSON());
			json.put("game", pGame.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, "Could not generate JSON!", e);
		}

		CustomDTNDataHandler.sendToClient(pContext, json);

		hostedGames.find(pGame).setState(GameState.PLANNING_PHASE);
	}

	/**
	 * get decisions from player
	 * @param game
	 * @param player
	 * @param decisions
	 */
	public static void receiveDecisions(final GameInfo pGame, final PlayerID pPlayer, final DecisionContainer pDecisions)
	{

	}

	/**
	 * distribute outcome to clients
	 */
	public static void distributeOutcome(final GameInfo pGame, final OutcomeContainer outcome)
	{

	}

	/**
	 * player quits game
	 * @param player
	 */
	public static void playerAbort(final Context pContext, final GameInfo pGame, final PlayerID pPlayer)
	{
		if(pGame.getServer().equals(pPlayer))
		{
			announceNewServer(pContext, pGame, null);
		}
	}

	/**
	 * server has quit, inform clients about new server
	 * @param pGame old game info
	 * @param pServer new server
	 */
	public static void announceNewServer(final Context pContext, final GameInfo pGame, final PlayerID pServer)
	{
		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.ABORTED.toJSON());
			json.put("game", pGame.toJSON());
			json.put("new_server", pServer.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, "Could not generate JSON!", e);
		}

		CustomDTNDataHandler.sendToClient(pContext, json);
	}

	/**
	 * @name lifetime constants
	 * @{
	 */
	//private static final int ANNOUNCEMENT_LIFETIME = DEFAULT_LIFETIME;
	/**
	 * @}
	 */
}
