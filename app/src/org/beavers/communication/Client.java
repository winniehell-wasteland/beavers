package org.beavers.communication;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.beavers.AppActivity;
import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameList;
import org.beavers.gameplay.GameState;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.PlayerID;

import android.util.Log;
import de.tubs.ibr.dtn.api.GroupEndpoint;

public class Client {

	public static final String TAG = "Client";
	public static final GroupEndpoint GROUP_EID = new GroupEndpoint("dtn://beavergame.dtn/client");
	/**
	 * @name lifetimes
	 * @{
	 */
	private static final int DEFAULT_LIFETIME = 100;
	/**
	 * @}
	 */

	public final GameList announcedGames;
	public final  GameList runningGames;

	/**
	 * default constructor
	 * @param pApp
	 */
	public Client(final AppActivity pApp)
	{
		app = pApp;

		announcedGames = new GameList();
		runningGames = new GameList();

		announcedGames.add(new GameInfo(new PlayerID("foo"), new GameID("bar"))).setState(GameState.ANNOUNCED);
		announcedGames.add(new GameInfo(new PlayerID("test"), new GameID("game"))).setState(GameState.ANNOUNCED);
		announcedGames.add(new GameInfo(new PlayerID("123"), new GameID("456"))).setState(GameState.ANNOUNCED);

		runningGames.add(new GameInfo(new PlayerID("server1"), new GameID("game1")));
		runningGames.add(new GameInfo(new PlayerID("server2"), new GameID("game2")));
		runningGames.add(new GameInfo(new PlayerID("server3"), new GameID("game3")));
		runningGames.add(new GameInfo(new PlayerID("server4"), new GameID("game4")));
	}

	/**
	 * server has announced new game
	 * @param pGame new game
	 */
	public void receiveGameInfo(final GameInfo pGame)
	{
		if(announcedGames.contains(pGame))
		{
			Log.e(TAG, "Game "+pGame+" was already announced!");
			return;
		}

		pGame.setState(GameState.ANNOUNCED);
		announcedGames.add(pGame);
	}

	/**
	 * joins a game
	 * @param pGame announced game
	 */
	public void joinGame(GameInfo pGame)
	{
		pGame = announcedGames.find(pGame);

		if(pGame == null)
		{
			Log.e(TAG, "Game "+pGame+" was not announced!");
			return;
		}

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("JOIN");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());
			stream.println(app.getPlayerID().toString());

			app.getDTNSession().send(Server.GROUP_EID, DEFAULT_LIFETIME, buffer.toString());

			buffer.close();
		} catch (final Exception e) {
			Log.e(TAG, "Could not send join request!", e);
			return;
		}

		pGame.setState(GameState.JOINED);
	}

	/**
	 * server requests ACK
	 * @param pGame joined game
	 */
	public void receiveGameReady(GameInfo pGame)
	{
		pGame = announcedGames.find(pGame);

		if(pGame == null)
		{
			Log.e(TAG, "Game "+pGame+" was not announced!");
			return;
		}

		if(acknowledgeGameReady(pGame))
		{
			announcedGames.remove(pGame);

			pGame.setState(GameState.STARTED);
			runningGames.add(pGame);
		}
	}

	/**
	 * responds to server
	 * @param pGame joined game
	 */
	public boolean acknowledgeGameReady(final GameInfo pGame)
	{
		if(!pGame.getState().equals(GameState.JOINED))
		{
			Log.e(TAG, "Game "+pGame+" has wrong state!");
			return false;
		}

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("ACK");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());
			stream.println(app.getPlayerID().toString());

			app.getDTNSession().send(Server.GROUP_EID, DEFAULT_LIFETIME, buffer.toString());

			buffer.close();
		} catch (final Exception e) {
			Log.e(TAG, "Could not send ack!", e);
			return false;
		}

		return true;
	}

	/**
	 * start planning phase
	 * @param pGame running game
	 */
	public void startPlanningPhase(GameInfo pGame)
	{
		pGame = runningGames.find(pGame);

		if(pGame == null)
		{
			Log.e(TAG, "Game "+pGame+" is not running!");
			return;
		}

		if(!pGame.getState().equals(GameState.STARTED)
				&& !pGame.getState().equals(GameState.EXECUTION_PHASE))
		{
			Log.e(TAG, "Game "+pGame+" has wrong state!");
			return;
		}

		pGame.setState(GameState.PLANNING_PHASE);
		app.updateGameScene(pGame);
	}

	/**
	 * send decisions to server
	 * @param pGame running game
	 * @param decisions
	 */
	public void sendDecisions(final GameInfo pGame, final DecisionContainer decisions)
	{

	}

	/**
	 * receive outcome from server
	 */
	public void receiveOutcome(final GameInfo pGame, final OutcomeContainer outcome)
	{

	}

	/**
	 * quit game
	 * @param player
	 */
	public void abortGame(final GameInfo pGame)
	{

	}

	/**
	 * server has quit, inform clients about new server
	 * @param player
	 */
	public void receiveNewServer(final GameInfo pGame, final PlayerID pPlayer)
	{
		pGame.setServer(pPlayer);

		if(app.getPlayerID().equals(pPlayer))
		{
			app.getServer().startPlanningPhase(pGame);
		}
	}

	private final AppActivity app;

	public void handlePayload(final DataInputStream input) throws IOException {
		final String command = input.readLine();

		if(command.equals("ANNOUNCE"))
		{
			final PlayerID server = new PlayerID(input.readLine());
			final GameID game = new GameID(input.readLine());

			receiveGameInfo(new GameInfo(server, game));
		}
		else
		{
			Log.w(TAG, "Unknown command: "+command);
		}
	}
}
