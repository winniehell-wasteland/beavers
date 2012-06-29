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
import android.content.Intent;
import android.util.Log;
import de.tubs.ibr.dtn.api.GroupEndpoint;

/**
 * client for game communication
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public enum Client {
	; // prevent instantiation

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = Client.class.getName();
	/**
	 * @}
	 */

	/**
	 * @name DTN
	 * @{
	 */
	public static final GroupEndpoint GROUP_EID =
		new GroupEndpoint("dtn://beavergame.dtn/client");
	/**
	 * @}
	 */

	/**
	 * @name intents
	 * @{
	 */
	public static final String GAME_STATE_CHANGED_INTENT =
		Client.class.getName() + ".GAME_STATE_CHANGED";
	public static final String JOIN_GAME_INTENT =
		Client.class.getName() + ".JOIN_GAME";
	/**
	 * @}
	 */

	/**
	 * @name game containers
	 * @{
	 */
	public static GameList announcedGames = new GameList();
	public static GameList runningGames = new GameList();
	/**
	 * @}
	 */

	/**
	 * quit game
	 *
	 * @param pContext activity context
	 * @param pGame game
	 */
	public static void abortGame(final Context pContext, GameInfo pGame) {
		if (!runningGames.contains(pGame)) {
			Log.e(TAG, pContext.getString(R.string.error_not_running,
					pGame.toString()));
			return;
		}

		pGame = runningGames.find(pGame);

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.ABORTED.toJSON());
			json.put("game", pGame.toJSON());
			json.put("player", Settings.playerID.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, pContext.getString(R.string.error_json), e);
		}

		CustomDTNDataHandler.sendToServer(pContext, pGame.getServer(), json);

		runningGames.remove(pGame);
		broadcastGameInfo(pContext, pGame);
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
			Log.e(TAG, "game: "+pJSON.optString("game"));

			final GameInfo game = GameInfo.fromJSON(pJSON.opt("game"));
			final PlayerSet players = PlayerSet.fromJSON(pJSON.opt("players"));

			if((players != null) && !players.contains(Settings.playerID))
			{
				// ignore
				return;
			}

			switch (GameState.valueOf(pJSON.optString("state"))) {
			case ABORTED:
			{
				if(runningGames.contains(game))
				{
					onReceiveNewServer(pContext, runningGames.find(game),
						Player.fromJSON(pJSON.opt("new_server")));
				}

				break;
			}
			case ANNOUNCED:
			{
				if(announcedGames.contains(game)) {
					Log.e(TAG,
						pContext.getString(R.string.error_already_announced,
							game.toString()));
				}
				else
				{
					onReceiveGameInfo(pContext, game);
				}

				break;
			}
			case EXECUTION_PHASE:
			{
				if(runningGames.contains(game))
				{
					onReceiveOutcome(pContext, runningGames.find(game),
						OutcomeContainer.fromJSON(pJSON.opt("outcome")));
				}

				break;
			}
			case PLANNING_PHASE:
			{
				if(runningGames.contains(game))
				{
					onReceiveStartPlanningPhase(pContext,
							runningGames.find(game));
				}

				break;
			}
			}
		}
	}

	/**
	 * join a game
	 *
	 * @param pContext activity context
	 * @param pGame announced game
	 */
	public static void joinGame(final Context pContext, GameInfo pGame) {
		if (!announcedGames.contains(pGame)) {
			Log.e(TAG, pContext.getString(R.string.error_not_announced,
					pGame.toString()));
			return;
		}

		pGame = announcedGames.find(pGame);

		if (!pGame.isInState(GameState.ANNOUNCED)) {
			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));
			return;
		}

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.JOINED.toJSON());
			json.put("game", pGame.toJSON());
			json.put("player", Settings.playerID.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, pContext.getString(R.string.error_json), e);
		}

		CustomDTNDataHandler.sendToServer(pContext, pGame.getServer(), json);

		pGame.setState(GameState.JOINED);
		broadcastGameInfo(pContext, pGame);
	}

	/**
	 * send decisions to server
	 *
	 * @param pContext activity context
	 * @param pGame running game
	 * @param decisions
	 */
	public static void sendDecisions(final Context pContext, GameInfo pGame,
			final DecisionContainer pDecisions) {
		if (!runningGames.contains(pGame)) {
			Log.e(TAG, pContext.getString(R.string.error_not_running,
					pGame.toString()));
			return;
		}

		pGame = runningGames.find(pGame);

		if (!pGame.isInState(GameState.PLANNING_PHASE)) {
			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));
			return;
		}

		final JSONObject json = new JSONObject();

		try {
			json.put("state", GameState.JOINED.toJSON());
			json.put("game", pGame.toJSON());
			json.put("player", Settings.playerID.toJSON());
			json.put("decisions", pDecisions.toJSON());
		} catch (final JSONException e) {
			Log.e(TAG, pContext.getString(R.string.error_json), e);
		}

		CustomDTNDataHandler.sendToServer(pContext, pGame.getServer(), json);
	}

	/**
	 * inform activities about a changed game
	 *
	 * @param pContext activity context
	 * @param pGame changed game
	 */
	private static void broadcastGameInfo(final Context pContext,
			final GameInfo pGame) {
		final Intent update_intent = new Intent(GAME_STATE_CHANGED_INTENT);

		update_intent.putExtra(GameInfo.parcelName, pGame);

		pContext.sendBroadcast(update_intent);
	}

	/**
	 * server has announced new game
	 *
	 * @param pContext activity context
	 * @param pGame new game
	 */
	private static void onReceiveGameInfo(final Context pContext,
                                       final GameInfo pGame) {
		pGame.setState(GameState.ANNOUNCED);
		announcedGames.add(pGame);

		broadcastGameInfo(pContext, pGame);

		if(pGame.isServer(Settings.playerID))
		{
			// auto join own game
			joinGame(pContext, pGame);
		}
	}

	/**
	 * server has quit, inform clients about new server
	 *
	 * @param pContext activity context
	 * @param pGame game
	 * @param pNewServer new server
	 */
	private static void onReceiveNewServer(final Context pContext,
			final GameInfo pGame, final Player pNewServer) {
		if (!pGame.isInState(GameState.PLANNING_PHASE)
				&& !pGame.isInState(GameState.EXECUTION_PHASE)) {
			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));
			return;
		}

		pGame.setServer(pNewServer);

		// we become server
		if (Settings.playerID.equals(pNewServer)) {
			Server.acquireGame(pContext, pGame);
		}

		broadcastGameInfo(pContext, pGame);
	}

	/**
	 * receive outcome from server
	 *
	 * @param pContext activity context
	 * @param pGame game
	 * @param pOutcome
	 */
	private static void onReceiveOutcome(final Context pContext,
			final GameInfo pGame, final OutcomeContainer pOutcome) {
		// TODO handle outcome
	}

	/**
	 * start planning phase
	 *
	 * @param pContext activity context
	 * @param pGame running game
	 */
	private static void onReceiveStartPlanningPhase(final Context pContext,
			final GameInfo pGame) {
		if (!pGame.isInState(GameState.JOINED)
				&& !pGame.isInState(GameState.EXECUTION_PHASE)) {

			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));

			return;
		}

		if (pGame.isInState(GameState.JOINED))
		{
			announcedGames.remove(pGame);
			runningGames.add(pGame);
		}

		// start planning phase
		pGame.setState(GameState.PLANNING_PHASE);
		broadcastGameInfo(pContext, pGame);
	}
}
