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

package de.winniehell.battlebeavers.gameplay;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.winniehell.battlebeavers.storage.CustomGSON;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * class to uniquely identify a game and store its state
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class GameInfo {

	/**
	 * @name JSON tags
	 * @{
	 */
	/** tag for JSON files */
	public static final String JSON_TAG = "gameinfo";

	/** {@link #JSON_TAG} for map */
	public static final String JSON_TAG_MAP = "map";

	/** {@link #JSON_TAG} for team */
	public static final String JSON_TAG_TEAM = "team";
	/**
	 * @}
	 */

	/**
	 * default constructor
	 * @param pServer server of the game
	 * @param pGame unique game ID on server
	 */
	public GameInfo(final String pMapName, final int pTeam) {
		map = pMapName;
		team = pTeam;

		state = GameState.UNKNOWN;
	}

	/** @return map name */
	public String getMapName() {
		return map;
	}

	/** @return current game state */
	public GameState getState() {
		return state;
	}

	/** @return team number */
	public int getTeam()
	{
		return team;
	}

	public void saveToFile(final Context pContext, final Game pGame)
	            throws IOException {
		final JsonWriter writer =
				CustomGSON.getWriter(pContext, getFileName(pContext, pGame));

		CustomGSON.getInstance().toJson(this, getClass(), writer);

		writer.close();

		Log.d(getClass().getSimpleName(), "Stored in "+getFileName(pContext, pGame));
	}

	/**
	 * changes the state
	 * @param pState new state
	 */
	public void setState(final GameState pState) {
		state = pState;
	}

	public static GameInfo fromFile(final Context pContext, final Game pGame)
	                       throws FileNotFoundException {
		Log.d(GameInfo.class.getSimpleName(), "Reading from "+getFileName(pContext, pGame));

		final JsonReader reader =
			CustomGSON.getReader(pContext, getFileName(pContext, pGame));

		return CustomGSON.getInstance().fromJson(reader, GameInfo.class);
	}


	private static String getFileName(final Context pContext,
	                                  final Game pGame) {
		return pGame.getDirectory(pContext).toString() + "/info.json";
	}

	/** map name */
    @SerializedName(JSON_TAG_MAP)
    private final String map;

	/** state of the game */
    @SerializedName(GameState.JSON_TAG)
	private GameState state;

    /** team number */
    @SerializedName(JSON_TAG_TEAM)
    private final int team;
}
