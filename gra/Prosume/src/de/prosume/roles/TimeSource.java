package de.prosume.roles;

import jade.core.Agent;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;


/**
 * Bietet eine zentrale Uhrzeit innerhalb des Prosume-Systems. 
 * Die Implementierung dient vor allem einen Zeitraffer für die Simulation nutzbar zu machen.
 * Alle anderen Rollen/Agents nutzen diesen Agent, um die aktuelle Zeit zu ermitteln. (Zeit=Slot)
 * 
 * @author Thorsten Zoerner
 *
 */
public class TimeSource extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4333488306674073001L;


	/**
	 * Fügt Behaviour dem Agent hinzu
	 * Registriert den TimeServer beim Directory Facilitator (DF Verzeichnisdienst von JADE)
	 */

	protected void setup() {
		 DFAgentDescription dfd = new DFAgentDescription();
		 dfd.setName(getAID());
		 ServiceDescription sd = new ServiceDescription();
		 sd.setType("timesource");
		 sd.setName("TimeServer");
		 dfd.addServices(sd);
		 try {
			 DFService.register(this, dfd);
		 }
		 catch (FIPAException fe) {
		 fe.printStackTrace();
		 }

		this.addBehaviour(new TimeServer(this));		
	}

	/**
	 * Liefert den aktuellen Zeitslot 
	 * Implementiert: 15 Minuten Slot ab 1.1.1970 - basierend auf System.currentTimeMillis
	 * Bedingung: Muss fortlaufend sein
	 * 
	 * @return
	 */
	
	private long getCurrentTimeSlot() {
		
		long time=System.currentTimeMillis();
		time=(long) Math.floor(time/90000);
		return time;
		
	}
	private class TimeServer extends CyclicBehaviour {
		
		Agent myAgent;
		
		TimeServer(Agent myAgent) {
			this.myAgent=myAgent;
			
		}
		
		
		 /**
		 * 
		 */
		private static final long serialVersionUID = 4737566497893264143L;

		public void action() {
				 ACLMessage msg = myAgent.receive();
				 
				 if (msg != null) {
					 	ACLMessage reply = msg.createReply();						 
					 	reply.setPerformative(ACLMessage.INFORM);
					 	
					 	 //String action = msg.getOntology();
						 String action = msg.getContent();
						 if(action.equals("currentslot"))	{				 			 
									 reply.setContent(String.valueOf(((TimeSource) myAgent).getCurrentTimeSlot()));
									 reply.setOntology("timeslot");
						 }
						 if(action.equals("help"))	{	 						 			 
									 reply.setContent("Available requests: currentslot");
									 reply.setOntology("help");
						 }
						 		
						 myAgent.send(reply);
						
				 }
		 }
	}
}
