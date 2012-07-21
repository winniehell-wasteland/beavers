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

package de.winniehell.battlebeavers;

import java.util.Random;
import java.util.UUID;

import de.winniehell.battlebeavers.gameplay.Player;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	public Settings(final Context pContext) {
		settings = pContext.getSharedPreferences("preferences",
		                                         Context.MODE_PRIVATE);
	}

	public int getDTNLifetime() {
		return DTN_LIFETIME_DEFAULT;
	}

	public String getDefaultMapName()
	{
		return MAP_NAME_DEFAULT;
	}

	public int getMaxPlayers() {
		return MAX_PLAYERS_DEFAULT;
	}

	public Player getPlayer() {
		final String id =
			settings.getString(PLAYER_ID_KEY, PLAYER_ID_DEFAULT);

		// store default
		if(id == PLAYER_ID_DEFAULT) {
			settings.edit().putString(PLAYER_ID_KEY, id).commit();
		}

		final String name =
			settings.getString(PLAYER_NAME_KEY, PLAYER_NAME_DEFAULT);

		// store default
		if(name == PLAYER_NAME_DEFAULT) {
			settings.edit().putString(PLAYER_NAME_KEY, name).commit();
		}

		return new Player(UUID.fromString(id), name);
	}

	/**
	 * @name keys
	 * @{
	 */
	private static String PLAYER_ID_KEY = "player_id";
	private static String PLAYER_NAME_KEY = "player_name";
	/**
	 * @}
	 */

	/**
	 * @name defaults
	 * @{
	 */
	private static int DTN_LIFETIME_DEFAULT = 300;
	private static String MAP_NAME_DEFAULT = "test";
	private static int MAX_PLAYERS_DEFAULT = 2;
	private static String PLAYER_ID_DEFAULT = UUID.randomUUID().toString();
	private static String PLAYER_NAME_DEFAULT =
		"player"+(new Random()).nextInt(1000);
	/**
	 * @}
	 */

	private final SharedPreferences settings;
}
