package org.beavers;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.beavers.gameplay.Game;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;

public class AppActivity extends BaseGameActivity implements IOnMenuItemClickListener, Parcelable {	
	
	private final IBinder binder = new EngineBinder();
    
	/**
	 * @name camera constants
	 * @{ 
	 */
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	/**
	 * @}
	 */
	
	/**
	 * @name menu constants
	 * @{
	 */
	protected static final int MENU_START = 0;
	protected static final int MENU_JOIN = 1;
	protected static final int MENU_LOAD = 2;
	protected static final int MENU_OPTIONS = 3;
	protected static final int MENU_HELP = 4;
	protected static final int MENU_QUIT = 5;
	/**
	 * @}
	 */

	protected Camera camera;

	protected Scene mainScene;

	protected MenuScene menuScene;

	private BitmapTextureAtlas menuTexture;
	protected TextureRegion menuStartTextureRegion;
	protected TextureRegion menuJoinTextureRegion;
	protected TextureRegion menuQuitTextureRegion;
	
	/*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    */
	
	public AppActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Engine onLoadEngine() {
		
        Display display = getWindowManager().getDefaultDisplay();
       
        ScreenOrientation orientation;
        
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
        	orientation = ScreenOrientation.LANDSCAPE;
        } else {
        	orientation = ScreenOrientation.PORTRAIT;
        }
		
		this.camera = new Camera(0, 0, display.getWidth(), display.getHeight());
		
		return new Engine(new EngineOptions(true, orientation, 
				new RatioResolutionPolicy(display.getWidth(), display.getHeight()), this.camera));
	}

	@Override
	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		this.menuTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.menuStartTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.menuTexture, this, "menu_start.png", 50, 0);
		this.menuJoinTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.menuTexture, this, "menu_join.png", 50, 64);
		this.menuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.menuTexture, this, "menu_quit.png", 50, 128);
		this.mEngine.getTextureManager().loadTexture(this.menuTexture);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.createMenuScene();

		this.mainScene = new Scene();
		this.mainScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
		
		return mainScene;
	}

	@Override
	public void onLoadComplete() {
		this.mainScene.setChildScene(this.menuScene, false, true, true);
	}

	protected void createMenuScene() {
		this.menuScene = new MenuScene(this.camera);

		// menu item "start"
		final SpriteMenuItem startMenuItem = new SpriteMenuItem(MENU_START, this.menuStartTextureRegion);
		this.menuScene.addMenuItem(startMenuItem);

		// menu item "join"
		final SpriteMenuItem joinMenuItem = new SpriteMenuItem(MENU_JOIN, this.menuJoinTextureRegion);
		this.menuScene.addMenuItem(joinMenuItem);
		
		// menu item "quit"
		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.menuQuitTextureRegion);
		this.menuScene.addMenuItem(quitMenuItem);
		
		this.menuScene.buildAnimations();

		this.menuScene.setBackgroundEnabled(false);

		this.menuScene.setOnMenuItemClickListener(this);
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(this.mainScene.hasChildScene()) {
				/* Remove the menu and reset it. */
				this.menuScene.back();
			} else {
				/* Attach the menu. */
				this.mainScene.setChildScene(this.menuScene, false, true, true);
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
			float pMenuItemLocalX, float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
		case MENU_START:
			System.out.println("Start game");
			
			Intent intent = new Intent(AppActivity.this, Game.class);

			intent.putExtra("app", AppActivity.this);
						
			startActivity(intent);
			return true;
		case MENU_JOIN:
			System.out.println("Join game");
			return true;
		case MENU_QUIT:
			System.out.println("Quit game");
			this.finish();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
	
    public class EngineBinder extends Binder {
        Engine getEngine() {
            // Return this instance of LocalService so clients can call public methods
            return AppActivity.this.getEngine();
        }
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStrongBinder(binder);
	}
	
	// necessary to load Parcelable
    public static final Parcelable.Creator<AppActivity> CREATOR = new Parcelable.Creator<AppActivity>() {
        public AppActivity createFromParcel(Parcel in) {
            return new AppActivity(in);
        }

        public AppActivity[] newArray(int size) {
            return new AppActivity[size];
        }
    };

    // load activity from Parcel
    private AppActivity(Parcel in) {
        mEngine = ((EngineBinder) in.readStrongBinder()).getEngine();
    }
}