package org.beavers.ingame;

import java.util.LinkedList;

import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.path.Direction;
import org.anddev.andengine.util.path.WeightedPath;
import org.beavers.R;
import org.beavers.Textures;
import org.beavers.gameplay.GameActivity;
import org.beavers.storage.CustomGSON;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;

/**
 * waypoint sprite
 * @author <a href="https://github.com/wintermadnezz/">wintermadnezz</a>
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class WayPoint extends Sprite implements IGameObject {

	/**
	 * default constructor
	 * @param pSoldier soldier this waypoint belongs to
	 * @param pPath path from previous waypoint
	 * @param pTile position of waypoint
	 */
	public WayPoint(final Soldier pSoldier, final WeightedPath pPath, final Tile pTile) {
		super(pTile.getX(), pTile.getY(),
			pTile.getTileWidth(), pTile.getTileHeight(),
			Textures.WAYPOINT.deepCopy());

		soldier = pSoldier;
		tile = pTile;
		path = pPath;

		pathLines = new LinkedList<Line>();

		aim = null;
		waitForAim = false;

		ignoreShots = false;
		wait = 0;

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

	public int getMenuID() {
		return R.menu.context_waypoint;
	}

	public WeightedPath getPath() {
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

	public boolean ignoresShots(){
		return ignoreShots;
	}

	public int getWait(){
		return wait;
	}

	public void onMenuCreated(final ContextMenu pMenu) {
		pMenu.setHeaderTitle(R.string.context_menu_waypoint);
		pMenu.findItem(R.id.context_menu_waypoint_remove)
			.setEnabled(isLast())
			.setVisible(!isFirst());
		pMenu.findItem(R.id.context_menu_ignore_attacks).setVisible(ignoreShots == false);
		pMenu.findItem(R.id.context_menu_react_on_attacks).setVisible(ignoreShots == true);
		pMenu.findItem(R.id.context_menu_add_aim).setVisible(aim == null);
		pMenu.findItem(R.id.context_menu_remove_aim).setVisible(aim != null);
		pMenu.findItem(R.id.context_menu_wait).setVisible(!isLast());
	}

	public void remove(){
		detachSelf();

		if(removeListener != null)
		{
			removeListener.onRemoveObject(this);
		}
	}

	@Override
	public void onAttached() {
		super.onAttached();
		getParent().sortChildren();
	}

	public boolean onMenuItemClick(final GameActivity pActivity, final MenuItem pItem) {
		switch (pItem.getItemId()) {
		case R.id.context_menu_waypoint_remove:
			if(isLast())
			{
				soldier.changeAP(getPath().getCost());
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

		case R.id.context_menu_ignore_attacks:
			ignoreShots= true;
			return true;
		case R.id.context_menu_react_on_attacks:
			ignoreShots= false;
			return true;
		case R.id.context_menu_wait:
			wait=5;
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
		if(isFirst())
		{
			final float[] scenter = soldier.getCenter();

			if((getTile().getCenterX() == scenter[0])
			   && (getTile().getCenterY() == scenter[1])
			   && (getTextureRegion().getHeight() > 0)) {

				getTextureRegion().setHeight(0);
				getTextureRegion().setWidth(0);
			}

			// shrink path line when soldier moves over it
			if(!pathLines.isEmpty())
			{
				convertSceneToLocalCoordinates(scenter, scenter);
				final Line line = pathLines.getFirst();

				if((line.getX2() != scenter[0]) || (line.getY2() != scenter[1]))
				{
					line.setPosition(line.getX1(), line.getY1(),
					                 scenter[0], scenter[1]);

					if((Math.abs(line.getWidth()) <= 1)
					   && (Math.abs(line.getHeight()) <= 1)) {
						pathLines.removeFirst().detachSelf();
					}
				}
			}
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
	private final WeightedPath path;
	private final LinkedList<Line> pathLines;

	/**
	 * @name aim
	 * @{
	 */
	private Aim aim;
	private boolean waitForAim;
	/**
	 * @}
	 */

	private boolean ignoreShots;
	private int wait;

	private IRemoveObjectListener removeListener;

	private void drawPath() {
		final int TILE_HEIGHT = getTile().getTileHeight();
		final int TILE_WIDTH = getTile().getTileWidth();

		Line line = new Line(0, 0, TILE_WIDTH/2, TILE_HEIGHT/2, 0);

		for(int i = getPath().getLength() - 1; i > 0; --i)
		{
			final Direction dir = getPath().getDirectionToPreviousStep(i);

			line = new Line(line.getX2(), line.getY2(),
					line.getX2() + dir.getDeltaX()*TILE_WIDTH,
					line.getY2() + dir.getDeltaY()*TILE_HEIGHT,
					2 + Math.abs(dir.getDeltaX()) + Math.abs(dir.getDeltaY()));

			line.setColor(0.0f, 1.0f, 0.0f, 0.5f);
			line.setZIndex(GameActivity.ZINDEX_WAYPOINTS);

			pathLines.addFirst(line);
			attachChild(line);
		}
	}
}
