package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.ingame.WayPoint;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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
		return JsonNull.INSTANCE;
	}
}