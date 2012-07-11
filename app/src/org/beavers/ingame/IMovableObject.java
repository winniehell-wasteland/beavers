package org.beavers.ingame;

import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.IWeightedPathFinder;
import org.anddev.andengine.util.path.WeightedPath;

public interface IMovableObject {
	WeightedPath findPath(final IWeightedPathFinder<IMovableObject> pPathFinder, final Tile pTarget);
	float getStepCost(final ITiledMap<IMovableObject> pMap, final Tile pFrom, final Tile pTo);
}
