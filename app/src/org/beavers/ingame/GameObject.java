package org.beavers.ingame;

import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;

public interface GameObject extends IEntity {

	TMXTile getTile();

	Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget);

	float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom, final TMXTile pTo);
}
