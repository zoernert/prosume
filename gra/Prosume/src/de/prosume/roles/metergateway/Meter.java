package de.prosume.roles.metergateway;

import java.util.HashMap;

import de.prosume.MeterReading;
import de.prosume.DelegateTrades;
/**
 * Wird von einem Gateway verwendet, um die Daten zu einem Zähler zu speichern.
 * 
 * @author Thorsten Zoerner
 *
 */

public class Meter {
	
	private MeterReading lastReading=null;
	private String address="";
	private long last_timeslot=0;
	private HashMap<String,DelegateTrades>  delegations=new HashMap<String,DelegateTrades>();
	
	
	public Meter(String address) {		
		this.address=address;		
	}
	
	public DelegateTrades getDelegation(long slot) {
		return this.delegations.get("D"+slot);
	}

	public void setDelegation(DelegateTrades delegation) {
		this.delegations.put("D"+delegation.getSlot(), delegation);
	}

	public Meter(String address, MeterReading mr) {
			this.address=address;
			this.lastReading=mr;
	}

	public MeterReading getLastReading() {
		return lastReading;
	}

	public void setLastReading(MeterReading lastReading) {
		this.lastReading = lastReading;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
