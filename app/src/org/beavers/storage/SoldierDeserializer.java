package org.beavers.storage;

import java.lang.reflect.Type;
import java.util.Iterator;

import org.beavers.ingame.Soldier;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

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
    	   || !object.has("view_angle"))
    	{
    		return null;
    	}

    	final Soldier soldier = new Soldier(object.get("team").getAsInt(),
    		(Tile) pContext.deserialize(object.get("tile"), Tile.class));

    	soldier.setId(object.get("id").getAsInt());
    	soldier.setRotation(object.get("view_angle").getAsFloat());

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