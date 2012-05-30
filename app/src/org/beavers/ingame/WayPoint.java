package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.beavers.ui.ContextMenuHandler;

import android.view.MenuItem;

public class WayPoint extends Rectangle implements ContextMenuHandler {

	public WayPoint(final TMXTile pTile)
	{
		super(pTile.getTileX(), pTile.getTileY(), pTile.getTileWidth(), pTile.getTileHeight());

		tile = pTile;
	}

	@Override
	public int getMenuID() {
		// TODO Auto-generated method stub
		return 1/0;
	}

	public TMXTile getTile() {
		return tile;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	private final TMXTile tile;
}
