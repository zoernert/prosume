package de.prosume;

import java.io.Serializable;

/**
 * Datenobjekt für einen Zählerstand
 * 
 * @author Thorsten Zoerner
 *
 */
public class MeterReading implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7638823165898359277L;
	private long produce=0;
	private long consume=0;
	
	public MeterReading(long produce,long consume) {
		this.produce=produce;
		this.consume=consume;
	}
	
	public long getProduce() {
		return produce;
	}
	public void setProduce(long produce) {
		this.produce = produce;
	}
	public long getConsume() {
		return consume;
	}
	public void setConsume(long consume) {
		this.consume = consume;
	}
	
	
	
}
