package org.beavers.storage;

import java.util.ArrayList;
import java.util.Iterator;

import org.beavers.ingame.IGameEventsListener;
import org.beavers.ingame.Soldier;
import org.beavers.storage.EventContainer.HPEvent;
import org.beavers.storage.EventContainer.ShootEvent;

import android.util.Log;

public class Outcome implements IGameEventsListener{

	private final transient long startTime;
	private final ArrayList<EventContainer> eventList;
	
	
	public Outcome(final long startT){
		startTime = startT;
		eventList= new ArrayList<EventContainer>();
	}
	


	@Override
	public void onHPEvent(final long timestamp, final Soldier soldier, final int hp) {
		eventList.add(new HPEvent(timestamp-startTime,soldier.getId(),hp));
		
	}

	@Override
	public void onShootEvent(final long timestamp, final Soldier soldier, final Soldier target) {
		eventList.add(new ShootEvent(timestamp-startTime,soldier.getId(),target.getId()));
		
	}
	
	
	public ArrayList<EventContainer> getEventList() {
		return eventList;
	}
	
	
	public void printEvents(){
		final Iterator i =eventList.iterator();
		while(i.hasNext()){
			final EventContainer e=(EventContainer)i.next();
			Log.e("EventContainer", e.toString());
		}

		Log.d("EventContainer", "size: "+eventList.size());
		final String json = CustomGSON.getInstance().toJson(eventList);
		Log.d("EventContainer", json);
		final ArrayList<EventContainer> test = CustomGSON.getInstance().fromJson(json, eventList.getClass());
		Log.d("EventContainer", "size: "+test.size());
	}
	

	


}
