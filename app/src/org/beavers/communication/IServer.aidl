package org.beavers.communication;

import android.os.ParcelFileDescriptor;

import org.beavers.gameplay.GameInfo;
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
	void addPlayer(in GameInfo pGame, in Player pPlayer);
	
	/**
	 * distribute outcome to clients
	 */
	void distributeOutcome(in GameInfo pGame, in OutcomeContainer pOutcome);
	
	/**
	 * level select, inform possible clients
	 *
	 * @param pGame new game
	 */
	void initiateGame(in GameInfo pGame);
	
	/**
	 * load the running games from file
	 */
	 void loadGameList();
	
	/**
	 * save the running games to file
	 */
	 void saveGameList();
}