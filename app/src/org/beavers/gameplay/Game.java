package org.beavers.gameplay;


import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.scene.Scene;

public class Game extends Scene {

	Engine engine = null;
	TMXTiledMap map;
	
	public Game(final Engine pEngine)
	{
		super();
		
		this.engine = pEngine;
	}

	public String getID()
	{
		return null;
	}

	public Player getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setServer(Player player) {
		// TODO Auto-generated method stub
		
	}
	
	public void playOutcome(OutcomeContainer outcome)
	{
		
	}
	
	public int getInitialActionPoints()
	{
		return -1;
	}
}
