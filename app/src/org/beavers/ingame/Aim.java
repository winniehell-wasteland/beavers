/*
	(c) winniehell, wintermadnezz (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package org.beavers.ingame;

import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.sprite.Sprite;
import org.beavers.Textures;
import org.beavers.gameplay.GameActivity;

public class Aim extends Sprite {

	public Aim(final WayPoint pWayPoint, final Tile pTile){
		super(pTile.getX() - pWayPoint.getX(), pTile.getY() - pWayPoint.getY(),
			pTile.getTileWidth(), pTile.getTileHeight(), Textures.AIM.deepCopy());
		tile = pTile;

		drawLineOfSight(getWidth()/2, getHeight()/2, -getX() + pWayPoint.getWidth()/2, -getY() + pWayPoint.getHeight()/2);

		setZIndex(GameActivity.ZINDEX_AIMPOINTS);
	}

	public Tile getTile() {
		return tile;
	}

	private final Tile tile;

	private void drawLineOfSight(final float pFromX, final float pFromY, final float pToX, final float pToY) {
		float distX = (pToX - pFromX), distY = (pToY - pFromY);
		final double dist = Math.ceil(Math.sqrt(distX*distX + distY*distY));

		distX /= dist;
		distY /= dist;

		final float padding = (float) Math.sqrt(getWidth()*getWidth() + getHeight()*getHeight())/4;

		Line line = new Line(getWidth()/2 + padding*distX, getHeight()/2 + padding*distY,
				getWidth()/2 + (padding+10)*distX, getHeight()/2 + (padding+10)*distY, 4);

		final int STEP_SIZE = 20;

		for(double pos = padding; pos < Math.max(dist-padding, 0); pos += STEP_SIZE)
		{
			line.setColor(1.0f, 0.0f, 0.0f, 0.5f);
			line.setZIndex(GameActivity.ZINDEX_AIMPOINTS);
			attachChild(line);

			line = new Line(line.getX1() + STEP_SIZE*distX, line.getY1() + STEP_SIZE*distY,
					line.getX2() + STEP_SIZE*distX, line.getY2() + STEP_SIZE*distY, 4);

		}
	}
}
