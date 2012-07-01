package org.beavers.storage;

import java.lang.reflect.Type;

import org.beavers.ingame.Tile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * serializer class for {@link Tile}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class TileSerializer implements JsonSerializer<Tile> {
	@Override
	public JsonElement serialize(final Tile pSrc, final Type pType,
	                             final JsonSerializationContext pContext) {
		if(pSrc == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonArray array = new JsonArray();

		array.add(pContext.serialize(pSrc.getColumn()));
		array.add(pContext.serialize(pSrc.getRow()));

		return array;
	}
}