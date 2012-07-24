/*
	(c) winniehell (2012)

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

package de.winniehell.battlebeavers.storage;

import java.lang.reflect.Type;

import org.anddev.andengine.util.path.WeightedPath;
import de.winniehell.battlebeavers.ingame.Tile;
import de.winniehell.battlebeavers.ingame.WayPoint;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * deserializer class for {@link WayPoint}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class WayPointDeserializer implements JsonDeserializer<WayPoint> {
    @Override
    public WayPoint deserialize(final JsonElement pJson, final Type pType,
                                final JsonDeserializationContext pContext)
                    throws JsonParseException {
    	if(!pJson.isJsonObject())
    	{
    		return null;
    	}

    	final JsonObject object = pJson.getAsJsonObject();

    	if(!object.has("tile") || (SoldierDeserializer.currentSoldier == null))
    	{
    		return null;
    	}

    	WeightedPath path = null;

    	if(object.has("path")) {
    		path = (WeightedPath) pContext.deserialize(
    			object.get("path"), WeightedPath.class
    		);
    	}

    	final WayPoint waypoint = new WayPoint(
        	SoldierDeserializer.currentSoldier,
        	path,
        	(Tile) pContext.deserialize(object.get("tile"), Tile.class)
        );

    	if(object.has("aim") && !object.get("aim").isJsonNull())
    	{
    		waypoint.setAim(
    			(Tile) pContext.deserialize(object.get("aim"), Tile.class));
    	}

    	if(object.has("wait") && !object.get("wait").isJsonNull())
    	{
    		waypoint.setWait(object.get("wait").getAsInt());
    	}

    	return waypoint;
    }
}
