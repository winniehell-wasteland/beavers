package org.beavers.gameplay;


import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
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
}
