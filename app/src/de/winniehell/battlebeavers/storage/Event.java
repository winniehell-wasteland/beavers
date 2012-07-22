/*
	(c) winniehell, wintermadnezz (2012)

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

public abstract class Event{
	
	long timestamp;
	int s;
	
	public Event(final long timestamp, final int s){
		this.timestamp=timestamp;
		this.s=s;
	}
	
	public int getSoldier() {
		return s;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public static class HPEvent extends Event{
		int hp;
		public HPEvent(final long timestamp, final int s, final int hp) {
			super(timestamp, s);
			this.hp=hp;
		}
		
		public int getHp() {
			return hp;
		}
		
		@Override
		public String toString() {
		
			return "HP Evenet: Time: "+getTimestamp()+" | ID: "+getSoldier()+" | HP lost: "+ getHp();
		}
	}
	
	public static class ShootEvent extends Event{
		int t;
		public ShootEvent(final long timestamp, final int s, final int t) {
			super(timestamp, s);
			this.t=t;
		}
		
		public int getTarget() {
			return t;
		}
		
		@Override
		public String toString() {
			
			return "Shoot Event: Time: "+getTimestamp()+" | Soldier: "+getSoldier()+" | Target " +getTarget();
		}
	}
}

