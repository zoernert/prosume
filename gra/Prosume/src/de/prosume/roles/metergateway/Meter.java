package de.prosume.roles.metergateway;

/**
 * Wird von einem Gateway verwendet, um die Daten zu einem Zähler zu speichern.
 * 
 * @author Thorsten Zoerner
 *
 */

public class Meter {
	
	private long lastReading=0;
	private String address="";
	private long last_timeslot=0;
	
	public Meter(String address,long lastreading) {
		this.lastReading=lastreading;
		this.address=address;		
	}
	
	public void updateReading(long lastreading,long network_timeslot) {
		if((lastreading<this.lastReading)||(network_timeslot<this.last_timeslot)) return;
		
		this.lastReading=lastreading;
		this.last_timeslot=network_timeslot;
		
	}
	
}
