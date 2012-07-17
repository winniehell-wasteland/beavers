package org.beavers.ingame;


public interface IGameEventsListener {
	public void onGameStart(long timestamp);
	public void onHPEvent(long timestamp, Soldier soldier, int hp);
	public void onShootEvent(long timestamp, Soldier soldier, Soldier target);
}
