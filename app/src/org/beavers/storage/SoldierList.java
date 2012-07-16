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
