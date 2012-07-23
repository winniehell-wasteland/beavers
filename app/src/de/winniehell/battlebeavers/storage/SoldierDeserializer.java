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
import java.util.Iterator;

import de.winniehell.battlebeavers.ingame.Soldier;
import de.winniehell.battlebeavers.ingame.Tile;
import de.winniehell.battlebeavers.ingame.WayPoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * deserializer class for {@link Soldier}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class SoldierDeserializer implements JsonDeserializer<Soldier> {
    @Override
    public Soldier deserialize(final JsonElement pJson, final Type pType,
                            final JsonDeserializationContext pContext)
                            throws JsonParseException {
    	if(!pJson.isJsonObject())
    	{
    		return null;
    	}

    	final JsonObject object = pJson.getAsJsonObject();

    	if(!object.has("id") || !object.has("team") || !object.has("tile")
    	   || !object.has("view_angle") || !object.has("hp"))
    	{
    		return null;
    	}

    	final Soldier soldier = new Soldier(object.get("team").getAsInt(),
    		(Tile) pContext.deserialize(object.get("tile"), Tile.class));

    	soldier.setId(object.get("id").getAsInt());
    	soldier.setRotation(object.get("view_angle").getAsFloat());
    	soldier.setHp(object.get("hp").getAsInt());

    	if(object.has("waypoints") && object.get("waypoints").isJsonArray()) {
        	currentSoldier = soldier;

	    	synchronized (currentSoldier) {
		    	final JsonArray waypoints =
		    		object.get("waypoints").getAsJsonArray();

		    	final Iterator<JsonElement> it = waypoints.iterator();

		    	if(it.hasNext()) {
			    	final WayPoint firstWaypoint =
			    		pContext.deserialize(it.next(), WayPoint.class);

			    	if(firstWaypoint.getAim() != null)
			    	{
			    		soldier.getFirstWaypoint().setAim(
			    			firstWaypoint.getAim().getTile());
			    	}
		    	}

		    	while(it.hasNext())
		    	{
		    		final WayPoint waypoint =
		    			pContext.deserialize(it.next(),WayPoint.class);

		    		soldier.addWayPoint(waypoint);
		    	}

		    	currentSoldier = null;
	    	}
    	}

        return soldier;
    }

    static Soldier currentSoldier;
}
