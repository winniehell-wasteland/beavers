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
	 * @return announced game with given key
	 */
	Game getAnnouncedGame(in String pKey);
	
	/**
	 * @return keys of announced games
	 */
	String[] getAnnouncedGames();
	
	/**
	 * @return number of announced games
	 */
	int getAnnouncedGamesCount();
	
	/**
	 * @return running game with given key
	 */
	Game getRunningGame(in String pKey);
	
	/**
	 * @return keys of running games
	 */
	String[] getRunningGames();
	
	/**
	 * @return number of running games
	 */
	int getRunningGamesCount();

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