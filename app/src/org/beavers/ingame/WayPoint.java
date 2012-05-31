package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.Textures;
import org.beavers.ui.ContextMenuHandler;

import android.view.MenuItem;

public class WayPoint extends Sprite implements ContextMenuHandler, GameObject {

	public boolean isLast = true;

	public WayPoint(final Path pPath, final TMXTile pTile)
	{
		super(pTile.getTileX(), pTile.getTileY(), pTile.getTileWidth(), pTile.getTileHeight(), Textures.WAYPOINT.deepCopy());

		path = pPath;
		tile = pTile;

		setZIndex(0);
	}

	@Override
	public Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {
		return pPathFinder.findPath(this, 0, getTile().getTileColumn(), getTile().getTileRow(),
        		pTarget.getTileColumn(), pTarget.getTileRow());
	}

	@Override
	public int getMenuID() {
		// TODO Auto-generated method stub
		return 1/0;
	}

	public Path getPath() {
		return path;
	}

	@Override
	public float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom, final TMXTile pTo) {
		// prevent diagonals at blocked tiles
		if((Math.abs(pTo.getTileRow() - pFrom.getTileRow()) == 1)
				&& (Math.abs(pTo.getTileColumn() - pFrom.getTileColumn()) == 1))
		{
			if(pMap.isTileBlocked(this, pFrom.getTileColumn(), pTo.getTileRow())
					|| pMap.isTileBlocked(this, pTo.getTileColumn(), pFrom.getTileRow()))
			{
				return 100;
			}
		}

		return 0;
	}

	@Override
	public TMXTile getTile() {
		return tile;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	private final Path path;
	private final TMXTile tile;
}
