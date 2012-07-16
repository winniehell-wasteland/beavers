package org.beavers.storage;

import java.lang.reflect.Type;

import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.WeightedPath;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * serializer class for {@link Path}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class WeightedPathSerializer implements JsonSerializer<WeightedPath> {
	@Override
	public JsonElement serialize(final WeightedPath pSrc, final Type pType,
	                             final JsonSerializationContext pContext) {
		if(pSrc == null)
		{
			return JsonNull.INSTANCE;
		}

		final JsonArray array = new JsonArray();

		for(int i = 0; i < pSrc.getLength(); ++i)
		{
			array.add(pContext.serialize(pSrc.getStep(i)));
		}

		return array;
	}
}