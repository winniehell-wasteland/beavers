package org.beavers;

import java.util.Random;
import java.util.UUID;

import org.beavers.gameplay.Player;

public class Settings {
	public static Player playerID =
		new Player(UUID.randomUUID(), "player"+(new Random()).nextInt(1000));
}
