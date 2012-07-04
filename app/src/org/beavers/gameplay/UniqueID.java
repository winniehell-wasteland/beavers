package org.beavers.gameplay;

import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class to uniquely identify a named thing
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 * @see {@link Player}, {@link Game}
 */
public class UniqueID implements Parcelable {

	public UniqueID(final UUID pID, final String pName) {
		id = pID;
		name = pName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof UniqueID)
		{
			return id.equals(((UniqueID)other).id);
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
	public int hashCode() {
		return id.hashCode();
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

	public static final Parcelable.Creator<UniqueID> CREATOR =
		new Parcelable.Creator<UniqueID>() {
			@Override
			public UniqueID createFromParcel(final Parcel parcel) {
				return new UniqueID(parcel);
			}

			@Override
			public UniqueID[] newArray(final int size) {
				return new UniqueID[size];
			}
	};

	private final UUID id;
	private final String name;

	protected UniqueID(final Parcel pParcel) {
		id = UUID.fromString(pParcel.readString());
		name = pParcel.readString();
	}
}
