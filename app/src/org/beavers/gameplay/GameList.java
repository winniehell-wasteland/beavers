package org.beavers.gameplay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * list for storing game information
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class GameList  implements Iterable<Game> {

	/**
	 * default constructor
	 */
	public GameList()
	{
		container = Collections.synchronizedMap(
			new HashMap<String, Game>()
		);
	}

	/**
	 * insert new game into list
	 * @param pGame new game
	 */
	public Game add(final Game pGame)
	{
		container.put(pGame.toString(), pGame);
		return container.get(pGame.toString());
	}

	public void clear() {
		container.clear();
	}

	/**
	 * @param pGame game to find
	 * @return true if game is in list
	 */
	public boolean contains(final Game pGame)
	{
		return container.containsKey(pGame.toString());
	}

	/**
	 * find a game in the list
	 *
	 * @param pKey game to find
	 * @return game in list (or null)
	 */
	public Game find(final Game pKey)
	{
		return container.get(pKey.toString());
	}

	public Game get(final String pKey) {
		return container.get(pKey);
	}


	public String[] getKeys()
	{
		return container.keySet().toArray(new String[0]);
	}

	@Override
	public Iterator<Game> iterator() {
		return container.values().iterator();
	}

	/**
	 * delete a game from the list
	 * @param pGame game to delete
	 */
	public void remove(final Game pGame) {
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
	private final Map<String, Game> container;
}
