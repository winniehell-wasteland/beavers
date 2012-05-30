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
import org.beavers.R;
import org.beavers.ingame.Soldier;

import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;

public class GameScene extends Scene implements IOnSceneTouchListener, IHoldDetectorListener, IScrollDetectorListener, OnCreateContextMenuListener, OnMenuItemClickListener {

	public GameInfo currentGame;

	public GameScene(final AppActivity pApp)
	{
		super();

		app = pApp;
		scrollDetector = new SurfaceScrollDetector(10.0f, this);
		holdDetector = new HoldDetector(200, 10.0f, 0.1f, this);
		registerUpdateHandler(holdDetector);

		loadMap("test");
		loadSoldiers();

		setOnSceneTouchListener(this);
	}

	public void loadMap(final String pMapName)
	{
		try {
			final TMXLoader tmxLoader = new TMXLoader(app, app.getEngine().getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {

				}
			});
			map = tmxLoader.loadFromAsset(app, "tmx/"+pMapName+".tmx");
		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
		}

		final TMXLayer tmxLayer = map.getTMXLayers().get(0);
		this.attachChild(tmxLayer);


	}

	public void playOutcome(final OutcomeContainer outcome)
	{

	}


	private void loadSoldiers(){
		c= new Soldier(app,this,0);
		c.createSoldier(20,20);
		registerTouchArea(c.getSprite());
		setTouchAreaBindingEnabled(true);

		c1= new Soldier(app,this,0);
		c1.createSoldier(40,20);
		registerTouchArea(c1.getSprite());
		setTouchAreaBindingEnabled(true);
	}

	public void setSelectedSoldier(final Soldier s){
		if(currentSoldier!=null)currentSoldier.markDeselected();
		currentSoldier=s;
	}


	final static float CAMERA_SPEED = 1.50f;

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
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
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent,
			final float pDistanceX, final float pDistanceY) {
			app.getEngine().getCamera().offsetCenter(-pDistanceX*CAMERA_SPEED, -pDistanceY*CAMERA_SPEED);
	}


	@Override
	public void onHold(final HoldDetector pHoldDetector, final long pHoldTimeMilliseconds,
			final float pHoldX, final float pHoldY) {
	}

	@Override
	public void onHoldFinished(final HoldDetector pHoldDetector,
			final long pHoldTimeMilliseconds, final float pHoldX, final float pHoldY) {

		Log.d("onHoldFinished", "t="+pHoldTimeMilliseconds);

		lastContextMenuTile = map.getTMXLayers().get(0).getTMXTileAt(pHoldX, pHoldY);

		if(lastContextMenuTile != null)
		{
			app.showGameContextMenu();
		}
	}


	public boolean predictCollision(final float x1, final float y1, final int dist){

		final float angleX=x1-(currentSoldier.getSprite().getX()+currentSoldier.getSprite().getWidth()/2);
		final float angleY=y1-(currentSoldier.getSprite().getY()+currentSoldier.getSprite().getHeight()/2);

		final double c= Math.sqrt(angleX*angleX+angleY*angleY);


		for(int step=dist;step<c;step+=dist){
			final float newX=(float) (step/c*angleX)+currentSoldier.getSprite().getX()+currentSoldier.getSprite().getWidth()/2;
			final float newY=(float) (step/c*angleY)+currentSoldier.getSprite().getY()+currentSoldier.getSprite().getHeight()/2;
			//Toast.makeText(app,""+dist/c,Toast.LENGTH_SHORT ).show();
			final Rectangle dot= new Rectangle(newX, newY, 2, 2);
			dot.setColor(1, 0, 0);
			GameScene.this.attachChild(dot);
			if(map.getTMXLayers().get(0).getTMXTileAt(newX, newY)!=null){
				if(map.getTMXLayers().get(0).getTMXTileAt(newX, newY).getTMXTileProperties(map)!=null){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
        final MenuInflater inflater = app.getMenuInflater();
        inflater.inflate(R.menu.context_tile, menu);

        // handle click events
        for(int i = 0; i < menu.size(); ++i)
        {
        	menu.getItem(i).setOnMenuItemClickListener(this);
        }
	}

	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		switch(pItem.getItemId())
		{
		case R.id.context_menu_move:

			if((currentSoldier!=null)
					&& (lastContextMenuTile != null)
					&& (lastContextMenuTile.getTMXTileProperties(map)==null)
					/*& lastContextMenuTile.getTMXTileProperties(map).containsTMXProperty("blocked", "true")*/
					)
			{
				final float centerX = lastContextMenuTile.getTileX() + lastContextMenuTile.getTileWidth()/2,
						centerY = lastContextMenuTile.getTileY() + lastContextMenuTile.getTileHeight()/2;

				if(predictCollision(centerX,centerY,20)){
					currentSoldier.stop();
					currentSoldier.move(Math.round(centerX),Math.round(centerY));

					if(wayPointMark!=null)
					{
						GameScene.this.detachChild(wayPointMark);
					}

					wayPointMark= new Rectangle(centerX, centerY, 10, 10);
					wayPointMark.setColor(0, 1, 0);
					GameScene.this.attachChild(wayPointMark);
				}
			}

			return true;
		default:
		return false;
		}
	}

	public void startPlanningPhase() {
		// TODO Auto-generated method stub

	}

	private Rectangle wayPointMark;
	private TMXTile lastContextMenuTile;
}
