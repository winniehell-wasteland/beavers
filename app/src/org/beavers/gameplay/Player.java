package org.beavers.gameplay;

import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class to uniquely identify a player (i.e. a playing device)
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class Player extends UniqueID {
	public Player(final UUID pID, final String pName) {
		super(pID, pName);
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof Player)
		{
			return super.equals(other);
		}
		else
		{
			return false;
		}
	}

	public static final Parcelable.Creator<Player> CREATOR =
		new Parcelable.Creator<Player>() {
			@Override
			public Player createFromParcel(final Parcel parcel) {
				return new Player(parcel);
			}

			@Override
			public Player[] newArray(final int size) {
				return new Player[size];
			}
	};

	private Player(final Parcel pParcel)
	{
		super(pParcel);
	}
}
