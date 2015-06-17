package de.prosume;

import java.io.Serializable;

/**
 * Daten für die Persistierung eines Handelsgeschäftes.
 * Da die Bilanzierung beim MeterGateway stattfindet, enthält dieses Objet lediglich die Daten, die vom Trader befüllt an das jeweilige Metergatewway gemeldet werden
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
	private String certificate_from=null;
	private String certificate_to=null;
	
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
	public String getCertificate_from() {
		return certificate_from;
	}
	public void setCertificate_from(String certificate_from) {
		this.certificate_from = certificate_from;
	}
	public String getCertificate_to() {
		return certificate_to;
	}
	public void setCertificate_to(String certificate_to) {
		this.certificate_to = certificate_to;
	}
	
	
	

}
