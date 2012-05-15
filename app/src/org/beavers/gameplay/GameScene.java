package org.beavers.gameplay;


import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.scene.Scene;
import org.beavers.AppActivity;

public class GameScene extends Scene {

	TMXTiledMap map;
	
	public GameScene(final AppActivity pApp)
	{
		super();
		
		app = pApp;
	}

	public void loadGame(GameInfo pGame)
	{
		// pGame.getFilename()
	}
	
	public void playOutcome(OutcomeContainer outcome)
	{
		
	}

	private final AppActivity app;
}
