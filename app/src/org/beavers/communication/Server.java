package org.beavers.communication;

import org.beavers.gameplay.DecisionContainer;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.OutcomeContainer;
import org.beavers.gameplay.Player;

public class Server {
	
	public Server(Player player)
	{
		
	}
	
	/**
	 * level select, inform possible clients
	 */
	public void initiateGame(Game game)
	{
	}
	
	/**
	 * player joins game
	 * @param player
	 */
	public void addPlayer(Game game, Player player)
	{
		
	}
	
	/**
	 * server tries to start game
	 */
	public void gameReady(Game game)
	{
		
	}
	
	/**
	 * inform clients about planning phase
	 */
	public void startPlanningPhase(Game game)
	{
		
		
	}
	
	/**
	 * get decisions from player
	 * @param game
	 * @param player
	 * @param decisions
	 */
	public void receiveDecisions(Game game, Player player, DecisionContainer decisions)
	{
		
	}
	
	/**
	 * distribute outcome to clients
	 */
	public void distributeOutcome(Game game, OutcomeContainer outcome)
	{
		
	}
	
	/**
	 * player quits game
	 * @param player
	 */
	public void playerAbort(Game game, Player player)
	{
		if(player.isServer(game))
		{
			announceNewServer(game, null);
		}
	}
	
	/**
	 * server has quit, inform clients about new server
	 * @param player
	 */
	public void announceNewServer(Game game, Player player)
	{
		
	}

	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}
}
