package org.beavers.communication;

import java.io.Serializable;
import java.util.HashMap;

import org.beavers.gameplay.Game;

/**
 * map for players of a all games
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class PlayerMap extends HashMap<Game, PlayerSet> {
	/** @see {@link Serializable} */
	private static final long serialVersionUID = 332279770783868241L;

	public PlayerMap() {
		super();
	}

	@Override
	public PlayerSet get(final Object key) {
		if(key instanceof Game)
		{
			if(!containsKey(key))
			{
				put((Game) key, new PlayerSet());
			}

			return super.get(key);
		}
		else
		{
			return null;
		}
	}
}
