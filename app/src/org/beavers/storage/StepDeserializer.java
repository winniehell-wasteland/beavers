package org.beavers.storage;

import java.lang.reflect.Type;

import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.Path.Step;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * deserializer class for {@link Step}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class StepDeserializer implements JsonDeserializer<Step> {
    @Override
    public Step deserialize(final JsonElement pJson, final Type pType,
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

        return dummyPath.new Step(array.get(0).getAsInt(),
        	array.get(1).getAsInt());
    }

    /** necessary to create new instances of Step */
    static private Path dummyPath = new Path();
}