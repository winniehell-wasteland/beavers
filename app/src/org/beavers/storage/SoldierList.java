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

import java.io.Serializable;
import java.util.HashSet;

import org.beavers.ingame.Soldier;

public class SoldierList extends HashSet<Soldier> {

	/** tag for collection in JSON files */
	public static final String JSON_TAG = "soldiers";

	/** @see {@link Serializable} */
	private static final long serialVersionUID = -7357704354193426714L;

}
