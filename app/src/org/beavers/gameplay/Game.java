package org.beavers.gameplay;

import java.io.FileNotFoundException;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.scene.Scene;

import android.content.Context;

public class Game extends Scene {

	private Context context;
	private Engine engine;
	private TMXTiledMap map;
	
	public Game(final Context pContext, final Engine pEngine)
	{
		super();

		this.context = pContext;
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
	
	private void loadMap(String name)
	{
		TMXLoader loader = new TMXLoader(this.context, engine.getTextureManager());
		
		try {
			map = loader.load(this.context.openFileInput(name + ".tmx"));
		} catch (TMXLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
