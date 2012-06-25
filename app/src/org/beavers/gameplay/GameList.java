package org.beavers.gameplay;

import java.util.Hashtable;

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
		container = new Hashtable<String, GameInfo>();
	}

	/**
	 * insert new game into list
	 * @param pGame new game
	 */
	public synchronized GameInfo add(final GameInfo pGame)
	{
		container.put(pGame.toString(), pGame);
		return container.get(pGame.toString());
	}

	/**
	 * @param pGame game to find
	 * @return true if game is in list
	 */
	public synchronized boolean contains(final GameInfo pGame)
	{
		return container.containsKey(pGame.toString());
	}

	/**
	 * find a game in the list
	 *
	 * @param pGame game to find
	 * @return game in list (or null)
	 */
	public synchronized GameInfo find(final GameInfo pGame)
	{
		return container.get(pGame.toString());
	}

	/**
	 * @param pIndex key index
	 * @return game with given index
	 */
	public synchronized GameInfo get(final int pIndex)
	{
		if(pIndex < container.size())
		{
			return container.get(container.keySet().toArray()[pIndex]);
		}
		else
		{
			return null;
		}
	}

	/**
	 * delete a game from the list
	 * @param pGame game to delete
	 */
	public synchronized void remove(final GameInfo pGame) {
		container.remove(pGame.toString());
	}

	/**
	 * @return the number of elements in the list
	 */
	public synchronized int size()
	{
		return container.size();
	}

	/** underlying container */
	private final Hashtable<String, GameInfo> container;
}
