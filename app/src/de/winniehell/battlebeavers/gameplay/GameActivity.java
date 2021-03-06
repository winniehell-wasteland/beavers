/*
	(c) winniehell, wintermadnezz (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers.gameplay;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.FixedResolutionPolicy;
import org.anddev.andengine.engine.options.resolutionpolicy.IResolutionPolicy;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXProperties;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTileProperty;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.TSXLoader;
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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Toast;
import de.winniehell.battlebeavers.App;
import de.winniehell.battlebeavers.R;
import de.winniehell.battlebeavers.Settings;
import de.winniehell.battlebeavers.Textures;
import de.winniehell.battlebeavers.communication.Client;
import de.winniehell.battlebeavers.communication.Client.ClientRemoteException;
import de.winniehell.battlebeavers.communication.Server;
import de.winniehell.battlebeavers.ingame.IMenuDialogListener;
import de.winniehell.battlebeavers.ingame.IMovableObject;
import de.winniehell.battlebeavers.ingame.ISoldierListener;
import de.winniehell.battlebeavers.ingame.Soldier;
import de.winniehell.battlebeavers.ingame.Tile;
import de.winniehell.battlebeavers.ingame.WaitTimeDialog;
import de.winniehell.battlebeavers.ingame.WayPoint;
import de.winniehell.battlebeavers.storage.Event;
import de.winniehell.battlebeavers.storage.Event.HPEvent;
import de.winniehell.battlebeavers.storage.Event.ShootEvent;
import de.winniehell.battlebeavers.storage.GameStorage;
import de.winniehell.battlebeavers.storage.GameStorage.UnexpectedTileContentException;
import de.winniehell.battlebeavers.storage.Outcome;
import de.winniehell.battlebeavers.storage.SoldierList;

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
		ISoldierListener,
		IMenuDialogListener
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
	public boolean onCreateOptionsMenu(final Menu pMenu) {
	    final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_menu, pMenu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu pMenu) {
		return currentGame.isInState(this, GameState.PLANNING_PHASE);
	}
	
	@Override
	public void onDialogSelected(final WayPoint waypoint) {
		new WaitTimeDialog(this, waypoint);

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
			Log.d(TAG, "Hold on tile "+tile);

			// create new Aim if necessary
			if((selectedWaypoint != null)
			   && selectedWaypoint.isWaitingForAim())
			{
				if(!selectedWaypoint.getTile().equals(tile))
				{
					selectedWaypoint.setAim(tile);
					mainScene.sortChildren();
				}

				return;
			}

			selectedWaypoint = null;

			GameInfo info;
			try {
				info = GameInfo.fromFile(this, currentGame);
			} catch (final FileNotFoundException e) {
				Log.e(TAG, "Could not load game info!", e);
				return;
			}
			
			// soldier on the tile
			if(storage.hasSoldierOnTile(info.getTeam(), tile))
			{
				try {
					final Soldier soldier = storage.getSoldierByTile(info.getTeam(), tile);

					if(soldier.equals(selectedSoldier))
					{
						selectedWaypoint = soldier.getFirstWaypoint();
					}
					else
					{
						selectSoldier(soldier);
					}
				} catch (final UnexpectedTileContentException e) {
					Log.e(TAG, e.getMessage());
				}
			}
			else if(storage.hasWaypointOnTile(selectedSoldier, tile))
			{
				try {
					final WayPoint waypoint =
						storage.getWaypointByTile(selectedSoldier, tile);

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
				} catch (final UnexpectedTileContentException e) {
					Log.e(TAG, e.getMessage());
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
								"Not enough AP left!",
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

					selectedSoldier.changeAP(-waypoint.getPath().getCost());
				}
			}
			else
			{
				Log.e(TAG, "no obj");
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
	}

	@Override
	public void onLoadComplete() {
		handleState();
	}

	@Override
	public Engine onLoadEngine() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        final Configuration configuration = getResources().getConfiguration();

        ScreenOrientation orientation;
        
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        	orientation = ScreenOrientation.PORTRAIT;
        } else {
        	orientation = ScreenOrientation.LANDSCAPE;
        }

		camera = new SmoothCamera(0, 0, metrics.widthPixels, metrics.heightPixels,
			100*metrics.widthPixels, 100*metrics.heightPixels, 0
		);

		final IResolutionPolicy policy =
			new FixedResolutionPolicy(metrics.widthPixels, metrics.heightPixels);

		return new Engine(
			new EngineOptions(true, orientation, policy, camera)
		);
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
		// initialize storage
		try {
			storage = new GameStorage(this, currentGame);
			storage.setPositionListener(this);
			storage.setMenuDialogListener(this);

		} catch (final Exception e) {
			Log.e(TAG, "Could not create game storage!", e);
			finish();
		}

		mEngine.registerUpdateHandler(new FPSLogger());
		mainScene = new Scene();

		loadMap();

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

		// show soldiers on mainScene
		for(int team = 0; team < getSettings().getMaxPlayers(); ++team)
		{
			for(final Soldier soldier : storage.getSoldiersByTeam(team))
			{
				mainScene.attachChild(soldier);
			}
		}

		return mainScene;
	}
	
	@Override
	public void onChange(final Soldier pSoldier) {
		if(pSoldier==null)return;
		if(pSoldier.equals(selectedSoldier))
		{
			updateHUD();
		}
	}
	
	@Override
	public void onDeath(final Soldier pSoldier) {
		if(pSoldier == null)
		{
			// ignore
			return;
		}

		
			try {
				storage.removeSoldier(pSoldier);
			} catch (final UnexpectedTileContentException e) {
				Log.e(TAG, "Could not remove soldier from storage!", e);
			}

			if(pSoldier.equals(selectedSoldier))
			{
				updateHUD();
				selectedSoldier = null;
			}
		
		
		
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.menu_execute:
		{
			try {
				final int team = getInfo().getTeam();

				currentGame.saveDecisions(
					this, team, storage.getSoldiersByTeam(team)
				);
			} catch (final IOException e) {
				Log.e(TAG,
					getString(R.string.error_game_write_decisions, currentGame),
					e
				);
				return true;
			}

			try {
				client.getService().sendDecisions(currentGame);
			} catch (final RemoteException e) {
				((ClientRemoteException)e).log();
				return true;
			}

			// deselect soldier
			selectSoldier(null);

			// disable user interaction
			holdDetector.setEnabled(false);

			return true;
		}
		default:
			return false;
		}
	}

	public void onOutcomeFinished() {
		Log.d(TAG, "Outcome finished...");
		
		if(!currentGame.isInState(this, GameState.EXECUTION_PHASE,GameState.PLANNING_PHASE)) {
			Log.e(TAG, getString(R.string.error_game_wrong_state, currentGame, currentGame.getState(this)));
			return;
		}
		Log.e(TAG, "Outcome REALY finished");
		if(storage.getSoldiersByTeam(1 - getInfo().getTeam()).size() == 0) {
			try {
				currentGame.setState(this, GameState.WON);
			} catch (final IOException e) {
				Log.e(TAG, getString(R.string.error_game_save_state, currentGame), e);
			}
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(GameActivity.this, "You won! :-)", Toast.LENGTH_LONG).show();
				}
			});
		}
		else if(storage.getSoldiersByTeam(getInfo().getTeam()).size() == 0) {
			try {
				currentGame.setState(this, GameState.LOST);
			} catch (final IOException e) {
				Log.e(TAG, getString(R.string.error_game_save_state, currentGame), e);
			}

			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(GameActivity.this, "You lost! :-(", Toast.LENGTH_LONG).show();
				}
			});
		}
		else {
			Log.d(TAG, "Game not finished yet!");
		}

		if(currentGame.isServer(getSettings().getPlayer())) {
			//outcome.printEvents();

			try {
				for(int team = 0; team < getSettings().getMaxPlayers(); ++team) {
					final SoldierList decisions =
						currentGame.getDecisions(this, team);
					outcome.addDecisions(decisions);
					currentGame.deleteDecisions(this, team);
				}
				
				currentGame.saveOutcome(this, outcome);
			} catch (final IOException e) {
				Log.e(TAG, "Could not save outcome!", e);
				return;
			}

			try {
				server.getService().distributeOutcome(currentGame);
			} catch (final RemoteException e) {
				Log.e(TAG, "Could not distribute outcome!", e);
			}
		}
		else {
			outcomeTimer = null;
			currentGame.deleteOutcome(this);
			handleState();
			Log.w("","handlestate");
		}
	}

	public void onSoldierStopped() {
		--activeSoldiers;
		
		if(activeSoldiers <= 0) {
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					onOutcomeFinished();
				}
			}, 5000);
		}
		else
		{
			Log.d(TAG, activeSoldiers+" left");
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

	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

		Intent intent = new Intent(GameActivity.this, Client.class);
		if(!bindService(intent, client, Service.BIND_AUTO_CREATE))
		{
			Log.e(TAG, getString(R.string.error_binding_client));
			return;
		}

		intent = new Intent(GameActivity.this, Server.class);
		if(!bindService(intent, server, Service.BIND_AUTO_CREATE))
		{
			Log.e(TAG, getString(R.string.error_binding_server));
			return;
		}

		currentGame = getIntent().getParcelableExtra(Game.PARCEL_NAME);

		// don't show game if we have nothing to show
		if(currentGame == null)
		{
			Log.e(TAG, "Current game is empty!");
			finish();
		}
		
		updateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context pContext, final Intent pIntent) {
				final Game game =
					pIntent.getParcelableExtra(Game.PARCEL_NAME);

				if(game.equals(currentGame)) {
					handleState();
				}
			}
		};

		outcome_executor = Executors.newCachedThreadPool();
	}

	@Override
	protected void onDestroy() {
		unbindService(client);
		unbindService(server);

		outcome_executor.shutdownNow();

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(updateReceiver);

		try {
			if(storage != null) {
				storage.saveToFile();
			}
		} catch (final FileNotFoundException e) {
			Log.e(TAG, "Could not write game storage!", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateReceiver.setDebugUnregister(true);
		registerReceiver(updateReceiver, new IntentFilter(Game.STATE_CHANGED_INTENT));
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
	
	/** outcome container */
	private Outcome outcome;

	private int activeSoldiers;
	
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

	private Game currentGame;
	private AStarPathFinder<IMovableObject> pathFinder;

	ExecutorService outcome_executor;

	private BroadcastReceiver updateReceiver;

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

	private void loadMap()
	{
		try {
			final TMXLoader tmxLoader = new TMXLoader(this, getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile, final TMXProperties<TMXTileProperty> pTMXTileProperties) {

				}
			});

			final String mapPath = "maps/" +getInfo().getMapName() + "/";

			TMXLoader.setAssetBasePath(mapPath);
			TSXLoader.setAssetBasePath(mapPath);

			map = tmxLoader.loadFromAsset(this, "map.tmx");

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
	private TimerHandler outcomeTimer;

	/**
	 *
	 * @param attacking das angreifende Team
	 * @param targets das angegriffene Team
	 */
	private void checkTargets(final int attacking, final int targets){
		//timer controls attacks in replay phase
		//if(currentGame.getState(this).getResId()==R.string.state_replay)return;

		final Iterator<Soldier> itr = storage.getSoldiersByTeam(attacking).iterator();
		while(itr.hasNext()){  //durchläuft Liste des ersten Teams
			final Soldier s=itr.next();

			final Iterator<Soldier> itr2= storage.getSoldiersByTeam(targets).iterator();

			// stop shooting if we can not see the target anymore
			if(s.isAttacking())
			{
				if(!s.getAttack().hasPath())
				{
					s.stopAttacking();
				}
			}

			if(!s.isAttacking() && !s.isIgnoringShots()){   //Abbruch, falls der Soldat schon schießt
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

							s.attack(t, this);
						}
					}
				}
			}
		}
	}

	private void handleState() {
		Log.d(TAG, "handleState() "+currentGame.getName()+" "+currentGame.getState(this));
		
		// planning phase
		if(currentGame.isInState(this, GameState.PLANNING_PHASE)) {
			Log.w("outcome","in planning phase");
			if(currentGame.hasDecisions(this, getInfo().getTeam())) {
				Toast.makeText(this, "Waiting for outcome!", Toast.LENGTH_LONG).show();
			}
			// client is still in execution phase
			else if(currentGame.hasOutcome(this)
			        && !currentGame.isServer(getSettings().getPlayer())) {
				Log.w("GameActivity","play outcome a");
				playOutcome();
			}
			else {
				Log.w("outcome","enable hold detector");
				holdDetector.setEnabled(true);
			}
		}
		else if(currentGame.isInState(this, GameState.EXECUTION_PHASE)) {
			// execution phase on server
			if(currentGame.isServer(getSettings().getPlayer())) {
				if(hasAllDecisions()) {
					recordOutcome();
				}
			}
			// execution phase on client
			else if(currentGame.hasOutcome(this)) {
				Log.w("GameActivity","play outcome b");
				playOutcome();
			}
		}
		else {
			Log.e(TAG, getString(R.string.error_game_wrong_state, currentGame, currentGame.getState(this)));
		}
	}

	private GameInfo getInfo() {
		try {
			return GameInfo.fromFile(this, currentGame);
		} catch (final FileNotFoundException e) {
			Log.e(TAG, "Could not load game info!", e);
			finish();
			return null;
		}
	}

	private boolean hasAllDecisions() {
		for(int team = 0; team < getSettings().getMaxPlayers(); ++team) {
			if(!currentGame.hasDecisions(this, team)) {
				return false;
			}
		}

		return true;
	}

	private synchronized void playOutcome()
	{
		if(outcomeTimer != null) {
			// already playing
			return;
		}
		
		try {
			outcome = currentGame.getOutcome(this);
		} catch (final IOException e) {
			Log.e(TAG, getString(R.string.error_game_load_outcome, currentGame), e);
			return;
		}

		try {
			storage.removeSoldiers();

			// load soldiers
			for(int team = 0; team < getSettings().getMaxPlayers(); ++team) {
				final SoldierList decisions =
					outcome.getDecisions(team);

				for(final Soldier soldier : decisions) {
					soldier.setSimulation(true);
					mainScene.attachChild(soldier);
					storage.addSoldier(soldier);
				}
			}
		} catch(final Exception e) {
			Log.e(TAG, getString(R.string.error_game_load_outcome, currentGame), e);
			return;
		}
		
		if(!outcome.getEventList().isEmpty()) {
		
		

		outcomeTimer =
			new TimerHandler(((float)outcome.getEventList().get(0).getTimestamp())/1000, new ITimerCallback() {

				@Override
				public void onTimePassed(final TimerHandler pTimerHandler) {
					Log.e("replay","time"+pTimerHandler.getTimerSeconds());
					final Event event=outcome.getEventList().remove(0);

					if(event instanceof HPEvent){
						
						final HPEvent hpEvent = (HPEvent)event;
						Log.e("replay","hp");
						storage.getSoldierById(hpEvent.getSoldier()).changeHP(hpEvent.getHp());
					}
					else if(event instanceof ShootEvent){
						final ShootEvent shootEvent = (ShootEvent)event;
						Log.e("replay","shot");
						storage.getSoldierById(shootEvent.getSoldier()).attack(
							storage.getSoldierById(shootEvent.getTarget()), GameActivity.this
						);
					}

					if(!outcome.getEventList().isEmpty()){
						final float time = outcome.getEventList().get(0).getTimestamp() - event.getTimestamp();
						pTimerHandler.setTimerSeconds(time/1000);
						pTimerHandler.reset();
					}
				}

		});
		
		getEngine().registerUpdateHandler(outcomeTimer);
		}
		startSoldierMovement();
	}

	private void recordOutcome() {
		outcome = new Outcome(System.currentTimeMillis());
		storage.setGameEventsListener(outcome);

		try {
			storage.removeSoldiers();

			// load soldiers
			for(int team = 0; team < getSettings().getMaxPlayers(); ++team) {
				final SoldierList decisions =
					currentGame.getDecisions(this, team);
				for(final Soldier soldier : decisions) {
					mainScene.attachChild(soldier);
					storage.addSoldier(soldier);
				}
			}
		} catch(final Exception e) {
			Log.e(TAG, "Could not load decisions!", e);
			return;
		}
		
		startTargetChecking();
		startSoldierMovement();
	}

	private void startSoldierMovement() {
		activeSoldiers = 0;
		
		for(int team = 0; team < getSettings().getMaxPlayers(); ++team) {
			for(final Soldier soldier : storage.getSoldiersByTeam(team)) {
				/*
				for(final WayPoint waypoint : soldier.getWaypoints())
				{
					mainScene.attachChild(waypoint);
				}
				*/

				++activeSoldiers;
				outcome_executor.execute(new Runnable() {

					@Override
					public void run() {
						soldier.startWalking(GameActivity.this);
					}
				});
			}
		}
	}

	private void startTargetChecking() {
		final TimerHandler checkTargetTimer =
			new TimerHandler(0.2f, new ITimerCallback() {

				@Override
				public void onTimePassed(final TimerHandler pTimerHandler) {
					checkTargets(0,1);
					checkTargets(1,0);

					updateHUD();
					
					if(activeSoldiers > 0) {
						pTimerHandler.reset();
					}
				}
		});

		getEngine().registerUpdateHandler(checkTargetTimer);
		
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
