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
	/** tag for JSON files */
	public static final String JSON_TAG = "player";

	/** {@link #JSON_TAG} for a collection */
	public static final String JSON_TAG_COLLECTION = "players";

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
