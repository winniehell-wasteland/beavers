package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.path.IPathFinder;
import org.anddev.andengine.util.path.ITiledMap;
import org.anddev.andengine.util.path.Path;
import org.beavers.Textures;

public class ViewPoint extends Sprite implements GameObject{
	
	
	private final TMXTile tile;
	private final WayPoint waypoint;
	
	public ViewPoint(final TMXTile pTile, final WayPoint wp){
		
		super(pTile.getTileX(), pTile.getTileY(), pTile.getTileWidth(), pTile.getTileHeight(), Textures.WAYPOINT.deepCopy());
		tile=pTile;
		waypoint =wp;
		setZIndex(0);
	}
	
	
	public WayPoint getWaypoint(){
		return waypoint;
	}
	@Override
	public TMXTile getTile() {
		return tile;
	}

	@Override
	public Path findPath(final IPathFinder<GameObject> pPathFinder, final TMXTile pTarget) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getStepCost(final ITiledMap<GameObject> pMap, final TMXTile pFrom,
			final TMXTile pTo) {
		// TODO Auto-generated method stub
		return 0;
	}
}
