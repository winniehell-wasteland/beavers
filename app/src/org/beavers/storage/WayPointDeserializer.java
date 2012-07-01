package org.beavers.storage;

import java.lang.reflect.Type;

import org.anddev.andengine.util.path.Path;
import org.beavers.ingame.Tile;
import org.beavers.ingame.WayPoint;

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

    	if(!object.has("path") || !object.has("tile"))
    	{
    		return null;
    	}

    	if(SoldierDeserializer.currentSoldier == null)
    	{
    		return null;
    	}

    	final WayPoint waypoint = new WayPoint(
        	SoldierDeserializer.currentSoldier,
        	(Path) pContext.deserialize(object.get("path"), Path.class),
        	(Tile) pContext.deserialize(object.get("tile"), Tile.class));

    	if(object.has("aim") && !object.get("aim").isJsonNull())
    	{
    		waypoint.setAim(
    			(Tile) pContext.deserialize(object.get("tile"), Tile.class));
    	}

    	return waypoint;
    }
}