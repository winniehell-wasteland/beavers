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
	 * become server of an existing game
	 *
	 * @param pGame game
	 */
	void acquireGame(in GameInfo pGame);
	
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
	 * receive a DTN message
	 *
	 * @param pData file descriptor of payload file
	 * @return true if handled
	 */
	boolean handleData(in ParcelFileDescriptor pData);
	
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