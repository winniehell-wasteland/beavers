package org.beavers.gameplay;

import java.io.Serializable;

/**
 * @author winniehell
 * class to uniquely identify a game within a server
 */
@SuppressWarnings("serial")
public final class GameID implements Serializable{

	private final String ID;

	public GameID(final String pID) {
		ID = pID;
	}

	@Override
	public boolean equals(final Object other) {
		if(other instanceof GameID)
		{
			return ID.equals(((GameID)other).ID);
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString() {
		return ID;
	}
}
