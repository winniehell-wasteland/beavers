package org.beavers.communication;

import java.util.HashMap;

import org.beavers.gameplay.GameInfo;

/**
 * map for players of a all games
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
@SuppressWarnings("serial")
public class PlayerMap extends HashMap<GameInfo, PlayerSet> {
	public PlayerMap() {
		super();
	}

	@Override
	public PlayerSet get(final Object key) {
		if(key instanceof GameInfo)
		{
			if(!containsKey(key))
			{
				put((GameInfo) key, new PlayerSet());
			}

			return super.get(key);
		}
		else
		{
			return null;
		}
	}
}
