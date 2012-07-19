package org.beavers.storage;

import java.util.ArrayList;
import java.util.Iterator;

import org.beavers.ingame.IGameEventsListener;
import org.beavers.ingame.Soldier;

import android.util.Log;

public class Outcome implements IGameEventsListener{

	private final long startTime;
	private final GameStorage storage;
	private final ArrayList<EventContainer> eventList;
	private final SoldierList team0,team1;
	
	public Outcome(final long startT, final GameStorage store){
		startTime = startT;
		storage = store;
		eventList= new ArrayList<EventContainer>();
		team0 = store.getSoldiersByTeam(0);
		team1 = store.getSoldiersByTeam(1);
		//final OutcomeSerializer serializer = new OutcomeSerializer();
		//serializer.serialize(this, Outcome.class, null );
	}
	
	public SoldierList getTeam0() {
		return team0;
	}
	
	public SoldierList getTeam1() {
		return team1;
	}

	@Override
	public void onHPEvent(final long timestamp, final Soldier soldier, final int hp) {
		eventList.add(new EventContainer(timestamp-startTime,soldier,hp));
		
	}

	@Override
	public void onShootEvent(final long timestamp, final Soldier soldier, final Soldier target) {
		eventList.add(new EventContainer(timestamp-startTime,soldier,target));
		
	}
	
	
	public ArrayList<EventContainer> getEventList() {
		return eventList;
	}
	
	
	public void printEvents(){
		final Iterator i =eventList.iterator();
		while(i.hasNext()){
			final EventContainer e=(EventContainer)i.next();
			if(e.getHp()!=0)Log.e("HP Event", "Time: "+e.getTimestamp()+" | Team: "+e.getS().getTeam()+" | HP lost: "+ e.getHp());
			else Log.e("Shoot Event","Time: "+e.getTimestamp()+" | Soldier: "+e.getS().getTeam()+" | Target " +e.getT().getTeam());
		}

	}
	

	


}
