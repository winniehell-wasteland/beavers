package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.util.path.Path;
import org.beavers.R;
import org.beavers.ui.ContextMenuHandler;

import android.view.ContextMenu;
import android.view.MenuItem;

public class EmptyTile implements ContextMenuHandler {

	public final TMXTile tile;

	public EmptyTile(final Scene pScene, final TMXTile pTile)
	{
		scene = pScene;
		tile = pTile;
	}

	@Override
	public int getMenuID() {
		return R.menu.context_tile;
	}

	@Override
	public void onMenuCreated(final ContextMenu pMenu) {
		pMenu.setHeaderTitle(R.string.context_menu_empty_tile);

		// TODO
		/*
		if(scene.getSelectedSoldier() == null)
		{
			for(int i = 0; i < pMenu.size(); ++i)
			{
				pMenu.getItem(i).setEnabled(false);
			}
		}
		else
		{
			shot = new Shot(scene.getSelectedSoldier());
			final Path path = shot.findPath(scene.getPathFinder(), tile);

			// can not shoot here
			if(path == null)
			{
				shot = null;
				pMenu.findItem(R.id.context_menu_shoot).setEnabled(false);
			}

			soldierPath = scene.getSelectedSoldier().findPath(scene.getPathFinder(), tile);
			pMenu.findItem(R.id.context_menu_move).setEnabled(soldierPath != null);
		}
		*/
	}

	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		switch(pItem.getItemId())
		{
		case R.id.context_menu_move:
			if(soldierPath != null)
			{
				return false;

				/*
				final WayPoint waypoint = new WayPoint(scene.getSelectedSoldier(), soldierPath, tile);
				scene.getSelectedSoldier().addWayPoint(waypoint);
				scene.addObject(waypoint);
				*/
			}

			return true;
		case R.id.context_menu_shoot:
			// TODO
			return false;

			/*
			if(shot != null)
			{
				scene.attachChild(shot);
				scene.getSelectedSoldier().fireShot(shot, tile);
			}

			return true;
			*/
		default:
		return false;
		}
	}

	private final Scene scene;

	private Shot shot;
	private Path soldierPath;
}
