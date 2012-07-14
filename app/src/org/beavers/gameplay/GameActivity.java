package org.beavers.gameplay;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.HoldDetector;
import org.anddev.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.IWeightedPathFinder;
import org.anddev.andengine.util.path.astar.AStarPathFinder;
import org.beavers.App;
import org.beavers.R;
import org.beavers.Settings;
import org.beavers.Textures;
import org.beavers.communication.Client;
import org.beavers.communication.Server;
import org.beavers.ingame.IGameObject;
import org.beavers.ingame.IMovableObject;
import org.beavers.ingame.IRemoveObjectListener;
import org.beavers.ingame.PathWalker;
import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;
import org.beavers.storage.CustomGSON;
import org.beavers.storage.GameStorage;
import org.beavers.storage.GameStorage.UnexpectedTileContentException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

/**
 * Activity for game display
 *
 * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class GameActivity extends BaseGameActivity
	implements ITiledMap<IMovableObject>,
		IOnSceneTouchListener,
		IHoldDetectorListener,
		IScrollDetectorListener,
		IRemoveObjectListener
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
	 * default constructor
	 */
	public GameActivity()
	{
		super();

		// initialize communication services
		client = new Client.Connection();
		server = new Server.Connection();

		// initialize detectors
		scrollDetector = new SurfaceScrollDetector(10.0f, this);
		holdDetector = new HoldDetector(200, 10.0f, 0.1f, this);
	}

	public Scene getMainScene() {
		return mainScene;
	}

	public TMXTiledMap getMap() {
		return map;
	}

	public IWeightedPathFinder<IMovableObject> getPathFinder() {
		return pathFinder;
	}

	public Soldier getSelectedSoldier() {
		return selectedSoldier;
	}

	@Override
	public float getStepCost(final IMovableObject pObject,
		final int pFromTileColumn, final int pFromTileRow,
		final int pToTileColumn, final int pToTileRow) {

		return pObject.getStepCost(this,
			new Tile(pFromTileColumn, pFromTileRow),
			new Tile(pToTileColumn, pToTileRow));
	}

	public GameStorage getStorage() {
		return storage;
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
	public boolean isTileBlocked(final IMovableObject pObject,
	                             final int pTileColumn, final int pTileRow) {
		if((pTileColumn < 0) || (pTileColumn >= getTileColumns())
		   || (pTileRow < 0) || (pTileRow >= getTileRows()))
		{
			return true;
		}

		final TMXTile tile = collisionLayer.getTMXTile(pTileColumn, pTileRow);

		return ((tile == null) || (tile.getGlobalTileID()!=0));
	}

	@Override
	public void onCreateContextMenu(final ContextMenu pMenu, final View pView,
			final ContextMenuInfo pInfo) {

		if(selectedWaypoint != null)
		{
	        final MenuInflater inflater = getMenuInflater();
	        inflater.inflate(selectedWaypoint.getMenuID(), pMenu);

			for(int i = 0; i < pMenu.size(); ++i)
			{
				pMenu.getItem(i).setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(final MenuItem pItem) {
						return selectedWaypoint.onMenuItemClick(GameActivity.this, pItem);
					}

				});
			}

			selectedWaypoint.onMenuCreated(pMenu);
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

		final Tile tile = Tile.fromCoordinates(pHoldX, pHoldY);

		if(tile != null)
		{
			Log.d(TAG, "Hold on tile ("+tile.getColumn()+","+tile.getRow()+")");

			// create new Aim if necessary
			if(selectedWaypoint instanceof WayPoint)
			{
				final WayPoint selectedWayPoint = selectedWaypoint;

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

			selectedWaypoint = null;

			// there is an something on the tile
			if(storage.isTileOccupied(tile))
			{
				try {
					if(storage.hasSoldierOnTile(tile))
					{
						final Soldier soldier = storage.getSoldierByTile(tile);

						if(soldier.equals(selectedSoldier))
						{
							selectedWaypoint = soldier.getFirstWaypoint();
						}
						else
						{
							selectSoldier(soldier);
						}
					}
					else if(storage.hasWaypointOnTile(tile))
					{
						final WayPoint waypoint =
							storage.getWaypointByTile(tile);

						if(!waypoint.getSoldier().equals(selectedSoldier))
						{
							selectSoldier(waypoint.getSoldier());
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(GameActivity.this,
										"Selected soldier by waypoint...",
										Toast.LENGTH_SHORT).show();
								}
							});
						}
						else
						{
							selectedWaypoint = waypoint;
						}
					}
				} catch (final UnexpectedTileContentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(selectedWaypoint != null)
				{
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mRenderSurfaceView.showContextMenu();
						}
					});
				}
			}
			else if(!isTileBlocked(null, tile.getColumn(), tile.getRow())
					&& (selectedSoldier != null))
			{
				final WayPoint waypoint =
					selectedSoldier.addWayPoint(getPathFinder(), tile);
				if(waypoint == null){

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(GameActivity.this,
								"No AP left",
								Toast.LENGTH_SHORT).show();
						}
					});
					}
				else{
					try {
						storage.addWaypoint(waypoint);
					} catch (final UnexpectedTileContentException e) {
						Log.e(TAG, "Could not add waypoint to storage!", e);
					}

					mainScene.attachChild(waypoint);

					selectedSoldier.changeAP(-waypoint.getPath().getCost()/10);
				}
			}
			else
			{
				Log.e(TAG, "no obj");
			}
		}
	}

	@Override
	public void onLoadComplete() {
		final TimerHandler gameTimer =
			new TimerHandler(0.2f, new ITimerCallback() {

				@Override
				public void onTimePassed(final TimerHandler pTimerHandler) {
					checkTargets(0,1);
					checkTargets(1,0);

					updateHUD();
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

		textureAtlas = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mfont =new Font(textureAtlas,Typeface.create(Typeface.DEFAULT,Typeface.BOLD),16,true,Color.WHITE);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		bluefont =new Font(textureAtlas,Typeface.create(Typeface.DEFAULT,Typeface.BOLD),16,true,Color.BLUE);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		redfont =new Font(textureAtlas,Typeface.create(Typeface.DEFAULT,Typeface.BOLD),16,true,Color.RED);
		getTextureManager().loadTexture(textureAtlas);

		getFontManager().loadFonts(mfont,bluefont,redfont);

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.AIM = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "aimpoint.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(16,16,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.MUZZLE_FLASH = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "muzzleflash.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SOLDIER_TEAM0 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "96x96blue.png", 0, 0, 3, 2);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SOLDIER_TEAM1 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "96x96red.png", 0, 0, 3, 2);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SOLDIER_SELECTION_CIRCLE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "circle.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(32,4,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SHOT_BULLET = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "arrow.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.WAYPOINT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "waypoint.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);
	}

	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		mainScene = new Scene();

		loadMap(currentGame.getMapName());

		if(map == null)
		{
			Log.e(TAG, "Map not initialized!");
			finish();
		}

		pathFinder = new AStarPathFinder<IMovableObject>(this, getTileColumns()*getTileRows(), true);

		camera.setBounds(0, map.getTileColumns() * map.getTileWidth(),
		                 0, map.getTileRows() * map.getTileHeight());
		camera.setBoundsEnabled(true);

		mainScene.registerUpdateHandler(holdDetector);
		mainScene.setOnSceneTouchListener(this);

        mRenderSurfaceView.setOnCreateContextMenuListener(this);

		// initialize storage
		try {
			storage = new GameStorage(this, currentGame);
			storage.setRemoveObjectListener(this);
		} catch (final Exception e) {
			Log.e(TAG, "Could not create game storage!", e);
			finish();
		}

		// show soldiers on mainScene
		for(int team = 0; team < storage.getTeamCount(); ++team)
		{
			for(final Soldier soldier : storage.getSoldiersByTeam(team))
			{
				mainScene.attachChild(soldier);
			}
		}

		return mainScene;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.menu_execute:

			final HashSet<Soldier> mySoldiers =  storage.getSoldiersByTeam(
				currentGame.getTeam(getSettings().getPlayer())
			);

			try {
				final Gson gson = CustomGSON.getInstance();
				client.getService().sendDecisions(currentGame,
				                                  gson.toJson(mySoldiers));
			} catch (final RemoteException e) {
				Log.e(TAG, getString(R.string.error_sending_decisions_failed), e);
				return true;
			}

			// deselect soldier
			selectSoldier(null);

			// disable user interaction
			holdDetector.setEnabled(false);

			final ExecutorService executor = Executors.newCachedThreadPool();

			for(int team = 0; team < storage.getTeamCount(); ++team) {
				for(final Soldier soldier : storage.getSoldiersByTeam(team)) {
					for(final WayPoint waypoint : soldier.getWaypoints())
					{
						mainScene.attachChild(waypoint);
					}

					executor.execute(new Runnable() {

						@Override
						public void run() {
							final PathWalker walker = new PathWalker(GameActivity.this, soldier);
							walker.start();
						}
					});
				}
			}

			return true;
		case R.id.menu_reset_hold_detector:

			holdDetector.setEnabled(true);

			return true;
		default:
			return false;
		}
	}

	@Override
	public void onRemoveObject(final IGameObject pObject) {
		if(pObject == null)
		{
			// ignore
			return;
		}

		if(pObject instanceof Soldier)
		{
			final Soldier soldier = (Soldier) pObject;

			try {
				storage.removeSoldier(soldier);
			} catch (final UnexpectedTileContentException e) {
				Log.e(TAG, "Could not remove soldier from storage!", e);
			}

			if(soldier.equals(selectedSoldier))
			{
				updateHUD();
				selectedSoldier = null;
			}
		}
		else if(pObject instanceof WayPoint)
		{
			final WayPoint waypoint = (WayPoint) pObject;

			try {
				storage.removeWaypoint(waypoint);
			} catch (final UnexpectedTileContentException e) {
				Log.e(TAG, "Could not remove waypoint from game storage!", e);
			}

			if(waypoint.equals(selectedWaypoint))
			{
				selectedWaypoint = null;
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
		camera.offsetCenter(-pDistanceX*CAMERA_SPEED, -pDistanceY*CAMERA_SPEED);

	}

	@Override
	public void onTileVisitedByPathFinder(final int pTileColumn, final int pTileRow) {

	}

	public void playOutcome(final OutcomeContainer outcome)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		Intent intent = new Intent(GameActivity.this, Client.class);
		if(!bindService(intent, client, Service.BIND_AUTO_CREATE))
		{
			Log.e(TAG, getString(R.string.error_binding_client_failed));
			return;
		}

		intent = new Intent(GameActivity.this, Server.class);
		if(!bindService(intent, server, Service.BIND_AUTO_CREATE))
		{
			Log.e(TAG, getString(R.string.error_binding_server_failed));
			return;
		}

		currentGame = getIntent().getParcelableExtra(GameInfo.PARCEL_NAME);

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

	/**
	 * @name constants
	 * @{
	 */
	private final static float CAMERA_SPEED = 1.50f;
	/**
	 * @}
	 */

	/**
	 * @name debug
	 * @{
	 */
	private static final String TAG = "GameScene";
	/**
	 * @}
	 */

	/**
	 * @name communication
	 * @{
	 */
	private final Client.Connection client;
	private final Server.Connection server;
	/**
	 * @}
	 */

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

	/** game object container */
	private GameStorage storage;

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
	private Soldier selectedSoldier;
	private WayPoint selectedWaypoint;
	/**
	 * @}
	 */
	private Font mfont,bluefont,redfont;

	private Line parallelA,parallelB,lineA,lineB;

	private GameInfo currentGame;
	private AStarPathFinder<IMovableObject> pathFinder;

	private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context pContext, final Intent pIntent) {
			final GameInfo game =
				pIntent.getParcelableExtra(GameInfo.PARCEL_NAME);

			// TODO update GameActivity
		}
	};

	/** @return application settings */
	private Settings getSettings()
	{
		if(getApplication() instanceof App)
		{
			return ((App) getApplication()).getSettings();
		}
		else
		{
			return null;
		}
	}

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
		floorLayer = map.getTMXLayers().get(1);

		mainScene.attachChild(collisionLayer);
		mainScene.attachChild(floorLayer);
	}

	private void updateHUD(){
		if(getSelectedSoldier() == null)
		{
			try {
				camera.setHUD(null);
			} catch(final NullPointerException e)
			{
				// ignore
			}

			return;
		}

		if(hud == null)
		{
			hud=new HUD();
			hud.setPosition(0, 0);

			final Rectangle hud_back = new Rectangle(0,0, camera.getWidth(),30);
			hud_back.setColor(0.2f,0.2f,0.2f);
			hud_back.setAlpha(0.5f);
			hud.attachChild(hud_back);

			final Rectangle missing_health = new Rectangle(10,10,100,8);
			hud.attachChild(missing_health);

			health_bar = new Rectangle(10,10,getSelectedSoldier().getHP() ,8);
			health_bar.setColor(1,0,0);
			hud.attachChild(health_bar);

			separator = new Text(0 ,0, mfont,":" );
			separator.setPosition(getEngine().getCamera().getWidth()/2-2,4);
			hud.attachChild(separator);
		}
		else
		{
			apText.detachSelf();
			blueText.detachSelf();
			redText.detachSelf();
		}

		health_bar.setWidth(getSelectedSoldier().getHP());

		apText = new Text(0 ,0, mfont,
		                  "AP " + getSelectedSoldier().getAP() + "/" +
		                  getSelectedSoldier().getmaxAP());
		apText.setPosition(getEngine().getCamera().getWidth()
		                   - apText.getWidth()-5, 4);
		hud.attachChild(apText);


		blueText=new Text(0 ,0, bluefont, "" + storage.getSoldiersByTeam(0).size() );
		blueText.setPosition(getEngine().getCamera().getWidth()/2-blueText.getWidth()-5, 4);
		hud.attachChild(blueText);

		redText=new Text(0 ,0, redfont, "" + storage.getSoldiersByTeam(1).size() );
		redText.setPosition(getEngine().getCamera().getWidth()/2+5, 4);
		hud.attachChild(redText);

		camera.setHUD(hud);
		//mainScene.sortChildren();
	}

	private Rectangle health_bar;
	private Text apText,redText,blueText, separator;

	/**
	 *
	 * @param attacking das angreifende Team
	 * @param targets das angegriffene Team
	 */
	private void checkTargets(final int attacking, final int targets){
		final Iterator<Soldier> itr = storage.getSoldiersByTeam(attacking).iterator();
		while(itr.hasNext()){  //durchläuft Liste des ersten Teams
			final Soldier s=itr.next();

			final Iterator<Soldier> itr2= storage.getSoldiersByTeam(targets).iterator();

			if(s.isShooting())
			{
				if(s.getShot().findPath(getPathFinder(), s.getShot().getTarget().getTile()) == null)
				{
					s.getShot().stopShooting();
				}
			}

			if(!s.isShooting() && !s.getIgnore()){   //Abbruch, falls der Soldat schon schießt
				while(itr2.hasNext()){  //durchläuft Liste des zweiten Teams

					final Soldier t=itr2.next();
					if(!t.isDead() && !s.isDead()){  //beide Soldaten müssen noch leben
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


						if(parallelA.collidesWith(lineB) && parallelB.collidesWith(lineA)){ //Soldat des zweiten Teams ist im Sichtbereich

							s.fireShot(t, this);
						}
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

			if(selectedSoldier != null)
			{
				selectedSoldier.markSelected();
			}

			updateHUD();
		}
	}

}
