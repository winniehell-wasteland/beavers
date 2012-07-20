package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.ingame.Soldier;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * serializer class for {@link Soldier}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class SoldierSerializer implements JsonSerializer<Soldier> {
	@Override
	public JsonElement serialize(final Soldier pSrc, final Type pType,
	                             final JsonSerializationContext pContext) {
		if(pSrc == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonObject object = new JsonObject();

		object.add("id", pContext.serialize(pSrc.getId()));
		object.add("team", pContext.serialize(pSrc.getTeam()));
		object.add("tile", pContext.serialize(pSrc.getTile()));
		object.add("view_angle", pContext.serialize(pSrc.getRotation()));
		object.add("waypoints", pContext.serialize(pSrc.getWaypoints()));

		return object;
	}
}