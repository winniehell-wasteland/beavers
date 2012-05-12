package org.beavers.communication;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.beavers.AppActivity;
import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.PlayerID;

import de.tubs.ibr.dtn.api.GroupEndpoint;

public class Server {

	public static final GroupEndpoint GROUP_EID = new GroupEndpoint("dtn://beavergame.dtn/server");

	/**
	 * @name lifetimes
	 * @{
	 */
	private static final int DEFAULT_LIFETIME = 100;
	private static final int ANNOUNCEMENT_LIFETIME = DEFAULT_LIFETIME;
	/**
	 * @}
	 */
	
	public Server(final AppActivity pApp)
	{
		app = pApp;
	}
	
	/**
	 * level select, inform possible clients
	 */
	public void initiateGame(GameInfo pGame)
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);
		
		try {
			stream.println("ANNOUNCE");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());
		
			app.getDTNSession().send(Client.GROUP_EID, ANNOUNCEMENT_LIFETIME, buffer.toString());
		
			buffer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * player tries to join game
	 * @param player
	 */
	public void addPlayer(GameInfo pGame, PlayerID pPlayer)
	{
		// TODO wait for all players, check if game still available
		
		gameReady(pGame);
	}
	
	/**
	 * server tries to start game
	 */
	public void gameReady(GameInfo pGame)
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("STARTREQUEST");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());
		
			app.getDTNSession().send(Client.GROUP_EID, DEFAULT_LIFETIME, buffer.toString());
		
			buffer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * inform clients about planning phase
	 */
	public void startPlanningPhase(GameInfo pGame)
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		final PrintStream stream = new PrintStream(buffer);

		try {
			stream.println("PLANNING");
			stream.println(pGame.getServer().toString());
			stream.println(pGame.getID().toString());
		
			app.getDTNSession().send(Client.GROUP_EID, DEFAULT_LIFETIME, buffer.toString());
		
			buffer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * get decisions from player
	 * @param game
	 * @param player
	 * @param decisions
	 */
	public void receiveDecisions(GameInfo pGame, PlayerID pPlayer, DecisionContainer pDecisions)
	{
		
	}
	
	/**
	 * distribute outcome to clients
	 */
	public void distributeOutcome(GameInfo pGame, OutcomeContainer outcome)
	{
		
	}
	
	/**
	 * player quits game
	 * @param player
	 */
	public void playerAbort(GameInfo pGame, PlayerID pPlayer)
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
	public void announceNewServer(GameInfo pGame, PlayerID pServer)
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handlePayload(DataInputStream input) throws IOException {		
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
	
	private final AppActivity app;
}
