package de.winniehell.battlebeavers.storage;

import java.lang.reflect.Type;

import de.winniehell.battlebeavers.ingame.Soldier;
import de.winniehell.battlebeavers.storage.Event.HPEvent;
import de.winniehell.battlebeavers.storage.Event.ShootEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class EventSerializer implements JsonSerializer<Event>{
	
	/**
	 * Serialize Soldiers
	 */
	@Override
	public JsonElement serialize(final Event event, final Type pType,
            final JsonSerializationContext pContext) {
		if(event == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonObject object = new JsonObject();

		object.addProperty("timestamp", event.getTimestamp());
		object.addProperty(Soldier.JSON_TAG, event.getSoldier());
		
		if(event instanceof HPEvent){
			HPEvent hpevent = (HPEvent) event;
			object.addProperty("hp", hpevent.getHp());
		}
		else if(event instanceof ShootEvent) {
			ShootEvent shootevent = (ShootEvent) event;
			object.addProperty("target", shootevent.getTarget());
		}
		else {
			object.addProperty("foo", 1/0);
		}
		
		return object;
	}
	

}
