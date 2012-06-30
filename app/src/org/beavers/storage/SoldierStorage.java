package org.beavers.storage;

import java.util.ArrayDeque;

import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;

import com.google.gson.annotations.SerializedName;

/**
 * serializable data of {@link Soldier}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class SoldierStorage {
	/** team the soldier belongs to */
	public int team;
	/** direction in that the soldier looks */
	@SerializedName("view_angle")
	public int viewAngle;
	/** waypoints of the soldier */
	public ArrayDeque<WaypointStorage> waypoints;

	/**
	 * default constructor
	 */
	public SoldierStorage(final int pTeam, final Tile pInitialPosition) {
		team = pTeam;
		waypoints = new ArrayDeque<WaypointStorage>();
		waypoints.addLast(new WaypointStorage(null, pInitialPosition));
	}
}
