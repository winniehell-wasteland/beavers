package org.beavers.storage;

import java.util.ArrayList;
import java.util.Iterator;

import org.beavers.ingame.IGameEventsListener;
import org.beavers.ingame.Soldier;
import org.beavers.storage.EventContainer.HPEvent;
import org.beavers.storage.EventContainer.ShootEvent;

import android.util.Log;

public class Outcome implements IGameEventsListener{

	/** tag for collection in JSON files */
	public static final String JSON_TAG = "outcome";

	private final transient long startTime;
	private final ArrayList<SoldierList> decisions;
	private final ArrayList<EventContainer> eventList;
	
	
	public Outcome(final long startT){
		startTime = startT;
		decisions = new ArrayList<SoldierList>();
		eventList= new ArrayList<EventContainer>();
	}
	
	public void addDecisions(final SoldierList pDecisions) {
		decisions.add(pDecisions);
	}
	
	public void getDecisions(final int pTeam) {
		return decisions.get(pTeam);
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
