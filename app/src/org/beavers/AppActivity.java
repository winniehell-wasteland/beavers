package org.beavers;

import java.util.UUID;

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
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import org.beavers.communication.Client;
import org.beavers.communication.Server;
import org.beavers.gameplay.Game;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.PlayerID;
import org.beavers.ui.Menu;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.widget.Toast;

public class AppActivity extends BaseGameActivity implements IOnMenuItemClickListener, Parcelable {	

	public AppActivity() {
		// TODO Auto-generated constructor stub
	}
	
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		playerID = new PlayerID(UUID.randomUUID().toString());
		
		client = new Client(this);
		server = new Server(this);
	}
	
	@Override
	protected void onDestroy() {
		client.disconnect();
		server.disconnect();
		
		super.onDestroy();
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
		
		fontTexture = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);		
		menuFont = new Font(fontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 100, true, Color.BLACK);
		
		getTextureManager().loadTexture(this.fontTexture);
		getFontManager().loadFont(menuFont);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mainScene = new Scene();
		this.mainScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

		this.menuScene = new Menu(this.camera, this, menuFont);
		this.gameScene = new Game(getEngine());
		
		this.mainScene.setChildScene(this.menuScene);
		
		return mainScene;
	}

	@Override
	public void onLoadComplete() {
		
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(this.mainScene.getChildScene() == this.gameScene) {
				this.gameScene.back();
				this.mainScene.setChildScene(this.menuScene);
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
		case Menu.START:
			Toast.makeText(this, "Start game", Toast.LENGTH_SHORT).show();
			
			this.menuScene.back();
			this.mainScene.setChildScene(gameScene);
			
			GameInfo newGame = new GameInfo(getPlayerID(), new GameID(UUID.randomUUID().toString()));
			
			server.initiateGame(newGame);

			return true;
		case Menu.JOIN:
			Toast.makeText(this, "Join game", Toast.LENGTH_SHORT).show();

			if(client.announcedGames.isEmpty())
			{
				Toast.makeText(this, "No announced games", Toast.LENGTH_LONG).show();
			}
			else
			{
				client.joinGame(client.announcedGames.get(client.announcedGames.size() - 1));
			}
			return true;
		case Menu.QUIT:
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

	public PlayerID getPlayerID() {
		return playerID;
	}

	public Client getClient() {
		return client;
	}
	
	public Server getServer() {
		return server;
	}
	
	private PlayerID playerID;

	private Client client;
	private Server server;
	
	private final IBinder binder = new EngineBinder();

	private Camera camera;

	private Scene mainScene;
	private Menu menuScene;
	private Game gameScene;

	private Texture fontTexture;
	private Font menuFont;
}