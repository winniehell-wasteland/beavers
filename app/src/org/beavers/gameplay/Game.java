package org.beavers.gameplay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.beavers.storage.CustomGSON;
import org.beavers.storage.SoldierList;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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

	/** @return game directory */
	public String getDirectory(final Context pContext) {
		return pContext.getFilesDir().getAbsolutePath() + "/" + this;
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
		} catch (final FileNotFoundException e) {
			return GameState.UNKNOWN;
		}
	}

	/** @return true if there is a decisions file for this team */
	public boolean hasDecisions(final Context pContext, final int pTeam) {
		return new File(getDecisionsFile(pContext, pTeam)).exists();
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

	public void saveDecisions(final Context pContext,
	                          final int pTeam, final SoldierList pDecisions)
	            throws IOException {
		writeDecisions(pContext, pTeam,
		               CustomGSON.getInstance().toJsonTree(pDecisions));
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

	public void writeDecisions(final Context pContext,
	                           final int pTeam, final JsonElement pDecisions)
	            throws IOException {
		final JsonWriter writer =
			CustomGSON.getWriter(pContext, getDecisionsFile(pContext, pTeam));

		CustomGSON.getInstance().toJson(pDecisions, writer);

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

	private String getDecisionsFile(final Context pContext, final int pTeam) {
		return getDirectory(pContext) + "/decisions-" + pTeam + ".json";
	}
}
