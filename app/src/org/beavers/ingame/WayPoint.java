package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.R;
import org.beavers.Textures;
import org.beavers.ui.ContextMenuHandler;

import android.view.ContextMenu;
import android.view.MenuItem;

public class WayPoint extends Sprite implements ContextMenuHandler, GameObject {

	public boolean isLast = true;

	public WayPoint(final Soldier pSoldier, final Path pPath, final TMXTile pTile)
	{
		super(pTile.getTileX(), pTile.getTileY(), pTile.getTileWidth(), pTile.getTileHeight(), Textures.WAYPOINT.deepCopy());

		path = pPath;
		soldier = pSoldier;
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
		return R.menu.context_waypoint;
	}

	public Path getPath() {
		return path;
	}

	public Soldier getSoldier() {
		return soldier;
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
				return Integer.MAX_VALUE;
			}
		}

		return 0;
	}

	@Override
	public TMXTile getTile() {
		return tile;
	}

	@Override
	public void onMenuCreated(final ContextMenu pMenu) {
		pMenu.setHeaderTitle(R.string.context_menu_waypoint);
		pMenu.findItem(R.id.context_menu_waypoint_remove).setEnabled(isLast);
	}
	
	
	private ViewPoint viewpoint;
	
	public ViewPoint getFocus(){
		return viewpoint;
	}
	
	public void setFocus(final ViewPoint vp){
		viewpoint=vp;
	}
	
	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		
		
		
		switch (pItem.getItemId()) {
		case R.id.context_menu_waypoint_remove:
			if(isLast)
			{
				soldier.setViewMode(false);
				soldier.removeWayPoint();
			}
		case R.id.context_menu_focus:
			soldier.setViewMode(true);
			return true;
		default:
			return false;
		}
	}
	
	private boolean viewpointmode;
	public void setViewPoint(final boolean vp){
		viewpointmode=vp;
	}

	private final Path path;
	private final Soldier soldier;
	private final TMXTile tile;
}
