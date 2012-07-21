/*
	(c) winniehell (2012)

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
