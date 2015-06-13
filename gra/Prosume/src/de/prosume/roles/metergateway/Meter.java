package de.prosume.roles.metergateway;

import de.prosume.MeterReading;

/**
 * Wird von einem Gateway verwendet, um die Daten zu einem Z�hler zu speichern.
 * 
 * @author Thorsten Zoerner
 *
 */

public class Meter {
	
	private MeterReading lastReading=null;
	private String address="";
	private long last_timeslot=0;
	
	public Meter(String address) {		
		this.address=address;		
	}
	
	public Meter(String name, MeterReading mr) {
		// TODO Auto-generated constructor stub
	}

	public void updateReading(MeterReading lastreading,long network_timeslot) {
		if(this.lastReading==null) {
			this.lastReading=lastreading;			
		} else {
			if(network_timeslot<this.last_timeslot) return;
			if(lastreading.getConsume()<this.lastReading.getConsume()) return;
			if(lastreading.getProduce()<this.lastReading.getProduce()) return;
			this.lastReading=lastreading;
		}		
	}
	
}
