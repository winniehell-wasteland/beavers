package org.beavers.storage;


public abstract class EventContainer{
	
	long timestamp;
	int s;
	
	public EventContainer(final long timestamp, final int s){
		this.timestamp=timestamp;
		this.s=s;
	}
	
	public int getS() {
		return s;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public static class HPEvent extends EventContainer{
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
		
			return "HP Evenet: Time: "+getTimestamp()+" | ID: "+getS()+" | HP lost: "+ getHp();
		}
	}
	
	public static class ShootEvent extends EventContainer{
		int t;
		public ShootEvent(final long timestamp, final int s, final int t) {
			super(timestamp, s);
			this.t=t;
		}
		
		public int getT() {
			return t;
		}
		
		@Override
		public String toString() {
			
			return "Shoot Event: Time: "+getTimestamp()+" | Soldier: "+getS()+" | Target " +getT();
		}
	}
}

