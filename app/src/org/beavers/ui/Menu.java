package org.beavers.ui;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.opengl.font.Font;

public class Menu extends MenuScene {
	
	/**
	 * @name menu item constants
	 * @{
	 */
	public static final int START = 0;
	public static final int JOIN = 1;
	public static final int LOAD = 2;
	public static final int OPTIONS = 3;
	public static final int HELP = 4;
	public static final int QUIT = 5;
	/**
	 * @}
	 */

	public Menu(final Camera pCamera,
			final IOnMenuItemClickListener pOnMenuItemClickListener,
			final Font pFont) {
		super(pCamera, pOnMenuItemClickListener);
		
		// menu item "start"
		final MenuItem startMenuItem = new MenuItem(START, pCamera.getWidth(), pFont, "Start");
		addMenuItem(startMenuItem);		

		// menu item "join"
		final MenuItem joinMenuItem = new MenuItem(JOIN, pCamera.getWidth(), pFont, "Join");
		addMenuItem(joinMenuItem);
		
		// menu item "quit"
		final MenuItem quitMenuItem = new MenuItem(QUIT, pCamera.getWidth(), pFont, "Quit");
		addMenuItem(quitMenuItem);
		
		buildAnimations();
		setBackgroundEnabled(false);
	}
}
