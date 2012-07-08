package org.beavers.ingame;

import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.path.Direction;
import org.anddev.andengine.util.path.Path;
import org.beavers.R;
import org.beavers.Textures;
import org.beavers.gameplay.GameActivity;
import org.beavers.storage.CustomGSON;
import org.beavers.ui.ContextMenuHandler;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;

/**
 * waypoint sprite
 * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class WayPoint extends Sprite implements ContextMenuHandler, IGameObject {

	/**
	 * default constructor
	 * @param pSoldier soldier this waypoint belongs to
	 * @param pPath path from previous waypoint
	 * @param pTile position of waypoint
	 */
	public WayPoint(final Soldier pSoldier, final Path pPath, final Tile pTile) {
		super(pTile.getX(), pTile.getY(),
			pTile.getTileWidth(), pTile.getTileHeight(),
			Textures.WAYPOINT.deepCopy());

		soldier = pSoldier;
		tile = pTile;

		path = pPath;

		waitForAim = false;

		if(path != null)
		{
			drawPath();

			Log.e(getClass().getName(), CustomGSON.getInstance().toJson(this));
		}

		setZIndex(GameActivity.ZINDEX_WAYPOINTS);
	}

	public Aim getAim() {
		return aim;
	}

	@Override
	public int getMenuID() {
		return R.menu.context_waypoint;
	}

	public WayPoint getNext() {
		return next;
	}

	public Path getPath() {
		return path;
	}

	public WayPoint getPrevious() {
		return previous;
	}

	public Soldier getSoldier() {
		return soldier;
	}

	@Override
	public Tile getTile() {
		return tile;
	}

	public boolean isWaitingForAim() {
		return waitForAim;
	}

	@Override
	public void onMenuCreated(final ContextMenu pMenu) {
		pMenu.setHeaderTitle(R.string.context_menu_waypoint);
		pMenu.findItem(R.id.context_menu_waypoint_remove)
			.setEnabled(next == null)
			.setVisible(previous != null);
		pMenu.findItem(R.id.context_menu_add_aim).setVisible(aim == null);
		pMenu.findItem(R.id.context_menu_remove_aim).setVisible(aim != null);
	}
	
	public void remove(){
		soldier.removeLastWayPoint();
		if(removeListener != null)
		{
			removeListener.onRemoveObject(this);
		}
	}
	
	
	
	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.context_menu_waypoint_remove:
			if(next == null)
			{
				soldier.changeAP(getPath().getLength()-1);
				soldier.removeLastWayPoint();

				if(removeListener != null)
				{
					removeListener.onRemoveObject(this);
				}
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

	public void setAim(final Tile pTile){
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

	public void setNext(final WayPoint pNext) {
		next = pNext;
	}

	public void setPrevious(final WayPoint pPrevious) {
		previous = pPrevious;
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		// make first way point invisible
		if((previous == null) && soldier.getTile().equals(getTile()))
		{
			getTextureRegion().setHeight(0);
			getTextureRegion().setWidth(0);
		}

		super.onManagedUpdate(pSecondsElapsed);
	}

	@Override
	public void setRemoveObjectListener(final IRemoveObjectListener pListener) {
		removeListener = pListener;
	}

	private final Soldier soldier;
	private final Tile tile;

	/**
	 * @name path
	 * @{
	 */
	private final Path path;
	private WayPoint next;
	private WayPoint previous;
	/**
	 * @}
	 */

	/**
	 * @name aim
	 * @{
	 */
	private Aim aim;
	private boolean waitForAim;
	/**
	 * @}
	 */

	private IRemoveObjectListener removeListener;

	private void drawPath() {
		Line line = new Line(0, 0,
			getTile().getTileWidth()/2, getTile().getTileHeight()/2, 0);

		for(int i = getPath().getLength() - 1; i > 0; --i)
		{
			final Direction dir = getPath().getDirectionToPreviousStep(i);

			line = new Line(line.getX2(), line.getY2(),
					line.getX2() + dir.getDeltaX()*getTile().getTileWidth(),
					line.getY2() + dir.getDeltaY()*getTile().getTileHeight(),
					2 + Math.abs(dir.getDeltaX()) + Math.abs(dir.getDeltaY()));

			line.setColor(0.0f, 1.0f, 0.0f, 0.5f);
			line.setZIndex(GameActivity.ZINDEX_WAYPOINTS);

			attachChild(line);
		}
	}
}
