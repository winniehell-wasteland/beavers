package org.beavers.gameplay;


import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
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
		TMXLoader loader = new TMXLoader(app, app.getEngine().getTextureManager());
		
		try {
			map = loader.loadFromAsset(app, pGame.getFilename() + ".tmx");
		} catch (TMXLoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void playOutcome(OutcomeContainer outcome)
	{
		
	}

	private final AppActivity app;
}
