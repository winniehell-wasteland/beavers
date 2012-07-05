package org.beavers.communication;

import android.os.ParcelFileDescriptor;

import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.Player;
import org.beavers.gameplay.DecisionContainer;

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
	void abortGame(in GameInfo pGame);
	
	
	/**
	 * @return announced game with given position in container
	 */
	GameInfo getAnnouncedGame(in int pPosition);
	
	/**
	 * @return number of announced games
	 */
	int getAnnouncedGamesCount();
	
	/**
	 * @return running game with given position in container
	 */
	GameInfo getRunningGame(in int pPosition);
	
	/**
	 * @return number of running games
	 */
	int getRunningGamesCount();

	/**
	 * receive a DTN message
	 *
	 * @param pData file descriptor of payload file
	 * @return true if handled
	 */
	boolean handleData(in ParcelFileDescriptor pData);
	

	/**
	 * join a game
	 *
	 * @param pGame announced game
	 */
	void joinGame(in GameInfo pGame);
	
	/**
	 * send decisions to server
	 *
	 * @param pGame running game
	 * @param decisions
	 */
	void sendDecisions(in GameInfo pGame, in DecisionContainer pDecisions);
}