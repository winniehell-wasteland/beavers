package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.ingame.Tile;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * deserializer class for {@link Tile}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class TileDeserializer implements JsonDeserializer<Tile> {
    @Override
    public Tile deserialize(final JsonElement pJson, final Type pType,
                            final JsonDeserializationContext pContext)
                            throws JsonParseException {
    	if(!pJson.isJsonArray())
    	{
    		return null;
    	}

    	final JsonArray array = pJson.getAsJsonArray();

    	if(array.size() != 2)
    	{
    		return null;
    	}

        return new Tile(array.get(0).getAsInt(),
        	array.get(1).getAsInt());
    }
}