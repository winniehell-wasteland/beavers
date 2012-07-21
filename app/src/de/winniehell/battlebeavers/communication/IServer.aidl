package de.winniehell.battlebeavers.communication;

import android.os.ParcelFileDescriptor;

import de.winniehell.battlebeavers.gameplay.Game;
import de.winniehell.battlebeavers.gameplay.Player;
import de.winniehell.battlebeavers.gameplay.OutcomeContainer;

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
	 * remove all games from list
	 */
	 void deleteGames();
	
	/**
	 * distribute outcome to clients
	 */
	void distributeOutcome(in Game pGame);
	
	/**
	 * level select, inform possible clients
	 *
	 * @param pGame new game
	 */
	void initiateGame(in Game pGame);
	
	/**
	 * load players of running games from file
	 */
	 void loadPlayerMap();
	
	/**
	 * save players of running games to file
	 */
	 void savePlayerMap();
}