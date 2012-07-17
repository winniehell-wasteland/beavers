package org.beavers.storage;

import java.util.ArrayList;
import java.util.Iterator;

import org.beavers.ingame.Soldier;

import android.util.Log;

public class Outcome {

	private final long startTime;
	private final GameStorage storage;
	ArrayList<EventContainer> eventList;
	
	public Outcome(final long startT, final GameStorage store){
		startTime = startT;
		storage = store;
		eventList= new ArrayList<EventContainer>();
	}
	
	public void hpEvent(final long time, final Soldier s, final int hp){
		eventList.add(new EventContainer(time-startTime,s,hp));
		
	}
	
	public void shootEvent(final long time, final Soldier s, final Soldier t){
		eventList.add(new EventContainer(time-startTime,s,t));
	}
	
	public ArrayList<EventContainer> getEventList() {
		return eventList;
	}
	
	public EventContainer getFirstEvent(){
	  return eventList.remove(0);
	}
	
	public void printEvents(){
		final Iterator i =eventList.iterator();
		while(i.hasNext()){
			final EventContainer e=(EventContainer)i.next();
			Log.d(null, "Time: "+e.getTimestamp()+" | Team: "+e.getS().getTeam());
			
		}

	}
	
	private class EventContainer{
		
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
}
