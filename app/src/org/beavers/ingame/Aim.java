package org.beavers.ingame;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXTile;
import org.anddev.andengine.entity.sprite.Sprite;
import org.beavers.Textures;

public class Aim extends Sprite {

	public Aim(final TMXTile pTile){

		super(pTile.getTileX(), pTile.getTileY(), pTile.getTileWidth(), pTile.getTileHeight(), Textures.AIM.deepCopy());
		tile = pTile;

		setZIndex(0);
	}

	public TMXTile getTile() {
		return tile;
	}

	private final TMXTile tile;
}
