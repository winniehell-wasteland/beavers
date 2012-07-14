package org.beavers.communication;

import java.util.HashMap;
import java.util.HashSet;

import org.beavers.gameplay.Game;
import org.beavers.gameplay.Player;

/**
 * map for players of a all games
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
@SuppressWarnings("serial")
public class PlayerMap extends HashMap<Game, HashSet<Player>> {
	public PlayerMap() {
		super();
	}

	@Override
	public HashSet<Player> get(final Object key) {
		if(key instanceof Game)
		{
			if(!containsKey(key))
			{
				put((Game) key, new HashSet<Player>());
			}

			return super.get(key);
		}
		else
		{
			return null;
		}
	}
}
