package org.beavers;

import java.util.UUID;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.view.RenderSurfaceView;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.beavers.communication.Client;
import org.beavers.communication.CustomDTNClient;
import org.beavers.communication.CustomDTNDataHandler;
import org.beavers.communication.Server;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.GameScene;
import org.beavers.gameplay.PlayerID;
import org.beavers.ui.GameListView;

import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import de.tubs.ibr.dtn.api.DTNClient.Session;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionDestroyedException;

public class AppActivity extends BaseGameActivity {

	public AppActivity()
	{
		playerID = new PlayerID(UUID.randomUUID().toString());

		client = new Client(this);
		server = new Server(this);
	}

	@Override
	protected void onCreate(final Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		dtnClient = new CustomDTNClient(this);
		dtnDataHandler = new CustomDTNDataHandler(dtnClient, server, client);

        dtnClient.setDataHandler(dtnDataHandler);

        try {
        	final Registration registration = new Registration("game/server");

        	registration.add(Server.GROUP_EID);
        	registration.add(Client.GROUP_EID);

			dtnClient.initialize(registration);
		} catch (final ServiceNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu (final Menu menu) {
		menu.clear();

		if(isShowing(mRenderSurfaceView))
		{
			return gameScene.onCreateOptionsMenu(menu);
		}
		else
		{
		    final MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.main_menu, menu);

		    return true;
		}
	}

	@Override
	protected void onDestroy() {
		// unregister at the daemon
		dtnClient.unregister();

		dtnDataHandler.stop();

		// destroy DTN client
		dtnClient.terminate();

		super.onDestroy();
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

		BitmapTextureAtlas textureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SOLDIER_TEAM0 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, this, "96x96anim.png", 0, 0, 3, 2);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SOLDIER_SELECTION_CIRCLE = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "circle.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(4,4,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.SHOT_BULLET = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "bullet.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(16,16,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.MUZZLE_FLASH = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "muzzleflash.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);

		textureAtlas = new BitmapTextureAtlas(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Textures.WAYPOINT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "waypoint.png", 0, 0);
		getTextureManager().loadTexture(textureAtlas);
	}

	@Override
	public Scene onLoadScene() {
		mEngine.registerUpdateHandler(new FPSLogger());
		gameScene = new GameScene(this);

        mRenderSurfaceView.setOnCreateContextMenuListener(gameScene);

		return gameScene;
	}

	@Override
	public void onLoadComplete() {

	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {

		if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {

			if(isShowing(runningGamesView))
		    {
				finish();
		    }
			else
			{
		    	frameLayout.removeAllViews();
			    frameLayout.addView(runningGamesView);
			}

			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if(isShowing(mRenderSurfaceView))
		{
			return gameScene.onOptionsItemSelected(item);
		}
		else
		{
			switch (item.getItemId()) {
			case R.id.menu_start_game:

				if(!isShowing(mRenderSurfaceView))
				{
					frameLayout.removeAllViews();

					final FrameLayout.LayoutParams surfaceViewLayoutParams = new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams());
					frameLayout.addView(mRenderSurfaceView, surfaceViewLayoutParams);
				}

				final GameInfo newGame = new GameInfo(getPlayerID(), new GameID(UUID.randomUUID().toString()));
				server.initiateGame(newGame);

				return true;
			case R.id.menu_join_game:

				if(!isShowing(announcedGamesView))
				{
					frameLayout.removeAllViews();
					frameLayout.addView(announcedGamesView);
				}

				return true;
			case R.id.menu_running_games:

				if(!isShowing(runningGamesView))
				{
					frameLayout.removeAllViews();
					frameLayout.addView(runningGamesView);
				}

				return true;
			case R.id.menu_quit:
				finish();

				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
	}

	public PlayerID getPlayerID() {
		return playerID;
	}

	public Client getClient() {
		return client;
	}

	public Session getDTNSession() throws SessionDestroyedException, InterruptedException {
		return dtnClient.getSession();
	}

	public Server getServer() {
		return server;
	}

	public void showGameContextMenu()
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mRenderSurfaceView.showContextMenu();
			}
		});
	}


	@Override
	protected void onSetContentView() {
	    mRenderSurfaceView = new RenderSurfaceView(this);
	    mRenderSurfaceView.setRenderer(mEngine);

	    announcedGamesView = new GameListView(this, client.announcedGames) {
			@Override
			public boolean onMenuItemClick(final MenuItem pItem) {
				switch(pItem.getItemId())
				{
				case R.id.menu_join:
					assert announcedGamesView.getCheckedItemIds().length == 1;
					client.joinGame((GameInfo) announcedGamesView.getItemAtPosition(announcedGamesView.getCheckedItemPosition()));

					return true;
				default:
					return false;
				}
			}

			@Override
			protected int getContextMenuRes() {
				return R.menu.context_announced_game;
			}

	    };

	    runningGamesView = new GameListView(this, client.runningGames) {
			@Override
			public boolean onMenuItemClick(final MenuItem item) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			protected int getContextMenuRes() {
				return R.menu.context_running_game;
			}

	    };

	    frameLayout = new FrameLayout(this);
	    final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
	    		FrameLayout.LayoutParams.FILL_PARENT,
	    		FrameLayout.LayoutParams.FILL_PARENT);
	    setContentView(frameLayout, layoutParams);

	    frameLayout.addView(runningGamesView);
	}

	/**
	 * update the visible game
	 * @param pGame changed game
	 */
	public void updateGame(final GameInfo pGame) {
		if(isShowing(mRenderSurfaceView)
				&& (gameScene.currentGame.equals(pGame)))
		{
			gameScene.startPlanningPhase();
		}
		else if(isShowing(announcedGamesView))
		{
			announcedGamesView.getAdapter().notifyDataSetChanged();
		}
		else if(isShowing(runningGamesView))
		{
			runningGamesView.getAdapter().notifyDataSetChanged();
		}
	}

	private static final String TAG = "AppActivity";

	private final PlayerID playerID;

	private final Client client;
	private final Server server;

	private CustomDTNClient dtnClient;
	private CustomDTNDataHandler dtnDataHandler;

	private ViewGroup frameLayout;
	private GameListView announcedGamesView;
	private GameListView runningGamesView;

	private SmoothCamera camera;
	private GameScene gameScene;

	private boolean isShowing(final View pView) {
		return (pView.getParent() == frameLayout);
	}
}
