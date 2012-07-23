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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.winniehell.battlebeavers.storage.CustomGSON;
import de.winniehell.battlebeavers.storage.Outcome;
import de.winniehell.battlebeavers.storage.SoldierList;

/**
 * class to uniquely identify a game
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class Game extends UniqueID {
	/**
	 * @name public constants
	 * @{
	 */
	/** tag for JSON files */
	public static final String JSON_TAG = "game";

	/** {@link #JSON_TAG} for {@link Collection}s */
	public static final String JSON_TAG_COLLECTION = "games";

	/** name for parcel in intent extras */
	public static final String PARCEL_NAME = Game.class.getName();
	/**
	 * @}
	 */

	/**
	 * @name intents
	 * @{
	 */
	public static final String STATE_CHANGED_INTENT =
		Game.class.getName() + ".STATE_CHANGED";
	/**
	 * @}
	 */

	/**
	 * default constructor
	 *
	 * @param pServer server of the game
	 * @param pID game id
	 * @param pName game name
	 */
	public Game(final Player pServer, final UUID pID, final String pName) {
		super(pID, pName);
		server = pServer;
	}

	/** delete all game directories */
	public static void deleteAll(final Context pContext) {
		final File basedir = new File(getBasePath(pContext));

		if(!basedir.exists()) {
			return;
		}

		for(final File serverdir : basedir.listFiles()) {
			for(final File gamedir : serverdir.listFiles()) {
				for(final File file : gamedir.listFiles()) {
					file.delete();
				}

				gamedir.delete();
			}

			serverdir.delete();
		}
	}

	/** delete decisions file */
	public void deleteDecisions(final Context pContext, final int pTeam) {
		if(!hasDecisions(pContext, pTeam)) {
			return ;
		}

		new File(getDecisionsFile(pContext, pTeam)).delete();
	}

	/** delete outcome file */
	public void deleteOutcome(final Context pContext) {
		if(!hasOutcome(pContext)) {
			return ;
		}

		new File(getOutcomeFile(pContext)).delete();
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof Game)
		{
			return server.equals(((Game)other).server) &&  super.equals(other);
		}
		else
		{
			return false;
		}
	}

	/** load decisions from file */
	public SoldierList getDecisions(final Context pContext, final int pTeam)
	                   throws IOException {
		if(!hasDecisions(pContext, pTeam)) {
			return null;
		}

		final JsonReader reader =
			CustomGSON.getReader(pContext, getDecisionsFile(pContext, pTeam));

		try {
			return CustomGSON.getInstance().fromJson(reader, SoldierList.class);
		} finally {
			reader.close();
		}
	}

	/** load outcome from file */
	public Outcome getOutcome(final Context pContext) throws IOException {
		if(!hasOutcome(pContext)) {
			return null;
		}

		final JsonReader reader =
			CustomGSON.getReader(pContext, getOutcomeFile(pContext));

		try {
			return CustomGSON.getInstance().fromJson(reader, Outcome.class);
		} finally {
			reader.close();
		}
	}

	/** @return game directory */
	public String getDirectory(final Context pContext) {
		return getBasePath(pContext) + this;
	}

	/** @return server of the game */
	public Player getServer() {
		return server;
	}

	/** @return game state or {@link GameState#UNKNOWN} if game info file does
	 *          not exist
	 */
	public GameState getState(final Context pContext) {
		try {
			final GameInfo info = GameInfo.fromFile(pContext, this);
			return info.getState();
		} catch(final NullPointerException e) {
			Log.e(Game.class.getName(), e.toString());
			return GameState.UNKNOWN;
		} catch(final FileNotFoundException e) {
			Log.e(Game.class.getName(), e.getMessage());
			return GameState.UNKNOWN;
		}
	}

	/** @return true if there is a decisions file for this team */
	public boolean hasDecisions(final Context pContext, final int pTeam) {
		return new File(getDecisionsFile(pContext, pTeam)).exists();
	}

	/** @return true if there is an outcome file for this game */
	public boolean hasOutcome(final Context pContext) {
		return new File(getOutcomeFile(pContext)).exists();
	}

	/** @return true if game is in given state(s) */
	public boolean isInState(final Context pContext,
	                         final GameState... pStates) {

		final GameState myState = getState(pContext);

		for(final GameState state : pStates) {
			if(myState.equals(state)) {
				return true;
			}
		}

		return false;
	}

	/** @return true if given player is server of the game */
	public boolean isServer(final Player pPlayer) {
		return server.equals(pPlayer);
	}

	/** save decisions to file */
	public void saveDecisions(final Context pContext,
	                          final int pTeam, final SoldierList pDecisions)
	            throws IOException {
		writeDecisions(pContext, pTeam,
		               CustomGSON.getInstance().toJsonTree(pDecisions));
	}

	/** save outcome to file */
	public void saveOutcome(final Context pContext, final Outcome pOutcome)
	            throws IOException {
		writeOutcome(pContext,
		             CustomGSON.getInstance().toJsonTree(pOutcome));
	}

	/**
	 * set game state
	 *
	 * @throws IOException if game info could not be written
	 */
	public void setState(final Context pContext, final GameState newState)
	            throws IOException {
		final GameInfo info = GameInfo.fromFile(pContext, this);
		info.setState(newState);
		info.saveToFile(pContext, this);
	}

	@Override
	public String toString() {
		return server.toString()+"/"+super.toString();
	}

	/** write decisions JSON to file */
	public void writeDecisions(final Context pContext,
	                           final int pTeam, final JsonElement pDecisions)
	            throws IOException {
		final JsonWriter writer =
			CustomGSON.getWriter(pContext, getDecisionsFile(pContext, pTeam));

		CustomGSON.getInstance().toJson(pDecisions, writer);

		writer.close();
	}
	/** write outcome JSON to file */
	public void writeOutcome(final Context pContext, final JsonElement pOutcome)
	            throws IOException {
		final JsonWriter writer =
			CustomGSON.getWriter(pContext, getOutcomeFile(pContext));

		CustomGSON.getInstance().toJson(pOutcome, writer);

		writer.close();
	}

	@Override
	public void writeToParcel(final Parcel pOut, final int pFlags) {
		super.writeToParcel(pOut, pFlags);
    	pOut.writeParcelable(server, pFlags);
	}

	public static final Parcelable.Creator<Game> CREATOR =
		new Parcelable.Creator<Game>() {
			@Override
			public Game createFromParcel(final Parcel parcel) {
				return new Game(parcel);
			}

			@Override
			public Game[] newArray(final int size) {
				return new Game[size];
			}
	};

	/** server of the game */
	private final Player server;

	private Game(final Parcel pParcel)
	{
		super(pParcel);
    	server = pParcel.readParcelable(Player.class.getClassLoader());
	}

	private static String getBasePath(final Context pContext) {
		return pContext.getFilesDir().getAbsolutePath() + "/games/";
	}

	/** @return name of decisions file for given team */
	private String getDecisionsFile(final Context pContext, final int pTeam) {
		return getDirectory(pContext) + "/decisions-" + pTeam + ".json";
	}

	/** @return name of outcome file for this game */
	private String getOutcomeFile(final Context pContext) {
		return getDirectory(pContext) + "/outcome.json";
	}
}
