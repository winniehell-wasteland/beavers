package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.storage.EventContainer.HPEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class EventContainerSerializer_ implements JsonSerializer<EventContainer>{
	
	/**
	 * Serialize Soldiers
	 */
	@Override
	public JsonElement serialize(final  EventContainer event, final Type pType,
            final JsonSerializationContext pContext) {
		if(event == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonObject object = null;
		if(event instanceof HPEvent){
		//	object=(JSONObject)pContext.serialize(event);
		}
		
		
		return object;
	}
	

}
