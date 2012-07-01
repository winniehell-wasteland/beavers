package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.ingame.WayPoint;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
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
    	return null;
    }
}