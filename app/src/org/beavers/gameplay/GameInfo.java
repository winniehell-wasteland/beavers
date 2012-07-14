package org.beavers.gameplay;

import org.beavers.storage.CustomGSON;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * class to uniquely identify a game and store its state
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class GameInfo {

	/** tag for JSON files */
	public static final String JSON_TAG = "gameinfo";

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

	/** @return true if game is in given state */
	public boolean isInState(final GameState pState) {
		return state.equals(pState);
	}

	public void saveToFile(final Context pContext, final Game pGame) {
		final JsonWriter writer =
				CustomGSON.getWriter(pContext, getFileName(pGame));

		CustomGSON.getInstance().toJson(this, getClass(), writer);
	}

	/**
	 * changes the state
	 * @param pState new state
	 */
	public void setState(final GameState pState) {
		state = pState;
	}

	public static GameInfo fromFile(final Context pContext, final Game pGame) {
		final JsonReader reader =
			CustomGSON.getReader(pContext, getFileName(pGame));

		return CustomGSON.getInstance().fromJson(reader, GameInfo.class);
	}


	private static String getFileName(final Game pGame) {
		return pGame.toString() + "/info.json";
	}


	/** state of the game */
    @SerializedName(GameState.JSON_TAG)
	private GameState state;

    private final int team;
    private final String map;
}
