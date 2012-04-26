package org.beavers.communication;

import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.Player;

public class Client {

	public Client(Player player)
	{
		
	}
	
	/**
	 * server has announced new game
	 * @param game
	 */
	public void receiveGameInfo(Game game)
	{
		
	}
	
	/**
	 * joins a game
	 * @param game
	 */
	public void joinGame(Game game)
	{
		
	}
	
	/**
	 * responds to server
	 * @param game
	 */
	public void acknowledgeGameReady(Game game)
	{
		
	}
	
	/**
	 * start planning phase
	 */
	public void startPlanningPhase(Game game)
	{
		
		
	}
	
	/**
	 * send decisions to server
	 * @param game
	 * @param decisions
	 */
	public void sendDecisions(Game game, DecisionContainer decisions)
	{
		
	}
	
	/**
	 * receive outcome from server
	 */
	public void receiveOutcome(Game game, OutcomeContainer outcome)
	{
		
	}
	
	/**
	 * quit game
	 * @param player
	 */
	public void abortGame(Game game)
	{
		
	}
	/**
	 * server has quit, inform clients about new server
	 * @param player
	 */
	public void receiveNewServer(Game game, Player player)
	{
		game.setServer(player);
		// TODO: ...
	}

	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}
}
