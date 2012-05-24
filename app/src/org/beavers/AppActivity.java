package org.beavers;

import java.util.UUID;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
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
import org.beavers.communication.CustomDTNClient;
import org.beavers.communication.CustomDTNDataHandler;
import org.beavers.communication.Server;
import org.beavers.gameplay.GameScene;
import org.beavers.gameplay.GameID;
import org.beavers.gameplay.GameInfo;
import org.beavers.gameplay.PlayerID;
import org.beavers.ui.Menu;

import de.tubs.ibr.dtn.api.DTNClient.Session;
import de.tubs.ibr.dtn.api.Registration;
import de.tubs.ibr.dtn.api.ServiceNotAvailableException;
import de.tubs.ibr.dtn.api.SessionDestroyedException;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.widget.Toast;

public class AppActivity extends BaseGameActivity implements IOnMenuItemClickListener {	

	public AppActivity() {
		// TODO Auto-generated constructor stub
	}
	
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		playerID = new PlayerID(UUID.randomUUID().toString());
		
		client = new Client(this);
		server = new Server(this);

		dtnClient = new CustomDTNClient(this);
		dtnDataHandler = new CustomDTNDataHandler(dtnClient, server, client);
		
        dtnClient.setDataHandler(dtnDataHandler);
                
        try {
        	final Registration registration = new Registration("game/server");

        	registration.add(Server.GROUP_EID);
        	registration.add(Client.GROUP_EID);
        	
			dtnClient.initialize(registration);
		} catch (ServiceNotAvailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
        Display display = getWindowManager().getDefaultDisplay();
       
        ScreenOrientation orientation;
        
        int rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
        	orientation = ScreenOrientation.LANDSCAPE;
        } else {
        	orientation = ScreenOrientation.PORTRAIT;
        }
		
		this.camera = new SmoothCamera(0, 0, display.getWidth(), display.getHeight(), 2*display.getWidth(), 2*display.getHeight(),0);
		
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
		this.gameScene = new GameScene(this);
		
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
	
	private PlayerID playerID;

	private Client client;
	private Server server;
	
	private CustomDTNClient dtnClient;
	private CustomDTNDataHandler dtnDataHandler;

	private SmoothCamera camera;

	private Scene mainScene;
	private Menu menuScene;
	private GameScene gameScene;

	private Texture fontTexture;
	private Font menuFont;
}
