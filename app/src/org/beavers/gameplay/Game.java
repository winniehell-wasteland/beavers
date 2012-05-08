package org.beavers.gameplay;

import java.io.FileNotFoundException;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.opengl.texture.TextureManager;
import org.beavers.AppActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Game extends Activity {

	Engine engine = null;
	TMXTiledMap map;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();		
		AppActivity app = intent.getParcelableExtra("app");
		
		engine = app.getEngine();
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
		TMXLoader loader = new TMXLoader(this, engine.getTextureManager());
		
		try {
			map = loader.load(openFileInput(name + ".tmx"));
		} catch (TMXLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
