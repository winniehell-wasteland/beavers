package org.beavers.gameplay;

import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class to uniquely identify a player (i.e. a playing device)
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class Player implements Parcelable {
	public Player(final UUID pID, final String pName) {
		id = pID;
		name = pName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof Player)
		{
			return id.equals(((Player)other).id);
		}
		else
		{
			return false;
		}
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public void writeToParcel(final Parcel pOut, final int pFlags) {
		pOut.writeString(id.toString());
		pOut.writeString(name);
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

	private final UUID id;
	private final String name;

	private Player(final Parcel pParcel)
	{
		id = UUID.fromString(pParcel.readString());
		name = pParcel.readString();
	}
}
