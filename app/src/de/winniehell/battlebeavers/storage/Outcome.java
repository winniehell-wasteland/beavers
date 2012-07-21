/*
	(c) wintermadnezz (2012)

	This file is part of the game Battle Beavers.

	Battle Beavers is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Battle Beavers is distributed in the hope that it will be fun,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Battle Beavers. If not, see <http://www.gnu.org/licenses/>.
*/

package de.winniehell.battlebeavers.storage;

import java.util.ArrayList;
import java.util.Iterator;

import de.winniehell.battlebeavers.ingame.IGameEventsListener;
import de.winniehell.battlebeavers.ingame.Soldier;
import de.winniehell.battlebeavers.storage.EventContainer.HPEvent;
import de.winniehell.battlebeavers.storage.EventContainer.ShootEvent;

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
	
	public SoldierList getDecisions(final int pTeam) {
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
