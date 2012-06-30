package org.beavers.ingame;

import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;

public interface IMovableObject {
	Path findPath(final IPathFinder<IMovableObject> pPathFinder, final Tile pTarget);
	float getStepCost(final ITiledMap<IMovableObject> pMap, final Tile pFrom, final Tile pTo);
}
