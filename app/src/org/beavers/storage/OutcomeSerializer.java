package org.beavers.storage;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class OutcomeSerializer implements JsonSerializer<Outcome>{
	
	/**
	 * Serialize Soldiers
	 */
	@Override
	public JsonElement serialize(final  Outcome outcome, final Type pType,
            final JsonSerializationContext pContext) {
		if(outcome == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonObject object = new JsonObject();
		object.add("team0",pContext.serialize(outcome.getTeam0()));
		object.add("team1",pContext.serialize(outcome.getTeam1()));
		
		
		return object;
	}
	
	/**
	 * Serialize Events
	 */
	public JsonElement SerializeEvents(final JsonObject obj, final ArrayList<EventContainer> evt,  final JsonSerializationContext pContext){
		obj.add("events",pContext.serialize(evt));
		return obj;
	}
}
