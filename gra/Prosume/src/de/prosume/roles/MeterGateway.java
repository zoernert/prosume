package de.prosume.roles;

import java.util.HashMap;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import de.prosume.roles.metergateway.*;
/**
 * Implementiert einen Messstellenbetreiber 
 * @author Thorsten Zoerner
 *
 */

public class MeterGateway extends Agent {
	
	
	private static final long serialVersionUID = 6322176011913142742L;
	
	private long current_network_time_slot=0;
	private String timesource=null;
	
	private HashMap meters = new HashMap(); // Hällt alle Meter (nach Adresse)
	
	/**
	 * Anmelden eines Metergateway als Agent im Netzwerk
	 */
	public void setup() {		
		
		 DFAgentDescription dfd = new DFAgentDescription();
		 dfd.setName(getAID());
		 ServiceDescription sd = new ServiceDescription();
		 sd.setType("metergateway");
		 sd.setName("MG-"+this.toString());
		 dfd.addServices(sd);
		 try {
			 DFService.register(this, dfd);
		 }
		 catch (FIPAException fe) {
		 fe.printStackTrace();
		 }
		 findTimeSource();
		 this.addBehaviour(new MeterGatewayService(this));
		 
		 
	}
	
	/**
	 * Sucht im Netzwerk nach der ersten findbaren Zeitquelle und setzt diese als lokal zu verwendende
	 * Sobald eine Quelle gefunden wurde, wird diese zur regelmäßigen Aktualisierung verwendet
	 */
	private void findTimeSource() {
		TickerBehaviour ticker = new TickerBehaviour(this,5000) {

			@Override
			protected void onTick() {
				MeterGateway mg = (MeterGateway) this.getAgent();
				if(mg.timesource==null) {					
					 DFAgentDescription template = new DFAgentDescription();
					 ServiceDescription sd = new ServiceDescription();
					 sd.setType("timesource");
					 template.addServices(sd);
					 AID[] timeservers = null;
					 int result_length=0;
					 try {
						 DFAgentDescription[] result = DFService.search(this.getAgent(), template);
						 result_length=result.length;
						 
						 timeservers = new AID[result.length];
						 for (int i = 0; i < result.length; ++i) {		
							 
							 timeservers[i] = result[i].getName();
						 }
					 	}
					 	catch (FIPAException fe) {
					 		fe.printStackTrace();
					 	}
					 /*TODO
					   aktuell nehmen wir immer die erste Zeitquelle, ohne auf die Gültigkeit zu achten */
					 if(result_length>0) {
						 	
						 	mg.timesource=timeservers[0].getName();						 							 	
						 	// TODO : Fixer Zeitwert von 5 Sekunden für den Abruf der aktuellen Slot - Sollte dynamisch sein
						 	myAgent.addBehaviour( new TickerBehaviour(myAgent,5000) {

								@Override
								protected void onTick() {
									MeterGateway myAgent = (MeterGateway) this.myAgent;
									if(myAgent.timesource!=null) {										
										ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
										msg.addReceiver(new AID(myAgent.timesource,AID.ISGUID));							
										msg.setOntology("timeslot");		
										msg.setContent("currentslot");
										send(msg);
									}
									
								} 						 								 		
						 		
						 	} );
						 	
						 	
					 }
				} else {
					// Wir stoppen, sobald wir einen Time Service haben
					this.stop();
				}				
			}
		};
		this.addBehaviour(ticker);
	}

	
	private class MeterGatewayService extends CyclicBehaviour {
		
		MeterGateway myAgent;
		
		MeterGatewayService(MeterGateway myAgent) {
			this.myAgent=myAgent;
			
		}
		
		
		 /**
		 * 
		 */
		

		public void action() {
				 ACLMessage msg = myAgent.receive();
				 
				 if (msg != null) {
					 if(msg.getOntology().equals("meter-reading")) {
						 
						 long reading = Long.parseLong(msg.getContent());
						 
						 Meter meter = (Meter) myAgent.meters.get(msg.getSender().getName());
						 if(meter==null) {
							 	meter = new Meter(msg.getSender().getName(),reading);
							 	myAgent.meters.put(msg.getSender().getName(), meter);							 	
						 }
						 meter.updateReading(reading, myAgent.current_network_time_slot);						 
						 ACLMessage reply = msg.createReply();		
						 reply.setOntology("timeslot");
						 reply.setPerformative(ACLMessage.INFORM);						 
					     reply.setContent(String.valueOf(myAgent.current_network_time_slot));					     
						 myAgent.send(reply); 
					 }	
					 if(msg.getOntology().equals("timeslot")) {
						 // Prüfen, ob wir den Timeslot von unserem Gateway bekommen haben
						 // TODO Auf Anschrift prüfen - nicht auf Name						 
						 if(msg.getSender().getName().equals(myAgent.timesource)) {
							 myAgent.current_network_time_slot=Long.parseLong(msg.getContent());							 
						 } else {
							 System.err.println("Received Timeslot of unknown source: "+msg.getSender().getName());
						 }
					 }
				 }
		 }
	}

}
