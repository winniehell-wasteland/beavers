package org.beavers.gameplay;

import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class to uniquely identify a game within a server
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class Game extends UniqueID {
	/** tag for JSON files */
	public static final String JSON_TAG = "game";

	public Game(final UUID pID, final String pName) {
		super(pID, pName);
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof Game)
		{
			return super.equals(other);
		}
		else
		{
			return false;
		}
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

	private Game(final Parcel pParcel)
	{
		super(pParcel);
	}
}
