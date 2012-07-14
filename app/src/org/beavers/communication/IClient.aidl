package org.beavers.communication;

import android.os.ParcelFileDescriptor;

import org.beavers.gameplay.Game;
import org.beavers.gameplay.Player;

/**
 * client service interface
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 * @see {@link Client}
 */
interface IClient {

	/**
	 * quit game
	 *
	 * @param pGame game
	 */
	void abortGame(in Game pGame);
	
	/**
	 * @return announced games
	 */
	Game[] getAnnouncedGames();
	
	/**
	 * @return keys of running games
	 */
	Game[] getRunningGames();

	/**
	 * join a game
	 *
	 * @param pGame announced game
	 */
	void joinGame(in Game pGame);
	
	/**
	 * load the running games from file
	 */
	 void loadGameList();
	
	/**
	 * save the running games to file
	 */
	 void saveGameList();
	
	/**
	 * send decisions to server
	 *
	 * @param pGame running game
	 * @param decisions
	 */
	void sendDecisions(in Game pGame, in String pSoldiers);
}