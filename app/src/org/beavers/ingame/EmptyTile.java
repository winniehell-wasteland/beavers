package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.beavers.R;
import org.beavers.gameplay.GameScene;
import org.beavers.ui.ContextMenuHandler;

import android.view.MenuItem;

public class EmptyTile implements ContextMenuHandler {

	public final TMXTile tile;

	public EmptyTile(final GameScene pGameScene, final TMXTile pTile)
	{
		gameScene = pGameScene;
		tile = pTile;
	}

	@Override
	public int getMenuID() {
		return R.menu.context_tile;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		switch(pItem.getItemId())
		{
		case R.id.context_menu_move:
			gameScene.addWayPoint(tile);

			return true;
		case R.id.context_menu_shoot:
			gameScene.fireShot(tile);

			return true;
		default:
		return false;
		}
	}

	private final GameScene gameScene;
}
