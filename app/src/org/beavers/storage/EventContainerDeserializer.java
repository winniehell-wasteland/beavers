/*
	(c) winniehell, wintermadnezz (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

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
