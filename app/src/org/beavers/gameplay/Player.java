package org.beavers.gameplay;

public class Player {
		
	public PlayerID getID()
	{
		return null;
	}

	public boolean isServer(GameInfo pGame) {
		return this.equals(pGame.getServer());
	}
	
	public int getActionPoints(GameInfo pGame)
	{
		return -1;
	}
	
	public void resetActionPoints(GameInfo pGame)
	{
		//getInitialActionPoints();
	}
	
	@Override
	public boolean equals(Object other) {

		if(other instanceof Player)
		{
			return (getID() == ((Player)other).getID());
		}
		else if(other instanceof PlayerID)
		{
			return (getID() == (PlayerID)other);
		}
		else
		{
			return false;
		}
	}
}
