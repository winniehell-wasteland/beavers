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

import de.winniehell.battlebeavers.ingame.Soldier;

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
		object.add("hp", pContext.serialize(pSrc.getHP()));

		return object;
	}
}
