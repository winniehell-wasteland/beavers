package org.beavers.gameplay;


import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.entity.primitive.Line;
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
import org.beavers.ingame.Position;
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
		c= new Soldier(app,this,0);
		c.createSoldier(20,20);
		this.registerTouchArea(c.getSprite());
		this.setTouchAreaBindingEnabled(true);
		
		c1= new Soldier(app,this,0);
		c1.createSoldier(40,20);
		this.registerTouchArea(c1.getSprite());
		this.setTouchAreaBindingEnabled(true);
	}
 
	public void setSelectedSoldier(Soldier s){
		if(currentSoldier!=null)currentSoldier.markDeselected();
		currentSoldier=s;
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
	private Soldier c,c1;
	private Soldier currentSoldier=null;
	@Override
	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
			float pDistanceX, float pDistanceY) {		
			app.getEngine().getCamera().offsetCenter(-pDistanceX*CAMERA_SPEED, -pDistanceY*CAMERA_SPEED);
	}
		

	@Override
	public void onHold(HoldDetector pHoldDetector, long pHoldTimeMilliseconds,
			float pHoldX, float pHoldY) {
	}
	
	private Rectangle r;
	@Override
	public void onHoldFinished(HoldDetector pHoldDetector,
			long pHoldTimeMilliseconds, float pHoldX, float pHoldY) {
		
		Log.d("onHoldFinished", "t="+pHoldTimeMilliseconds);

		
		if(currentSoldier!=null){
			if(this.map.getTMXLayers().get(0).getTMXTileAt(pHoldX, pHoldY)!=null){
				if(this.map.getTMXLayers().get(0).getTMXTileAt(pHoldX, pHoldY).getTMXTileProperties(map)==null 
					&&predictCollision(pHoldX,pHoldY,20)
					/*&this.map.getTMXLayers().get(0).getTMXTileAt(pHoldX, pHoldY).getTMXTileProperties(map).containsTMXProperty("blocked", "true")*/){
					currentSoldier.stop();
					currentSoldier.move(Math.round(pHoldX),Math.round(pHoldY));
					
					if(r!=null)GameScene.this.detachChild(r);
					r= new Rectangle(pHoldX, pHoldY, 10, 10);
					r.setColor(0, 1, 0);
					GameScene.this.attachChild(r);
				}
			}
		}
	}	
	
	
	public boolean predictCollision(float x1, float y1, int dist){

		float angleX=x1-(currentSoldier.getSprite().getX()+currentSoldier.getSprite().getWidth()/2);
		float angleY=y1-(currentSoldier.getSprite().getY()+currentSoldier.getSprite().getHeight()/2);
			
		double c= Math.sqrt(angleX*angleX+angleY*angleY);
		
		
		for(int step=dist;step<c;step+=dist){
			float newX=(float) (step/c*angleX)+currentSoldier.getSprite().getX()+currentSoldier.getSprite().getWidth()/2;
			float newY=(float) (step/c*angleY)+currentSoldier.getSprite().getY()+currentSoldier.getSprite().getHeight()/2;
			//Toast.makeText(app,""+dist/c,Toast.LENGTH_SHORT ).show();
			Rectangle dot= new Rectangle(newX, newY, 2, 2);
			dot.setColor(1, 0, 0);
			GameScene.this.attachChild(dot);
			if(this.map.getTMXLayers().get(0).getTMXTileAt(newX, newY)!=null){
				if(this.map.getTMXLayers().get(0).getTMXTileAt(newX, newY).getTMXTileProperties(map)!=null){
					return false;
				}
			}
		}
		return true;
	}
	
	
}
