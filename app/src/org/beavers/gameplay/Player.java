package org.beavers.gameplay;

public class Player {
		
	public PlayerID getID()
	{
		return null;
	}

	public boolean isServer(Game game) {
		return this.equals(game.getInfo().getServer());
	}
	
	public int getActionPoints(Game game)
	{
		return -1;
	}
	
	public void resetActionPoints(Game game)
	{
		game.getInitialActionPoints();
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
