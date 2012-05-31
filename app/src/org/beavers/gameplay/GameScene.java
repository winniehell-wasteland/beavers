package org.beavers.gameplay;

import java.util.Hashtable;

import org.anddev.andengine.entity.IEntity;
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
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.Path.Step;
import org.anddev.andengine.util.path.astar.AStarPathFinder;
import org.beavers.AppActivity;
import org.beavers.ingame.EmptyTile;
import org.beavers.ingame.GameObject;
import org.beavers.ingame.Soldier;
import org.beavers.ui.ContextMenuHandler;

import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Toast;

public class GameScene extends Scene
	implements ITiledMap<IEntity>,
		IOnSceneTouchListener,
		IHoldDetectorListener,
		IScrollDetectorListener,
		OnCreateContextMenuListener
{
	public GameInfo currentGame;

	public GameScene(final AppActivity pApp)
	{
		super();

		app = pApp;

		scrollDetector = new SurfaceScrollDetector(10.0f, this);
		holdDetector = new HoldDetector(200, 10.0f, 0.1f, this);
		registerUpdateHandler(holdDetector);

		gameObjects = new Hashtable<TMXTile, GameObject>();

		loadMap("test");
		loadSoldiers();

		pathFinder = new AStarPathFinder<IEntity>(this, 1600, true);

		wayPointMark = new Rectangle(0, 0, map.getTileWidth(), map.getTileHeight());
		wayPointMark.setColor(0.0f, 1.0f, 0.0f, 0.5f);
		wayPointMark.setZIndex(0);

		sortChildren();

		setOnSceneTouchListener(this);
	}

	public void addWayPoint(final TMXTile pTile) {
		if(pTile == null)
		{
			Log.wtf(TAG, "There is no way to nirvana!");
		}
		else if(selectedSoldier == null)
		{
			Toast.makeText(app, "No soldier selected!", Toast.LENGTH_SHORT).show();
		}
		else if((pTile.getTMXTileProperties(map) != null)
				/*&& lastContextMenuTile.getTMXTileProperties(map).containsTMXProperty("blocked", "true")*/)
		{
			Toast.makeText(app, "Tile is blocked!", Toast.LENGTH_SHORT).show();
		}
		else
		{
			final Path path = findPath(selectedSoldier, pTile);

			if(path == null)
			{
				Toast.makeText(app, "No path found!", Toast.LENGTH_SHORT).show();
			}
			else
			{
				selectedSoldier.stop();
				//selectedSoldier.move(pTile);
				//selectedSoldier.move(map, path);

				for(int i = 0; i < path.getLength(); ++i)
				{
					final Step step = path.getStep(i);
					final TMXTile tile = map.getTMXLayers().get(0).getTMXTile(step.getTileColumn(), step.getTileRow());

					final Rectangle rect = new Rectangle(tile.getTileX(), tile.getTileY(), tile.getTileWidth(), tile.getTileHeight());
					rect.setColor(1.0f, 1.0f, 0.0f, 0.5f);
					attachChild(rect);
				}

				wayPointMark.setPosition(pTile.getTileX(), pTile.getTileY());

				if(!wayPointMark.hasParent())
				{
					attachChild(wayPointMark);
				}
			}
		}
	}

	public void fireShot(final TMXTile pTile) {

		if(selectedSoldier != null)
		{
			// TODO if(predictCollision(centerX,centerY,10)){

			final float centerX = pTile.getTileX() + pTile.getTileWidth()/2,
					centerY = pTile.getTileY() + pTile.getTileHeight()/2;

			selectedSoldier.shootAt(centerX, centerY, this);
			//attachChild(shot);
		}
	}

	public TMXTiledMap getMap() {
		return map;
	}

	public Soldier getSelectedSoldier() {
		return selectedSoldier;
	}

	@Override
	public float getStepCost(final IEntity pEntity, final int pFromTileColumn,
			final int pFromTileRow, final int pToTileColumn, final int pToTileRow) {

		// prevent diagonals at blocked tiles
		if((Math.abs(pToTileRow - pFromTileRow) == 1)
				&& (Math.abs(pToTileColumn - pFromTileColumn) == 1))
		{
			if(isTileBlocked(pEntity, pFromTileColumn, pToTileRow)
					|| isTileBlocked(pEntity, pToTileColumn, pFromTileRow))
			{
				return 100;
			}
		}

		return 0;
	}

	@Override
	public int getTileColumns() {
		if(map == null)
		{
			return 0;
		}
		else
		{
			return map.getTileColumns();
		}
	}

	@Override
	public int getTileRows() {
		if(map == null)
		{
			return 0;
		}
		else
		{
			return map.getTileRows();
		}
	}

	@Override
	public boolean isTileBlocked(final IEntity pEntity, final int pTileColumn, final int pTileRow) {
		if(map == null)
		{
			return false;
		}
		else
		{
			final TMXTile tile = map.getTMXLayers().get(0).getTMXTile(pTileColumn, pTileRow);

			return ((tile == null) || (tile.getTMXTileProperties(map) != null));
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu pMenu, final View pView,
			final ContextMenuInfo pInfo) {

		if(contextMenuHandler != null)
		{
	        final MenuInflater inflater = app.getMenuInflater();
	        inflater.inflate(contextMenuHandler.getMenuID(), pMenu);

	        pMenu.setHeaderTitle(contextMenuHandler.getClass().getSimpleName());

			for(int i = 0; i < pMenu.size(); ++i)
			{
				pMenu.getItem(i).setOnMenuItemClickListener(contextMenuHandler);
			}
		}
	}

	@Override
	public void onHold(final HoldDetector pHoldDetector, final long pHoldTimeMilliseconds,
			final float pHoldX, final float pHoldY) {
	}

	@Override
	public void onHoldFinished(final HoldDetector pHoldDetector,
			final long pHoldTimeMilliseconds, final float pHoldX, final float pHoldY) {

		Log.d("onHoldFinished", "t="+pHoldTimeMilliseconds);

		final TMXTile tile = map.getTMXLayers().get(0).getTMXTileAt(pHoldX, pHoldY);

		if(tile != null)
		{
			if(gameObjects.containsKey(tile))
			{
				final GameObject obj = gameObjects.get(tile);

				if(obj instanceof Soldier)
				{
					selectSoldier((Soldier) obj);
				}
			}
			else if(!isTileBlocked(null, tile.getTileColumn(), tile.getTileRow()))
			{
				contextMenuHandler = new EmptyTile(this, tile);
				app.showGameContextMenu();
			}
		}
	}

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

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent,
			final float pDistanceX, final float pDistanceY) {
			app.getEngine().getCamera().offsetCenter(-pDistanceX*CAMERA_SPEED, -pDistanceY*CAMERA_SPEED);
	}

	@Override
	public void onTileVisitedByPathFinder(final int pTileColumn, final int pTileRow) {
		// TODO Auto-generated method stub
	}

	public void playOutcome(final OutcomeContainer outcome)
	{
		// TODO Auto-generated method stub
	}

	public void startPlanningPhase() {
		// TODO Auto-generated method stub
	}

	private final static float CAMERA_SPEED = 1.50f;
	private static final String TAG = "GameScene";

	private final AppActivity app;

	private final SurfaceScrollDetector scrollDetector;
	private final HoldDetector holdDetector;

	private TMXTiledMap map;
	private final Hashtable<TMXTile, GameObject> gameObjects;
	private final AStarPathFinder<IEntity> pathFinder;

	private Soldier selectedSoldier;
	private final Rectangle wayPointMark;

	private ContextMenuHandler contextMenuHandler;

	private void addSoldier(final Soldier pSoldier)
	{
		gameObjects.put(pSoldier.getTile(), pSoldier);
		attachChild(pSoldier);
	}

	private Path findPath(final Soldier pSoldier, final TMXTile pTarget)
	{
		return pathFinder.findPath(pSoldier, 0,
        		pSoldier.getTile().getTileColumn(), pSoldier.getTile().getTileRow(),
        		pTarget.getTileColumn(), pTarget.getTileRow());
	}

	private void loadMap(final String pMapName)
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
			return;
		}

		final TMXLayer tmxLayer = map.getTMXLayers().get(0);
		this.attachChild(tmxLayer);
	}


	private void loadSoldiers(){
		addSoldier(new Soldier(0, map.getTMXLayers().get(0).getTMXTileAt(20, 20)));
		addSoldier(new Soldier(0, map.getTMXLayers().get(0).getTMXTileAt(140, 20)));
	}

	private synchronized void selectSoldier(final Soldier pSoldier) {
		if(selectedSoldier != pSoldier)
		{
			if(selectedSoldier != null)
			{
				selectedSoldier.markDeselected();
			}

			pSoldier.markSelected();
			selectedSoldier = pSoldier;
		}
	}

}
