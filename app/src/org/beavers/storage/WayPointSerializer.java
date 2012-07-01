package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.ingame.WayPoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * serializer class for {@link WayPoint}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class WayPointSerializer implements JsonSerializer<WayPoint> {
	@Override
	public JsonElement serialize(final WayPoint pSrc, final Type pType,
	                             final JsonSerializationContext pContext) {
		if(pSrc == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonObject object = new JsonObject();

		object.add("path", pContext.serialize(pSrc.getPath()));
		object.add("tile", pContext.serialize(pSrc.getTile()));

		if(pSrc.getAim() == null)
		{
			object.add("aim", JsonNull.INSTANCE);
		}
		else
		{
			object.add("aim", pContext.serialize(pSrc.getAim().getTile()));
		}

		return object;
	}
}