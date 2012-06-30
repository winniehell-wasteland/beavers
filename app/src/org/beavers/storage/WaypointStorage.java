package org.beavers.storage;

import org.anddev.andengine.util.path.Path;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

/**
 * serializable data of {@link WayPoint}
 *
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class WaypointStorage {

	public Path path;
	public Tile tile;

	public Tile aim;

	public WaypointStorage(final Path pPath, final Tile pTile) {
		tile = pTile;
		path = pPath;

		aim = null;
	}
}
