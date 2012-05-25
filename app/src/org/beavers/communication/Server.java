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

public class Server {

	public static final GroupEndpoint GROUP_EID = new GroupEndpoint("dtn://beavergame.dtn/server");

	public final GameList hostedGames;

	public Server(final AppActivity pApp)
	{
		app = pApp;
		hostedGames = new GameList();
	}

	/**
	 * level select, inform possible clients
	 */
	public void initiateGame(final GameInfo pGame)
	{
		if(hostedGames.contains(pGame))
		{
			Log.e("Server", "Game "+pGame+" already exists!");
			return;
		}

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("ANNOUNCE");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());

			app.getDTNSession().send(Client.GROUP_EID, ANNOUNCEMENT_LIFETIME, buffer.toString());

			buffer.close();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pGame.setState(GameState.ANNOUNCED);
		hostedGames.add(pGame);
	}

	/**
	 * player tries to join game
	 * @param player
	 */
	public void addPlayer(final GameInfo pGame, final PlayerID pPlayer)
	{
		// TODO wait for all players, check if game still available

		gameReady(pGame);
	}

	/**
	 * server tries to start game
	 */
	public void gameReady(final GameInfo pGame)
	{
		if(!hostedGames.contains(pGame))
		{
			Log.e("Server", "Could not find game "+pGame+"!");
			return;
		}

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("STARTREQUEST");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());

			app.getDTNSession().send(Client.GROUP_EID, DEFAULT_LIFETIME, buffer.toString());

			buffer.close();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		hostedGames.find(pGame).setState(GameState.STARTED);
	}

	/**
	 * inform clients about planning phase
	 */
	public void startPlanningPhase(final GameInfo pGame)
	{
		if(!hostedGames.contains(pGame))
		{
			Log.e("Server", "Could not find game "+pGame+"!");
			return;
		}

		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("PLANNING");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());

			app.getDTNSession().send(Client.GROUP_EID, DEFAULT_LIFETIME, buffer.toString());

			buffer.close();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		hostedGames.find(pGame).setState(GameState.PLANNING_PHASE);
	}

	/**
	 * get decisions from player
	 * @param game
	 * @param player
	 * @param decisions
	 */
	public void receiveDecisions(final GameInfo pGame, final PlayerID pPlayer, final DecisionContainer pDecisions)
	{

	}

	/**
	 * distribute outcome to clients
	 */
	public void distributeOutcome(final GameInfo pGame, final OutcomeContainer outcome)
	{

	}

	/**
	 * player quits game
	 * @param player
	 */
	public void playerAbort(final GameInfo pGame, final PlayerID pPlayer)
	{
		if(pGame.getServer().equals(pPlayer))
		{
			announceNewServer(pGame, null);
		}
	}

	/**
	 * server has quit, inform clients about new server
	 * @param pGame old game info
	 * @param pServer new server
	 */
	public void announceNewServer(final GameInfo pGame, final PlayerID pServer)
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("NEWSERVER");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());
			stream.println(pServer.toString());

			app.getDTNSession().send(Client.GROUP_EID, DEFAULT_LIFETIME, buffer.toString());

			buffer.close();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handlePayload(final DataInputStream input) throws IOException {
		final String command = input.readLine();

		if(command.equals("JOIN"))
		{
			final PlayerID server = new PlayerID(input.readLine());
			final GameID game = new GameID(input.readLine());
			final PlayerID player = new PlayerID(input.readLine());

			if(server.equals(app.getPlayerID()))
			{
				addPlayer(new GameInfo(server, game), player);
			}
		}
	}

	/**
	 * @name lifetime constants
	 * @{
	 */
	private static final int DEFAULT_LIFETIME = 100;
	private static final int ANNOUNCEMENT_LIFETIME = DEFAULT_LIFETIME;
	/**
	 * @}
	 */

	private final AppActivity app;
}
