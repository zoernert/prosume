package de.prosume;

import java.io.Serializable;

/**
 * Daten f�r die Persistierung eines Handelsgesch�ftes.
 * Da die Bilanzierung beim MeterGateway stattfindet, enth�lt dieses Objet lediglich die Daten, die vom Trader bef�llt an das jeweilige Metergatewway gemeldet werden
 * 
 * @author Thorsten Zoerner
 *
 */
public class Trade implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9109587768201733872L;
	private String fromGateway=null;
	private String toGateway=null;
	private long power=0;
	private long slot=0;
	private String certificate=null;
	public String getFromGateway() {
		return fromGateway;
	}
	public void setFromGateway(String fromGateway) {
		this.fromGateway = fromGateway;
	}
	public String getToGateway() {
		return toGateway;
	}
	public void setToGateway(String toGateway) {
		this.toGateway = toGateway;
	}
	public long getPower() {
		return power;
	}
	public void setPower(long power) {
		this.power = power;
	}
	public long getSlot() {
		return slot;
	}
	public void setSlot(long slot) {
		this.slot = slot;
	}
	public String getCertificate() {
		return certificate;
	}
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}
	
	

}
