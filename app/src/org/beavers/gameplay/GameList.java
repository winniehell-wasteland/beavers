package org.beavers.gameplay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * list for storing game information
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class GameList {

	/**
	 * default constructor
	 */
	public GameList()
	{
		container = Collections.synchronizedMap(
			new HashMap<String, GameInfo>()
		);
	}

	/**
	 * insert new game into list
	 * @param pGame new game
	 */
	public GameInfo add(final GameInfo pGame)
	{
		container.put(pGame.toString(), pGame);
		return container.get(pGame.toString());
	}

	/**
	 * @param pGame game to find
	 * @return true if game is in list
	 */
	public boolean contains(final GameInfo pGame)
	{
		return container.containsKey(pGame.toString());
	}

	/**
	 * find a game in the list
	 *
	 * @param pKey game to find
	 * @return game in list (or null)
	 */
	public GameInfo find(final GameInfo pKey)
	{
		return container.get(pKey.toString());
	}

	public GameInfo get(final String pKey) {
		return container.get(pKey);
	}


	public String[] getKeys()
	{
		return container.keySet().toArray(new String[0]);
	}

	/**
	 * delete a game from the list
	 * @param pGame game to delete
	 */
	public void remove(final GameInfo pGame) {
		container.remove(pGame.toString());
	}

	/**
	 * @return the number of elements in the list
	 */
	public int size()
	{
		return container.size();
	}

	/** underlying container */
	private final Map<String, GameInfo> container;
}
