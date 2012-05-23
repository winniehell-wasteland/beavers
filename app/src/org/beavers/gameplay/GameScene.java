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
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.util.Debug;
import org.beavers.AppActivity;
import org.beavers.ingame.Soldier;

import android.view.MotionEvent;
import android.widget.Toast;

public class GameScene extends Scene implements IOnSceneTouchListener {

	TMXTiledMap map;
	
	public GameScene(final AppActivity pApp)
	{
		super();
		
		app = pApp;
		
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
		float a[]=c.getSceneCoordinates();
		
		Toast.makeText(app,""+c.getSprite().getRotation(), Toast.LENGTH_LONG).show();
		c.move(80,300);
		//c.move(-200,40);
		//Toast.makeText(context,a[0]+" "+a[1] , Toast.LENGTH_LONG).show();
	}
 
	float x1=0.0f ;
	float y1=0.0f ;
	float x2=0.0f; 
	float y2=0.0f; 
	float diffX=0.0f;
	float diffY=0.0f;
	int camSpeed=150;
	
	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pTouchEvent) {
		if(pTouchEvent.getAction() == MotionEvent.ACTION_UP){
			Rectangle r= new Rectangle(pTouchEvent.getMotionEvent().getX(), pTouchEvent.getMotionEvent().getY(), 10, 10);
			r.setColor(0, 1, 0);
			this.attachChild(r);
			c.move((int)pTouchEvent.getMotionEvent().getX(),(int)pTouchEvent.getMotionEvent().getY());
		}
		 if(pTouchEvent.getAction() == MotionEvent.ACTION_DOWN)
	        {
			
			x1= pTouchEvent.getMotionEvent().getX();
			y1= pTouchEvent.getMotionEvent().getY();
	             
	        }
		 else if(pTouchEvent.getAction() == MotionEvent.ACTION_MOVE)
	        {
				
			x2= pTouchEvent.getMotionEvent().getX();
			y2= pTouchEvent.getMotionEvent().getY();
			diffX=x1-x2;
			diffY=y1-y2;
			if(diffX<4 && diffX >-4)diffX=0;
			if(diffY<4 && diffY >-4)diffY=0;
			
			app.getEngine().getCamera().setCenter(app.getEngine().getCamera().getCenterX()+diffX*camSpeed,app.getEngine().getCamera().getCenterY()+diffY*camSpeed);
			x1=x2;
			y1=y2;
	        }
		return true;
 	}

	private final AppActivity app;
	private Soldier c;
}
