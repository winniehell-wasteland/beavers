package org.beavers.communication;

import java.io.DataInputStream;
import java.io.IOException;

import org.beavers.AppActivity;
import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.PlayerID;

import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;

public class Client {

	public Client(final AppActivity pContext)
	{
		context = pContext;
		
		dtnClient = new CustomDTNClient(context);
        
        dtnClient.setDataHandler(dtnDataHandler);
        
        try {
			dtnClient.initialize(context, new Registration("client"));
		} catch (ServiceNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		// unregister at the daemon
		dtnClient.unregister();

		dtnDataHandler.stop();
		
		// destroy DTN client
		dtnClient.terminate();
		dtnClient = null;	
	}
	
	/**
	 * server has announced new game
	 * @param game
	 */
	public void receiveGameInfo(GameInfo game)
	{
		
	}
	
	/**
	 * joins a game
	 * @param game
	 */
	public void joinGame(GameInfo game)
	{
		
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
	public void receiveNewServer(GameInfo game, PlayerID player)
	{
		game.setServer(player);
		// TODO: ...
	}
	
	private final AppActivity context;
	private CustomDTNClient dtnClient;
	
	private CustomDTNDataHandler dtnDataHandler = new CustomDTNDataHandler(dtnClient) {
		@Override
		void receiveData(DataInputStream reader) throws IOException {
			final String command = reader.readLine();

			if(command == "ANNOUNCE")
			{
				final PlayerID server = new PlayerID(reader.readLine());
				final GameID game = new GameID(reader.readLine());
				
				receiveGameInfo(new GameInfo(server, game));
			}
			else
			{
				//System.out.println("Got: " + );
			}
		}
		
	};
}
