package org.beavers.gameplay;

/**
 * @author winniehell
 * class to uniquely identify a game within a server
 */
public final class GameID {

	private final String ID; 

	public GameID(String pID) {
		this.ID = pID;
	}

	@Override
	public boolean equals(Object other) {
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
