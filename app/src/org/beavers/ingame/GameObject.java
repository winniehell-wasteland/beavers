package org.beavers.ingame;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;

public interface GameObject extends IEntity {

	Tile getTile();

	Path findPath(final IPathFinder<GameObject> pPathFinder, final Tile pTarget);

	float getStepCost(final ITiledMap<GameObject> pMap, final Tile pFrom, final Tile pTo);
}
