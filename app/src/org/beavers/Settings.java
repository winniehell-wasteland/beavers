package org.beavers;

import java.util.UUID;

import org.beavers.gameplay.PlayerID;

public class Settings {
	public static PlayerID playerID = new PlayerID(UUID.randomUUID().toString());
}
