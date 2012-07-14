package org.beavers;

import java.util.Random;
import java.util.UUID;

import org.beavers.gameplay.Player;

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
		final String name =
			settings.getString(PLAYER_NAME_KEY, PLAYER_NAME_DEFAULT);

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
	private static int DTN_LIFETIME_DEFAULT = 100;
	private static String MAP_NAME_DEFAULT = "map";
	private static int MAX_PLAYERS_DEFAULT = 2;
	private static String PLAYER_ID_DEFAULT = UUID.randomUUID().toString();
	private static String PLAYER_NAME_DEFAULT =
		"player"+(new Random()).nextInt(1000);
	/**
	 * @}
	 */

	private final SharedPreferences settings;
}
