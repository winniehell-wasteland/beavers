package org.beavers.storage;

import java.util.ArrayList;

import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.Path.Step;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

/**
 * serializable data of {@link WayPoint}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
public class WaypointStorage {

	public ArrayList<int[]> path;
	public Tile tile;

	public Tile aim;

	public WaypointStorage(final Path pPath, final Tile pTile) {
		tile = pTile;
		path = new ArrayList<int[]>();

		if(pPath != null)
		{
			for(int i = 0; i < pPath.getLength(); ++i)
			{
				final Step step = pPath.getStep(i);
				path.add(new int[] {step.getTileColumn(), step.getTileRow()});
			}
		}

		aim = null;
	}

}
