package org.beavers;

import java.util.Random;
import java.util.UUID;

import org.beavers.gameplay.Player;

public class Settings {
	public static Player player =
		new Player(UUID.randomUUID(), "player"+(new Random()).nextInt(1000));
	public static String defaultMap = "map";
}
