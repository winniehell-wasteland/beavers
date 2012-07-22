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

package de.winniehell.battlebeavers.storage;

import java.lang.reflect.Type;

import de.winniehell.battlebeavers.ingame.Soldier;
import de.winniehell.battlebeavers.storage.Event.HPEvent;
import de.winniehell.battlebeavers.storage.Event.ShootEvent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

class EventDeserializer implements JsonDeserializer<Event> {
    @Override
    public Event deserialize(final JsonElement pJson, final Type pType,
                            final JsonDeserializationContext pContext)
                            throws JsonParseException {
    	if(!pJson.isJsonObject())
    	{
    		return null;
    	}

    	final JsonObject object = pJson.getAsJsonObject();
    	
    	if(!object.has(Soldier.JSON_TAG) || !object.has("timestamp")) {
    		return null;
    	}
    	
    	if(object.has("target")) {
    		return new ShootEvent(
    			object.get("timestamp").getAsLong(),
    			object.get(Soldier.JSON_TAG).getAsInt(), 
    			object.get("target").getAsInt()
    		);
    	}
    	else if(object.has("hp")) {
    		return new HPEvent(
    			object.get("timestamp").getAsLong(),
    			object.get(Soldier.JSON_TAG).getAsInt(),
    			object.get("hp").getAsInt()
    		);
    	}
    	
    	return null;
    }
}
