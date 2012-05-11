package org.beavers.gameplay;

/**
 * @author winniehell
 * class to uniquely identify a player (i.e. a playing device)
 */
public class PlayerID {

	private final String ID; 
	
	public PlayerID(String pID) {
		this.ID = pID;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof PlayerID)
		{
			return (ID == ((PlayerID)other).ID);
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
