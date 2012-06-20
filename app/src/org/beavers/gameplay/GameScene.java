package org.beavers.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.anddev.andengine.engine.camera.hud.HUD;
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
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.astar.AStarPathFinder;
import org.beavers.AppActivity;
import org.beavers.R;
import org.beavers.ingame.EmptyTile;
import org.beavers.ingame.GameObject;
import org.beavers.ingame.PathWalker;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.WayPoint;
import org.beavers.ui.ContextMenuHandler;

import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Toast;

public class GameScene extends Scene
	implements ITiledMap<GameObject>,
		IOnSceneTouchListener,
		IHoldDetectorListener,
		IScrollDetectorListener,
		OnCreateContextMenuListener
{
	/**
	 * @name z-index constants
	 * @{
	 */
	public static final int ZINDEX_BACKGROUND = 0;
	public static final int ZINDEX_WAYPOINTS = ZINDEX_BACKGROUND + 10;
	public static final int ZINDEX_AIMPOINTS = ZINDEX_WAYPOINTS + 10;
	public static final int ZINDEX_SOLDIERS = ZINDEX_AIMPOINTS + 10;
	/**
	 * @}
	 */

	/**
	 * @name helper functions
	 * @{
	 */
	public static int getTileCenterX(final TMXTile pTile)
	{
		return pTile.getTileX() + pTile.getTileWidth()/2;
	}

	public static int getTileCenterY(final TMXTile pTile)
	{
		return pTile.getTileY() + pTile.getTileHeight()/2;
	}
	/**
	 * @}
	 */

	public GameInfo currentGame;

	/**
	 * default constructor
	 * @param pApp
	 */
	public GameScene(final AppActivity pApp)
	{
		super();

		app = pApp;

		// initialize detectors
		scrollDetector = new SurfaceScrollDetector(10.0f, this);
		holdDetector = new HoldDetector(200, 10.0f, 0.1f, this);
		registerUpdateHandler(holdDetector);

		// initialize game object containers
		gameObjects = new HashMap<TMXTile, GameObject>();
		soldiers = new ArrayList<HashSet<Soldier>>(getTeamCount());

		for(int i = 0; i < getTeamCount(); ++i)
		{
			soldiers.add(new HashSet<Soldier>());
		}

		loadMap("map");
		loadSoldiers();

		if(map != null)
		{
			pathFinder = new AStarPathFinder<GameObject>(this, 1600, true);
		}
		else
		{
			pathFinder = null;
		}

		setOnSceneTouchListener(this);
	}
	
	public AppActivity getApp(){
		return app;
	}
	/**
	 * insert a new game object on the map
	 * @param pObject the object to insert
	 */
	public void addObject(final GameObject pObject)
	{
		gameObjects.put(pObject.getTile(), pObject);
		attachChild(pObject);

		sortChildren();

		if(pObject instanceof Soldier)
		{
			final Soldier soldier = (Soldier) pObject;
			soldiers.get(soldier.getTeam()).add(soldier);
		}
	}

	public TMXTiledMap getMap() {
		return map;
	}

	public IPathFinder<GameObject> getPathFinder() {
		return pathFinder;
	}

	public Soldier getSelectedSoldier() {
		return selectedSoldier;
	}

	public HashSet<Soldier> getSoldiers(final int pTeam) {
		if(pTeam < getTeamCount())
		{
			return soldiers.get(pTeam);
		}
		else
		{
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public float getStepCost(final GameObject pObject, final int pFromTileColumn,
			final int pFromTileRow, final int pToTileColumn, final int pToTileRow) {

		return pObject.getStepCost(this, collisionLayer.getTMXTile(pFromTileColumn, pFromTileRow),
				collisionLayer.getTMXTile(pToTileColumn, pToTileRow));
	}

	/**
	 *
	 * @return
	 */
	public int getTeamCount() {
		return 2;
	}

	@Override
	public int getTileColumns() {
		return map.getTileColumns();
	}

	@Override
	public int getTileRows() {
		return map.getTileRows();
	}

	public TMXLayer getCollisionLayer() {
		return collisionLayer;
	}

	@Override
	public boolean isTileBlocked(final GameObject pObject, final int pTileColumn, final int pTileRow) {
		final TMXTile tile = collisionLayer.getTMXTile(pTileColumn, pTileRow);
		//return tile!=null;

		return ((tile == null) || (tile.getGlobalTileID()!=0)); // (tile.getTMXTileProperties(map) != null));
	}

	/**
	 * move a {@link GameObject} to a new tile
	 * @param pObject moving object
	 * @param pSourceTile old position
	 * @param pTargetTile new position
	 */
	public void moveObject(final GameObject pObject, final TMXTile pSourceTile, final TMXTile pTargetTile) {
		gameObjects.remove(pSourceTile);
		gameObjects.put(pTargetTile, pObject);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu pMenu, final View pView,
			final ContextMenuInfo pInfo) {

		if(contextMenuHandler != null)
		{
	        final MenuInflater inflater = app.getMenuInflater();
	        inflater.inflate(contextMenuHandler.getMenuID(), pMenu);

			for(int i = 0; i < pMenu.size(); ++i)
			{
				pMenu.getItem(i).setOnMenuItemClickListener(contextMenuHandler);
			}

			contextMenuHandler.onMenuCreated(pMenu);
		}
	}

	public boolean onCreateOptionsMenu(final Menu menu) {
	    final MenuInflater inflater = app.getMenuInflater();
		inflater.inflate(R.menu.game_menu, menu);

		return true;
	}

	@Override
	public void onHold(final HoldDetector pHoldDetector, final long pHoldTimeMilliseconds,
			final float pHoldX, final float pHoldY) {
	}

	@Override
	public void onHoldFinished(final HoldDetector pHoldDetector,
			final long pHoldTimeMilliseconds, final float pHoldX, final float pHoldY) {

		final TMXTile tile = collisionLayer.getTMXTileAt(pHoldX, pHoldY);

		if(tile != null)
		{
			Log.d(TAG, "Hold on tile ("+tile.getTileColumn()+","+tile.getTileRow()+")");

			// create new Aim if necessary
			if(contextMenuHandler instanceof WayPoint)
			{
				final WayPoint selectedWayPoint = (WayPoint) contextMenuHandler;

				if(selectedWayPoint.isWaitingForAim())
				{
					if(!selectedWayPoint.getTile().equals(tile))
					{
						selectedWayPoint.setAim(tile);
						sortChildren();
					}

					return;
				}
			}

			if(gameObjects.containsKey(tile))
			{
				final GameObject obj = gameObjects.get(tile);

				if(obj instanceof Soldier)
				{
					selectSoldier((Soldier) obj);
				}
				else if(obj instanceof WayPoint)
				{
					final WayPoint waypoint = (WayPoint) obj;

					if(waypoint.getSoldier() != selectedSoldier)
					{
						selectSoldier(waypoint.getSoldier());
						app.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(app, "Selected soldier by waypoint", Toast.LENGTH_SHORT).show();
							}
						});
					}
					else
					{
						contextMenuHandler = waypoint;
						app.showGameContextMenu();
					}
				}
			}
			else if(!isTileBlocked(null, tile.getTileColumn(), tile.getTileRow())
					&& (selectedSoldier != null))
			{
				contextMenuHandler = new EmptyTile(this, tile);
				app.showGameContextMenu();
			}
		}
	}

	public boolean onOptionsItemSelected(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.menu_execute:
			final PathWalker walker = new PathWalker(this, selectedSoldier);
			walker.start();

			return true;
		default:
			return false;
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

	}

	public void playOutcome(final OutcomeContainer outcome)
	{
		// TODO Auto-generated method stub
	}

	public void removeObject(final GameObject pObject) {
		if(gameObjects.get(pObject.getTile()).equals(pObject))
		{
			gameObjects.remove(pObject.getTile());
		}

		detachChild(pObject);
	}

	public void startPlanningPhase() {
		// TODO Auto-generated method stub
	}

	private final static float CAMERA_SPEED = 1.50f;
	@SuppressWarnings("unused")
	private static final String TAG = "GameScene";

	private final AppActivity app;

	/**
	 * @name detectors
	 * @{
	 */
	private final SurfaceScrollDetector scrollDetector;
	private final HoldDetector holdDetector;
	/**
	 * @}
	 */

	/**
	 * @name TMX
	 * @{
	 */
	private TMXTiledMap map;
	private TMXLayer collisionLayer;
	private TMXLayer floorLayer;
	/**
	 * @}
	 */

	/**
	 * @name game object containers
	 * @{
	 */
	private final HashMap<TMXTile, GameObject> gameObjects;
	private final ArrayList<HashSet<Soldier>> soldiers;
	/**
	 * @}
	 */

	private final AStarPathFinder<GameObject> pathFinder;

	private ContextMenuHandler contextMenuHandler;
	private Soldier selectedSoldier;

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

		collisionLayer = map.getTMXLayers().get(0);
		floorLayer =map.getTMXLayers().get(1);
		this.attachChild(collisionLayer);
		this.attachChild(floorLayer);
	}

	private HUD hud;
	private void loadHUD(){
		hud=new HUD();
		hud.setPosition(0, 0);
        getSelectedSoldier().changeHP(-10);
		final Rectangle hud_back = new Rectangle(0,0,app.getEngine().getCamera().getWidth(),30);
		final Rectangle missing_health = new Rectangle(10,10,100,8);
		final Rectangle health_bar = new Rectangle(10,10,getSelectedSoldier().getHP() ,8);

		health_bar.setColor(1,0,0);
		hud_back.setColor(0.2f,0.2f,0.2f);
		hud_back.setAlpha(0.5f);
		hud.attachChild(hud_back);
		hud.attachChild(missing_health);
		hud.attachChild(health_bar);
		app.getEngine().getCamera().setHUD(hud);

	}
	private void loadSoldiers(){
		addObject(new Soldier(0, collisionLayer.getTMXTile(0, 0)));
		addObject(new Soldier(0, collisionLayer.getTMXTile(2, 0)));
	}

	private synchronized void selectSoldier(final Soldier pSoldier) {
		if(selectedSoldier != pSoldier)
		{
			if(selectedSoldier != null)
			{
				selectedSoldier.markDeselected();
			}

			selectedSoldier = pSoldier;
			selectedSoldier.markSelected();
			loadHUD();
			sortChildren();
		}
	}
}
