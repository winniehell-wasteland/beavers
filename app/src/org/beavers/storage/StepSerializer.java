package org.beavers.storage;

import java.lang.reflect.Type;

import org.anddev.andengine.util.path.Path.Step;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * serializer class for {@link Step}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class StepSerializer implements JsonSerializer<Step> {
	@Override
	public JsonElement serialize(final Step pSrc, final Type pType,
	                             final JsonSerializationContext pContext) {
		if(pSrc == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonArray array = new JsonArray();

		array.add(pContext.serialize(pSrc.getTileColumn()));
		array.add(pContext.serialize(pSrc.getTileRow()));

		return array;
	}
}