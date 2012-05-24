package org.beavers.gameplay;


import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.HoldDetector;
import org.anddev.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.util.Debug;
import org.beavers.AppActivity;
import org.beavers.ingame.Soldier;

import android.util.Log;
import android.widget.Toast;

public class GameScene extends Scene implements IOnSceneTouchListener, IHoldDetectorListener, IScrollDetectorListener {
	
	public GameScene(final AppActivity pApp)
	{
		super();
		
		app = pApp;
		scrollDetector = new SurfaceScrollDetector(10.0f, this);
		holdDetector = new HoldDetector(200, 10.0f, 0.1f, this);
		this.registerUpdateHandler(holdDetector);
		
		loadMap("test");
		loadSoldiers();
		
		this.setOnSceneTouchListener(this);
	}

	public void loadMap(String pMapName)
	{
		try {
			final TMXLoader tmxLoader = new TMXLoader(this.app, app.getEngine().getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {

				}
			});
			this.map = tmxLoader.loadFromAsset(app, "tmx/"+pMapName+".tmx");

		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}

		final TMXLayer tmxLayer = this.map.getTMXLayers().get(0);
		this.attachChild(tmxLayer);
	}
	
	public void playOutcome(OutcomeContainer outcome)
	{
		
	}
	

	private void loadSoldiers(){
		c= new Soldier(app,0,0,0);
		c.createSoldier(this,20,20);
	
		
		Toast.makeText(app,""+c.getSprite().getRotation(), Toast.LENGTH_LONG).show();
		c.move(80,300);
		//c.move(-200,40);
		//Toast.makeText(context,a[0]+" "+a[1] , Toast.LENGTH_LONG).show();
	}
 
	final static float CAMERA_SPEED = 1.50f;
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(holdDetector.onSceneTouchEvent(pScene, pSceneTouchEvent)
				| scrollDetector.onSceneTouchEvent(pScene, pSceneTouchEvent))
		{
			return true;
		}
		else
		{
			return false;
		}
 	}

	private final AppActivity app;
	private final SurfaceScrollDetector scrollDetector;
	private final HoldDetector holdDetector;

	private TMXTiledMap map;
	private Soldier c;

	@Override
	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
			float pDistanceX, float pDistanceY) {		
		app.getEngine().getCamera().offsetCenter(-pDistanceX*CAMERA_SPEED, -pDistanceY*CAMERA_SPEED);
	}

	@Override
	public void onHold(HoldDetector pHoldDetector, long pHoldTimeMilliseconds,
			float pHoldX, float pHoldY) {
	}

	@Override
	public void onHoldFinished(HoldDetector pHoldDetector,
			long pHoldTimeMilliseconds, float pHoldX, float pHoldY) {
		
		Log.d("onHoldFinished", "t="+pHoldTimeMilliseconds);

		Rectangle r= new Rectangle(pHoldX, pHoldY, 10, 10);
		r.setColor(0, 1, 0);
		GameScene.this.attachChild(r);
		
		c.stop();
		c.move(Math.round(pHoldX),Math.round(pHoldY));
	}
}
