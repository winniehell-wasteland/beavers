package org.beavers.communication;

import java.util.HashSet;

import org.beavers.gameplay.PlayerID;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * set for players of a game
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
@SuppressWarnings("serial")
public class PlayerSet extends HashSet<PlayerID> {
	public PlayerSet()
	{
		super();
	}

	public Object toJSON()
	{
		final JSONArray array = new JSONArray();

		for(final PlayerID player : this)
		{
			array.put(player.toJSON());
		}

		return array;
	}

	public static PlayerSet fromJSON(final Object pJSON)
	{
		try {
			if(pJSON instanceof JSONArray)
			{
				final JSONArray array = (JSONArray) pJSON;
				final PlayerSet set = new PlayerSet();

				for(int i = 0; i < array.length(); ++i)
				{
					set.add((PlayerID) array.get(i));
				}
			}
		} catch (final JSONException e) {

		}

		return null;
	}
}
