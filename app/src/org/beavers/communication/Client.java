package org.beavers.communication;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.beavers.AppActivity;
import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.PlayerID;

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
	
	public ArrayList<GameInfo> announcedGames = new ArrayList<GameInfo>();

	public Client(final AppActivity pApp)
	{
		app = pApp;
	}
	
	/**
	 * server has announced new game
	 * @param game
	 */
	public void receiveGameInfo(GameInfo game)
	{
		announcedGames.add(game);
	}
	
	/**
	 * joins a game
	 * @param game
	 */
	public void joinGame(GameInfo pGame)
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * responds to server
	 * @param game
	 */
	public void acknowledgeGameReady(GameInfo game)
	{
		
	}
	
	/**
	 * start planning phase
	 */
	public void startPlanningPhase(GameInfo game)
	{
		
		
	}
	
	/**
	 * send decisions to server
	 * @param game
	 * @param decisions
	 */
	public void sendDecisions(GameInfo game, DecisionContainer decisions)
	{
		
	}
	
	/**
	 * receive outcome from server
	 */
	public void receiveOutcome(GameInfo game, OutcomeContainer outcome)
	{
		
	}
	
	/**
	 * quit game
	 * @param player
	 */
	public void abortGame(GameInfo game)
	{
		
	}
	
	/**
	 * server has quit, inform clients about new server
	 * @param player
	 */
	public void receiveNewServer(GameInfo pGame, PlayerID pPlayer)
	{
		pGame.setServer(pPlayer);

		if(app.getPlayerID().equals(pPlayer))
		{
			app.getServer().startPlanningPhase(pGame);
		}
	}
	
	private final AppActivity app;

	public void handlePayload(DataInputStream input) throws IOException {
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
