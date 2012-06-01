package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.util.path.Path;
import org.beavers.R;
import org.beavers.gameplay.GameScene;
import org.beavers.ui.ContextMenuHandler;

import android.view.ContextMenu;
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
	public void onMenuCreated(final ContextMenu pMenu) {
		pMenu.setHeaderTitle(R.string.context_menu_empty_tile);

		if(gameScene.getSelectedSoldier() == null)
		{
			for(int i = 0; i < pMenu.size(); ++i)
			{
				pMenu.getItem(i).setEnabled(false);
			}
		}
		else
		{
			shot = new Shot(gameScene.getSelectedSoldier());
			final Path path = shot.findPath(gameScene.getPathFinder(), tile);

			// can not shoot here
			if(path == null)
			{
				shot = null;
				pMenu.findItem(R.id.context_menu_shoot).setEnabled(false);
			}

			soldierPath = gameScene.getSelectedSoldier().findPath(gameScene.getPathFinder(), tile);
			pMenu.findItem(R.id.context_menu_move).setEnabled(soldierPath != null);
		}
	}

	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		switch(pItem.getItemId())
		{
		case R.id.context_menu_move:
			if(soldierPath != null)
			{
				// gameScene.getSelectedSoldier().stop();
				// gameScene.getSelectedSoldier().move(tile);

				final WayPoint waypoint = new WayPoint(gameScene.getSelectedSoldier(), soldierPath, tile);
				gameScene.getSelectedSoldier().addWayPoint(waypoint);

				gameScene.drawPath(waypoint.getPath(), waypoint);
				gameScene.addObject(waypoint);

				gameScene.sortChildren();
			}

			return true;
		case R.id.context_menu_shoot:
			if(shot != null)
			{
				gameScene.attachChild(shot);
				gameScene.getSelectedSoldier().fireShot(shot, tile);
			}

			return true;
		default:
		return false;
		}
	}

	private final GameScene gameScene;

	private Shot shot;
	private Path soldierPath;
}
