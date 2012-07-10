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

	public Path getPath() {
		return path;
	}

	public Soldier getSoldier() {
		return soldier;
	}

	@Override
	public Tile getTile() {
		return tile;
	}

	public boolean isFirst()
	{
		return soldier.getFirstWaypoint().equals(this);
	}

	public boolean isLast()
	{
		return soldier.getLastWaypoint().equals(this);
	}

	public boolean isWaitingForAim() {
		return waitForAim;
	}

	@Override
	public void onMenuCreated(final ContextMenu pMenu) {
		pMenu.setHeaderTitle(R.string.context_menu_waypoint);
		pMenu.findItem(R.id.context_menu_waypoint_remove)
			.setEnabled(isLast())
			.setVisible(!isFirst());
		pMenu.findItem(R.id.context_menu_add_aim).setVisible(aim == null);
		pMenu.findItem(R.id.context_menu_remove_aim).setVisible(aim != null);
	}

	public void remove(){
		detachSelf();

		if(removeListener != null)
		{
			removeListener.onRemoveObject(this);
		}
	}

	@Override
	public boolean onMenuItemClick(final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.context_menu_waypoint_remove:
			if(isLast())
			{
				soldier.changeAP(getPath().getCosts());
				soldier.removeLastWayPoint();

				remove();
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

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		// make first way point invisible
		if(isFirst() && soldier.getTile().equals(getTile()))
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

	/** path from previous waypoint	*/
	private final Path path;

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
