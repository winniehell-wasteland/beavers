package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.storage.EventContainer.HPEvent;
import org.beavers.storage.EventContainer.ShootEvent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

class EventContainerDeserializer implements JsonDeserializer<EventContainer> {
    @Override
    public EventContainer deserialize(final JsonElement pJson, final Type pType,
                            final JsonDeserializationContext pContext)
                            throws JsonParseException {
    	if(!pJson.isJsonObject())
    	{
    		return null;
    	}

    	final JsonObject object = pJson.getAsJsonObject();
    	
    	if(!object.has("s") || !object.has("timestamp")) {
    		return null;
    	}
    	
    	if(object.has("t")) {
    		return new ShootEvent(object.get("timestamp").getAsLong(), object.get("s").getAsInt(), object.get("t").getAsInt());
    	}
    	else if(object.has("hp")) {
    		return new HPEvent(object.get("timestamp").getAsLong(), object.get("s").getAsInt(), object.get("hp").getAsInt());
    	}
    	
    	return null;
    }
}