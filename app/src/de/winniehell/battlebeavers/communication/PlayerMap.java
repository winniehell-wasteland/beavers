/*
	(c) winniehell (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers.communication;

import java.io.Serializable;
import java.util.HashMap;

import de.winniehell.battlebeavers.gameplay.Game;

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
