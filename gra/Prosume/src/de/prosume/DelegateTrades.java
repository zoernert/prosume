package de.prosume;

import java.io.Serializable;


/**
 * Nachrichteninhalt um von einem Consumer die Trades für einen Zeitslot an einen Trader zu delegieren
 * @author Thorsten Zoerner
 *
 */

public class DelegateTrades implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5182683469042934547L;
	private String meterGateway="";
	private long slot=0;
	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	private String trader="";
	private String certificate="";
	
	
	public DelegateTrades(String meterGatway,long slot,String trader,String certificate) {
		this.meterGateway=meterGatway;
		this.slot=slot;
		this.trader=trader;
		this.certificate=certificate;
	}

	public String getTrader() {
		return trader;
	}

	public void setTrader(String trader) {
		this.trader = trader;
	}

	public String getMeterGateway() {
		return meterGateway;
	}

	public void setMeterGateway(String meterGateway) {
		this.meterGateway = meterGateway;
	}

	public long getSlot() {
		return slot;
	}

	public void setSlot(long slot) {
		this.slot = slot;
	}
	
}
