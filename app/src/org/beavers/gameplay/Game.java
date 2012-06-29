package org.beavers.gameplay;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * class to uniquely identify a game within a server
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public final class Game implements Parcelable {
	public Game(final UUID pID, final String pName) {
		id = pID;
		name = pName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof Game)
		{
			return id.equals(((Game)other).id);
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

	public Object toJSON()
	{
		final JSONObject json = new JSONObject();

		try {
			json.put("id", id.toString());
			json.put("name", name);
		} catch (final JSONException e) {
			e.printStackTrace();
			return null;
		}

		return json;
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

	public static Game fromJSON(final Object pJSON) {
		if(pJSON instanceof JSONObject)
		{
			final JSONObject obj = (JSONObject) pJSON;
			return new Game(
				UUID.fromString(obj.optString("id")),
				obj.optString("name"));
		}

		return null;
	}

	private final UUID id;
	private final String name;

	private Game(final Parcel pParcel)
	{
		id = UUID.fromString(pParcel.readString());
		name = pParcel.readString();
	}
}
