package org.beavers.ingame;

import org.anddev.andengine.entity.IEntity;

public interface IGameObject extends IEntity {
	Tile getTile();
	void setRemoveObjectListener(IRemoveObjectListener pListener);
}
