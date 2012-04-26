package org.beavers.ingame;

public class Character extends GameObject {
	
	
	public int getHealthPercentage()
	{
		return -1;
	}
	
	public Weapon getWeapon()
	{
		return null;
	}
	
	public ViewDirection getViewDirection()
	{
		return null;
	}
	
	public Action getAction(int index)
	{
		return null;
	}

	@Override
	public String getType() {
		return this.getClass().getSimpleName();
	}
}
