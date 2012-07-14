package org.beavers.communication;

import android.os.ParcelFileDescriptor;

import org.beavers.gameplay.Game;
import org.beavers.gameplay.Player;
import org.beavers.gameplay.OutcomeContainer;

/**
 * server service interface
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 * @see {@link Server}
 */
interface IServer {
	
	/**
	 * player tries to join game
	 *
	 * @param pGame game
	 * @param pPlayer player
	 *
	 */
	void addPlayer(in Game pGame, in Player pPlayer);
	
	/**
	 * distribute outcome to clients
	 */
	void distributeOutcome(in Game pGame, in OutcomeContainer pOutcome);
	
	/**
	 * level select, inform possible clients
	 *
	 * @param pGame new game
	 */
	void initiateGame(in Game pGame);
	
	/**
	 * load the running games from file
	 */
	 void loadGameList();
	
	/**
	 * save the running games to file
	 */
	 void saveGameList();
}