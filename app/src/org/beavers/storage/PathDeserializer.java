package org.beavers.storage;

import java.lang.reflect.Type;

import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.Path.Step;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * deserializer class for {@link Path}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class PathDeserializer implements JsonDeserializer<Path> {
    @Override
    public Path deserialize(final JsonElement pJson, final Type pType,
                            final JsonDeserializationContext pContext)
                            throws JsonParseException {
    	if(!pJson.isJsonArray())
    	{
    		return null;
    	}

    	final Path path = new Path();

    	for(final JsonElement step : pJson.getAsJsonArray())
    	{
    		path.append((Step) pContext.deserialize(step, Step.class));
    	}

        return path;
    }
}