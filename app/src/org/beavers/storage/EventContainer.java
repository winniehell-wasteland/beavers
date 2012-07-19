package org.beavers.storage;

import org.beavers.ingame.Soldier;

public class EventContainer{
	
	long timestamp;
	int hp;
	Soldier s;
	Soldier t;
	
	public EventContainer(final long timestamp, final Soldier s, final Soldier t){
		this.timestamp=timestamp;
		this.s=s;
		this.t=t;
	}
	
	public EventContainer(final long timestamp, final Soldier s, final int hp){
		this.timestamp=timestamp;
		this.s=s;
		this.hp=hp;
	}

	
	public int getHp() {
		return hp;
	}
	
	public Soldier getS() {
		return s;
	}
	
	public Soldier getT() {
		return t;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}

