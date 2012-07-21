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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * deserializer class for {@link Step}
 * @author <a href="https://github.com/winniehell/">winniehell</a>
 */
class StepDeserializer implements JsonDeserializer<Step> {
    @Override
    public Step deserialize(final JsonElement pJson, final Type pType,
                            final JsonDeserializationContext pContext)
                            throws JsonParseException {
    	if(!pJson.isJsonArray())
    	{
    		return null;
    	}

    	final JsonArray array = pJson.getAsJsonArray();

    	if(array.size() != 2)
    	{
    		return null;
    	}

        return dummyPath.new Step(array.get(0).getAsInt(),
        	array.get(1).getAsInt());
    }

    /** necessary to create new instances of Step */
    static private Path dummyPath = new Path();
}
