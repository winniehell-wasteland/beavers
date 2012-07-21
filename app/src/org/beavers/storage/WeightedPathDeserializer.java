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

package org.beavers.storage;

import java.lang.reflect.Type;

import org.anddev.andengine.util.path.Path;
import org.anddev.andengine.util.path.Path.Step;
import org.anddev.andengine.util.path.WeightedPath;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * deserializer class for {@link Path}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class WeightedPathDeserializer implements JsonDeserializer<WeightedPath> {
    @Override
    public WeightedPath deserialize(final JsonElement pJson, final Type pType,
                                    final JsonDeserializationContext pContext)
                        throws JsonParseException {
    	if(!pJson.isJsonArray())
    	{
    		return null;
    	}
    	
    	// TODO read path cost
    	final WeightedPath path = new WeightedPath(0);

    	for(final JsonElement step : pJson.getAsJsonArray())
    	{
    		path.append((Step) pContext.deserialize(step, Step.class));
    	}

        return path;
    }
}
