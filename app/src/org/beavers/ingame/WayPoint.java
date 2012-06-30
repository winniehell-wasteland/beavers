package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.path.Direction;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.R;
import org.beavers.Textures;
import org.beavers.gameplay.GameActivity;
import org.beavers.ui.ContextMenuHandler;

import android.view.ContextMenu;
import android.view.MenuItem;

/**
 * waypoint sprite
 * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class WayPoint extends Sprite implements ContextMenuHandler, GameObject {

	

	/**
	 * default constructor
	 * @param pSoldier soldier this waypoint belongs to
	 * @param pPath path from previous waypoint
	 * @param pTile position of waypoint
	 */
	public WayPoint(final Soldier pSoldier, final Path pPath, final TMXTile pTile)
	{
		super(pTile.getTileX(), pTile.getTileY(), pTile.getTileWidth(), pTile.getTileHeight(), Textures.WAYPOINT.deepCopy());

		path = pPath;
		soldier = pSoldier;
		tile = pTile;

		waitForAim = false;

		if(pPath != null)
		{
			drawPath();
		}

		setZIndex(GameActivity.ZINDEX_WAYPOINTS);
	}

	@Override
	public Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {
		return pPathFinder.findPath(this, 0, getTile().getTileColumn(), getTile().getTileRow(),
        		pTarget.getTileColumn(), pTarget.getTileRow());
	}

	public Aim getAim() {
		return aim;
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

	public boolean isWaitingForAim() {
		return waitForAim;
	}
	
	@Override
	public void onAttached() {
		super.onAttached();
		getParent().sortChildren();
	}
	
	@Override
	public void onMenuCreated(final ContextMenu pMenu) {
		pMenu.setHeaderTitle(R.string.context_menu_waypoint);
		pMenu.findItem(R.id.context_menu_waypoint_remove).setEnabled(isLast);
		pMenu.findItem(R.id.context_menu_waypoint_remove).setVisible(!isFirst);
		pMenu.findItem(R.id.context_menu_add_aim).setVisible(aim == null);
		pMenu.findItem(R.id.context_menu_remove_aim).setVisible(aim != null);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.context_menu_waypoint_remove:
			if(isLast)
			{
				soldier.removeWayPoint();

				detachChildren();
			
				// FIXME parent is no longer GameActivity
				//assert (getParent() instanceof GameActivity);
				//((GameActivity)getParent()).removeObject(this);
			}

			return true;
		case R.id.context_menu_add_aim:
			waitForAim = true;

			return true;
		case R.id.context_menu_remove_aim:
			setAim(null);

			return true;
		default:
			return false;
		}
	}

	public void setAim(final TMXTile pTile){
		if(pTile == null)
		{
			detachChild(aim);
			aim = null;
		} else {
			aim = new Aim(this, pTile);
			attachChild(aim);

			waitForAim = false;
		}
	}
	
	public void setFirst() {
		isFirst = true;
	}

	public void setLast(final boolean isLast) {
		this.isLast = isLast;
	}
	
	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		/**/
		// make first way point invisible
		if(isFirst && soldier.getTile().equals(tile))
		{
			getTextureRegion().setHeight(0);
			getTextureRegion().setWidth(0);
		}
		/**/

		super.onManagedUpdate(pSecondsElapsed);
	}

	
	private final Path path;
	private final Soldier soldier;
	private final TMXTile tile;

	private Aim aim;
	private boolean waitForAim;
	/** true iff this is the first waypoint of the corresponding soldier */
	private boolean isFirst = false;
	/** true iff this is the last waypoint of the corresponding soldier */
	private boolean isLast = true;
	private void drawPath() {
		Line line = new Line(0, 0, tile.getTileWidth()/2, tile.getTileHeight()/2,0);

		for(int i = path.getLength() - 1; i > 0; --i)
		{
			final Direction dir = path.getDirectionToPreviousStep(i);

			line = new Line(line.getX2(), line.getY2(),
					line.getX2() + dir.getDeltaX()*tile.getTileWidth(), line.getY2() + dir.getDeltaY()*tile.getTileHeight(),
					2 + Math.abs(dir.getDeltaX()) + Math.abs(dir.getDeltaY()));

			line.setColor(0.0f, 1.0f, 0.0f, 0.5f);
			line.setZIndex(GameActivity.ZINDEX_WAYPOINTS);

			attachChild(line);
		}
	}
}

