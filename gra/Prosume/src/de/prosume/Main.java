/**
 * 
 */
package de.prosume;

import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Startet das Testsystem basierend auf Jade3
 * 
 * Benötigt: Jade http://jade.tilab.com/
 * 
 * @author Thorsten Zoerner
 *
 */
public class Main {

	
	private static void bootstrap() throws ProfileException {
		Runtime rt = Runtime.instance();
		Profile p = new ProfileImpl(false);
	    
	    AgentContainer ac = rt.createAgentContainer(p);
	    try {
	    	
			AgentController timeServer = ac.createNewAgent("TimeSource", "de.prosume.roles.TimeSource", new Object[0]);
			timeServer.start();
			AgentController metergateway = ac.createNewAgent("MeterGateway", "de.prosume.roles.MeterGateway", new Object[0]);
			metergateway.start();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * @param args
	 *  
	 */
	public static void main(String[] args)  {
		
	    Runtime rt = Runtime.instance();
	    rt.setCloseVM(true);
	    	   
	    jade.Boot.main(args);
	    
	    try { 
	    	Main.bootstrap();
	    } catch(Exception ex) {
	    	 ex.printStackTrace();
	    	 System.exit(1);
	    }
	}

}
