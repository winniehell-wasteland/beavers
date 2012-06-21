package org.beavers.gameplay;

import java.io.Serializable;

/**
 * @author winniehell
 * class to uniquely identify a player (i.e. a playing device)
 */
@SuppressWarnings("serial")
public final class PlayerID implements Serializable {
	private final String ID;

	public PlayerID(final String pID) {
		ID = pID;
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof PlayerID)
		{
			return ID.equals(((PlayerID)other).ID);
		}
		else
		{
			return false;
		}
	}

	public static PlayerID fromJSON(final Object pJSON) {
		if(pJSON instanceof String)
		{
			return new PlayerID((String) pJSON);
		}

		return null;
	}

	public Object toJSON()
	{
		return ID;
	}

	@Override
	public String toString() {
		return ID;
	}
}
