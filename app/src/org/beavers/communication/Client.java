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

	public Client(final AppActivity pApp)
	{
		app = pApp;

		announcedGames = new GameList();
		runningGames = new GameList();

		announcedGames.add(new GameInfo(new PlayerID("foo"), new GameID("bar")));
		announcedGames.add(new GameInfo(new PlayerID("test"), new GameID("game")));
		announcedGames.add(new GameInfo(new PlayerID("123"), new GameID("456")));

		runningGames.add(new GameInfo(new PlayerID("server1"), new GameID("game1")));
		runningGames.add(new GameInfo(new PlayerID("server2"), new GameID("game2")));
		runningGames.add(new GameInfo(new PlayerID("server3"), new GameID("game3")));
		runningGames.add(new GameInfo(new PlayerID("server4"), new GameID("game4")));
	}

	/**
	 * server has announced new game
	 * @param game
	 */
	public void receiveGameInfo(final GameInfo pGame)
	{
		if(announcedGames.contains(pGame))
		{
			Log.e("Client", "Game "+pGame+" was already announced!");
			return;
		}

		pGame.setState(GameState.ANNOUNCED);
		announcedGames.add(pGame);
	}

	/**
	 * joins a game
	 * @param game
	 */
	public void joinGame(final GameInfo pGame)
	{
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * responds to server
	 * @param game
	 */
	public void acknowledgeGameReady(final GameInfo pGame)
	{

	}

	/**
	 * start planning phase
	 */
	public void startPlanningPhase(final GameInfo pGame)
	{


	}

	/**
	 * send decisions to server
	 * @param game
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
			System.out.println("Got: '"+command+"'");
		}
	}
}
