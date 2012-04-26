package org.beavers.gameplay;

public class Player {
	public String getID()
	{
		return null;
	}

	public boolean isServer(Game game) {
		// TODO Auto-generated method stub
		return (game.getServer() == this);
	}
	
	public int getActionPoints(Game game)
	{
		return -1;
	}
	
	public void resetActionPoints(Game game)
	{
		game.getInitialActionPoints();
	}
}
