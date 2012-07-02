package org.beavers.communication;

import java.lang.reflect.Type;
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
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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

		runningGames.remove(pGame);
		broadcastGameInfo(pContext, pGame);

		final Message message = new ClientMessage(pGame);
		CustomDTNDataHandler.sendToServer(pContext, pGame.getServer(),
		                                  message.toJsonObject());
	}

	/**
	 * receive a DTN message
	 *
	 * @param pContext activity context
	 * @param pJSON payload in JSON format
	 */
	public static void handlePayload(final Context pContext,
	                                 final JsonObject pJSON) {
		if(pJSON.has("game"))
		{
			final Gson gson = CustomGSON.getInstance();

			final GameInfo game =
				gson.fromJson(pJSON.get("game"), GameInfo.class);
			Log.e(TAG, "game: "+game);

			switch (game.getState()) {
			case ABORTED:
			{
				if(runningGames.contains(game))
				{
					onReceiveNewServer(pContext, runningGames.find(game),
					                   gson.fromJson(pJSON.get("new_server"),
					                                 Player.class));
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
					                 gson.fromJson(pJSON.get("outcome"),
					                               OutcomeContainer.class));
				}

				break;
			}
			case PLANNING_PHASE:
			{

				if(runningGames.contains(game) || announcedGames.contains(game))
				{
					final Type type =
						new TypeToken<HashSet<Player>>() {}.getType();

					final HashSet<Player> players =
						gson.fromJson(pJSON.get("players"), type);

					onReceiveStartPlanningPhase(pContext,
					                            runningGames.find(game),
					                            players);
				}

				break;
			}
			default:
				break;
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
			                              pGame.toString(),
			                              pGame.getState().toString()));
			return;
		}

		pGame.setState(GameState.JOINED);
		broadcastGameInfo(pContext, pGame);

		final Message message = new ClientMessage(pGame);
		CustomDTNDataHandler.sendToServer(pContext, pGame.getServer(),
		                                  message.toJsonObject());
	}

	/**
	 * send decisions to server
	 *
	 * @param pContext activity context
	 * @param pGame running game
	 * @param decisions
	 */
	public static void sendDecisions(final Context pContext, final GameInfo pGame,
			final DecisionContainer pDecisions) {
		if (!runningGames.contains(pGame)) {
			Log.e(TAG, pContext.getString(R.string.error_not_running,
					pGame.toString()));
			return;
		}

		final GameInfo rgame = runningGames.find(pGame);

		if (!pGame.isInState(GameState.PLANNING_PHASE)) {
			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
			                              rgame.toString(),
			                              rgame.getState().toString()));
			return;
		}

		final Message message = new DecissionMessage(pGame, pDecisions);
		CustomDTNDataHandler.sendToServer(pContext, rgame.getServer(),
		                                  message.toJsonObject());
	}

	/**
	 * @name messages
	 * @{
	 */

	static class ClientMessage extends Message
	{
		Player player = Settings.player;

		ClientMessage(final GameInfo pGame) {
			super(pGame);
		}

	}

	static class DecissionMessage extends ClientMessage
	{
		DecisionContainer decisions;

		DecissionMessage(final GameInfo pGame,
		                 final DecisionContainer pDecisions) {
			super(pGame);
			decisions = pDecisions;
		}
	}
	/**
	 * @}
	 */

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
		announcedGames.add(pGame);
		broadcastGameInfo(pContext, pGame);

		if(pGame.isServer(Settings.player))
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
		if (Settings.player.equals(pNewServer)) {
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
			final GameInfo pGame, final HashSet<Player> players) {
		if(!pGame.isInState(GameState.JOINED)
		   && !pGame.isInState(GameState.EXECUTION_PHASE)) {

			Log.e(TAG, pContext.getString(R.string.error_wrong_state,
					pGame.toString(), pGame.getState().toString()));

			return;
		}

		if (pGame.isInState(GameState.JOINED))
		{
			announcedGames.remove(pGame);

			if(players.contains(Settings.player))
			{
				runningGames.add(pGame);
			}
		}
		else if(!players.contains(Settings.player))
		{
			// TODO do something, we got kicked out!
		}

		// start planning phase
		pGame.setState(GameState.PLANNING_PHASE);
		broadcastGameInfo(pContext, pGame);
	}
}
