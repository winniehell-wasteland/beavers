package org.beavers.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
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
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.HoldDetector;
import org.anddev.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.astar.AStarPathFinder;
import org.beavers.R;
import org.beavers.Textures;
import org.beavers.communication.Client;
import org.beavers.ingame.GameObject;
import org.beavers.ingame.PathWalker;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.WayPoint;
import org.beavers.ui.ContextMenuHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

public class GameActivity extends BaseGameActivity
	implements ITiledMap<GameObject>,
		IOnSceneTouchListener,
		IHoldDetectorListener,
		IScrollDetectorListener
{
	/**
	 * @name z-index constants
	 * @{
	 */
	public static final int ZINDEX_BACKGROUND = 0;
	public static final int ZINDEX_WAYPOINTS  = ZINDEX_BACKGROUND + 10;
	public static final int ZINDEX_AIMPOINTS  = ZINDEX_WAYPOINTS  + 10;
	public static final int ZINDEX_SOLDIERS   = ZINDEX_AIMPOINTS  + 10;
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

	/**
	 * default constructor
	 * @param pApp
	 */
	public GameActivity()
	{
		super();

		// initialize detectors
		scrollDetector = new SurfaceScrollDetector(10.0f, this);
		holdDetector = new HoldDetector(200, 10.0f, 0.1f, this);

		// initialize game object containers
		gameObjects = new HashMap<TMXTile, GameObject>();
		soldiers = new ArrayList<HashSet<Soldier>>(getTeamCount());

		for(int i = 0; i < getTeamCount(); ++i)
		{
			soldiers.add(new HashSet<Soldier>());
		}
	}

	/**
	 * insert a new game object on the map
	 * @param pObject the object to insert
	 */
	public void addObject(final GameObject pObject)
	{
		gameObjects.put(pObject.getTile(), pObject);
		mainScene.attachChild(pObject);

		mainScene.sortChildren();

		if(pObject instanceof Soldier)
		{
			final Soldier soldier = (Soldier) pObject;
			soldiers.get(soldier.getTeam()).add(soldier);
		}
	}

	public TMXLayer getFloorLayer() {
		return floorLayer;
	}



	public Scene getMainScene() {
		return mainScene;
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
	        final MenuInflater inflater = getMenuInflater();
	        inflater.inflate(contextMenuHandler.getMenuID(), pMenu);

			for(int i = 0; i < pMenu.size(); ++i)
			{
				pMenu.getItem(i).setOnMenuItemClickListener(contextMenuHandler);
			}

			contextMenuHandler.onMenuCreated(pMenu);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
	    final MenuInflater inflater = getMenuInflater();
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
						mainScene.sortChildren();
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
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(GameActivity.this,
									"Selected soldier by waypoint",
									Toast.LENGTH_SHORT).show();
							}
						});
					}
					else
					{
						contextMenuHandler = waypoint;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mRenderSurfaceView.showContextMenu();
							}
						});
					}
				}
			}
			else if(!isTileBlocked(null, tile.getTileColumn(), tile.getTileRow())
					&& (selectedSoldier != null))
			{
				final Path soldierPath =
					selectedSoldier.findPath(getPathFinder(), tile);

				if(soldierPath != null)
				{
					final WayPoint waypoint =
						new WayPoint(selectedSoldier, soldierPath, tile);
					selectedSoldier.addWayPoint(waypoint);
					addObject(waypoint);
				}
			}
		}
	}

	@Override
	public void onLoadComplete() {
		loadMap(currentGame.getMapName());
		loadSoldiers();

		if(map != null)
		{
			pathFinder = new AStarPathFinder<GameObject>(this, 1600, true);
		}
		else
		{
			pathFinder = null;
		}

		mainScene.registerUpdateHandler(holdDetector);
		mainScene.setOnSceneTouchListener(this);

final TimerHandler gameTimer = new TimerHandler(0.3f, new ITimerCallback() {

			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				checkTargets();
			}
		});
		gameTimer.setAutoReset(true);
		getEngine().registerUpdateHandler(gameTimer);

	}

	@Override
	public Engine onLoadEngine() {
        final Display display = getWindowManager().getDefaultDisplay();

        ScreenOrientation orientation;

        final int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
        	orientation = ScreenOrientation.LANDSCAPE;
        } else {
        	orientation = ScreenOrientation.PORTRAIT;
        }

		camera = new SmoothCamera(0, 0, display.getWidth(), display.getHeight(), 2*display.getWidth(), 2*display.getHeight(),0);

		return new Engine(new EngineOptions(true, orientation,
				new RatioResolutionPolicy(display.getWidth(), display.getHeight()), camera));
	}

	@Override
	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		BitmapTextureAtlas textureAtlas;

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.AIM = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "aimpoint.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(16,16,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.MUZZLE_FLASH = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "muzzleflash.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SOLDIER_TEAM0 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "96x96anim.png", 0, 0, 3, 2);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SOLDIER_SELECTION_CIRCLE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "circle.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(4,4,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SHOT_BULLET = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "bullet.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.WAYPOINT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "waypoint.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);
	}

	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		mainScene = new Scene();

        mRenderSurfaceView.setOnCreateContextMenuListener(this);

		return mainScene;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.menu_execute:
			final PathWalker walker = new PathWalker(this, selectedSoldier);
			walker.start();

			//Client.sendDecisions(this, currentGame, new DecisionContainer());

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
		camera.offsetCenter(-pDistanceX*CAMERA_SPEED, -pDistanceY*CAMERA_SPEED);
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

		mainScene.detachChild(pObject);
	}

	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		if(!getIntent().hasExtra(getApplicationInfo().packageName + "game"))
		{
			Log.e(TAG, "no extra");
			return;
		}

		currentGame = getIntent().getParcelableExtra(
			getApplicationInfo().packageName + "game");

		// don't show game if we have nothing to show
		if(currentGame == null)
		{
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(updateReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(updateReceiver, new IntentFilter(Client.GAME_STATE_CHANGED_INTENT));
	}

	private final static float CAMERA_SPEED = 1.50f;
	private static final String TAG = "GameScene";

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

	/**
	 * @name scenery
	 * @{
	 */
	private SmoothCamera camera;
	private HUD hud;
	private Scene mainScene;
	/**
	 * @}
	 */

	/**
	 * @name active entities
	 * @{
	 */
	private ContextMenuHandler contextMenuHandler;
	private Soldier selectedSoldier;
	/**
	 * @}
	 */

	private Line parallelA,parallelB,lineA,lineB;

	private GameInfo currentGame;
	private AStarPathFinder<GameObject> pathFinder;

	private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context pContext, final Intent pIntent) {
			final GameInfo game = pIntent.getParcelableExtra("game");

			// TODO update GameActivity
		}
	};

	private void loadMap(final String pMapName)
	{
		try {
			final TMXLoader tmxLoader = new TMXLoader(this, getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {

				}
			});
			map = tmxLoader.loadFromAsset(this, "tmx/"+pMapName+".tmx");
		} catch (final TMXLoadException tmxle) {
			Debug.e(tmxle);
			return;
		}

		collisionLayer = map.getTMXLayers().get(0);
		floorLayer =map.getTMXLayers().get(1);
		mainScene.attachChild(collisionLayer);
		mainScene.attachChild(floorLayer);
	}

	private void loadHUD(){
		hud=new HUD();
		hud.setPosition(0, 0);
        getSelectedSoldier().changeHP(-10);
		final Rectangle hud_back = new Rectangle(0,0, camera.getWidth(),30);
		final Rectangle missing_health = new Rectangle(10,10,100,8);
		final Rectangle health_bar = new Rectangle(10,10,getSelectedSoldier().getHP() ,8);

		health_bar.setColor(1,0,0);
		hud_back.setColor(0.2f,0.2f,0.2f);
		hud_back.setAlpha(0.5f);
		hud.attachChild(hud_back);
		hud.attachChild(missing_health);
		hud.attachChild(health_bar);
		camera.setHUD(hud);

	}
	private void loadSoldiers(){
		addObject(new Soldier(0,collisionLayer, collisionLayer.getTMXTile(0, 0)));
		addObject(new Soldier(0,collisionLayer, collisionLayer.getTMXTile(2, 0)));
	}

	public void checkTargets(){
		final Iterator itr=getSoldiers(0).iterator();
		while(itr.hasNext()){
			final Soldier s=(Soldier)itr.next();
			final Iterator itr2=getSoldiers(0).iterator();
			while(itr2.hasNext()){
				final Soldier t=(Soldier)itr2.next();
				if(s!=t){
					/* parallelA = new Line(
							t.getCenter()[0]-(s.getLineA().getX1()-s.getLineA().getX2()),
							t.getCenter()[1]-(s.getLineA().getY1()-s.getLineA().getY2()),
							t.getCenter()[0]+(s.getLineA().getX1()-s.getLineA().getX2()),
							t.getCenter()[1]+(s.getLineA().getY1()-s.getLineA().getY2())
							);*/
					parallelB = new Line(
							t.getCenter()[0]-(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[0]),
							t.getCenter()[1]-(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[1]),
							t.getCenter()[0]+(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[0]),
							t.getCenter()[1]+(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[1])
							);

					parallelA = new Line(
							t.getCenter()[0]-(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[0]),
							t.getCenter()[1]-(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[1]),
							t.getCenter()[0]+(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[0]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[0]),
							t.getCenter()[1]+(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[1]-s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[1])
							);

					lineB=new Line(s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[0],
							s.convertLocalToSceneCoordinates(s.getLineB().getX1(),s.getLineB().getY1())[1],
							s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[0],
							s.convertLocalToSceneCoordinates(s.getLineB().getX2(),s.getLineB().getY2())[1]);

					lineA=new Line(s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[0],
							s.convertLocalToSceneCoordinates(s.getLineA().getX1(),s.getLineA().getY1())[1],
							s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[0],
							s.convertLocalToSceneCoordinates(s.getLineA().getX2(),s.getLineA().getY2())[1]);


					if(parallelA.collidesWith(lineB) && parallelB.collidesWith(lineA)){

							s.fireShot(t.getTile(), this);

					}

				}
			}
		}
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
			mainScene.sortChildren();
		}
	}
}
