package org.beavers.gameplay;

import java.io.File;
import java.util.UUID;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

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

	/** name for parcel in intent extras */
	public static final String PARCEL_NAME = Game.class.getName();
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

	/** @return server of the game */
	public Player getServer() {
		return server;
	}

	/** @return true if given player is server of the game */
	public boolean isServer(final Player pPlayer) {
		return server.equals(pPlayer);
	}

	@Override
	public String toString() {
		return server.toString()+"/"+super.toString();
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

	public File getDirectory(final Context pContext) {
		return new File(pContext.getFilesDir().getAbsolutePath()
		                + "/" + toString());
	}
}
