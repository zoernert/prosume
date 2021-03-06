package de.prosume.roles;

/**
 * Ein Consumer, der zeitgleich auch als Producer auftreten kann.
 * Alle relevanten Methoden sind bereits beim Consumer implementiert, werden allerdings hier aus Gründen der Verständlichkeit erweitert. 
 * 
 * @author Thorsten Zoerner
 *
 */
public class Prosumer extends Consumer {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6253471281274556570L;

	@Override
	public void prosume() {
		// TODO Auto-generated method stub
		this.consume();
		this.produce();
	}

	@Override
	public void consume() {
		// TODO Auto-generated method stub
		super.consume();
	}

	@Override
	public void produce() {
		// TODO Auto-generated method stub
		long produce=this.currentMeterReading.getProduce();
		produce+=Math.round(Math.random()*1000);
		this.currentMeterReading.setProduce(produce);
	}

	
}
