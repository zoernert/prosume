package de.prosume.roles;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import de.prosume.DelegateTrades;
import de.prosume.Trade;

/**
 * Ein Trader führt Handelsgeschäfte zwischen den verschiedenen Consumer/Prosumern durch. Dazu sind mögliche Trades für einen Zeitslot zu erkennen und im anschluss durchzuführen.
 * Die Durchführung ist eine wechselseitige Benachrichtung der MeterGateways über die Bilanzveränderung.
 * 
 * 
 * @author Thorsten Zoerner
 *
 */
public class Trader extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7837887312855240652L;
	private String timesource=null;
	
	HashMap<String,DelegateTrades> delegations = new HashMap<String, DelegateTrades>();
	private long current_network_time_slot=0;
	
	public void setup() {		
		
		 DFAgentDescription dfd = new DFAgentDescription();
		 dfd.setName(getAID());
		 ServiceDescription sd = new ServiceDescription();
		 sd.setType("trader");
		 sd.setName("trader-"+this.toString());
		 dfd.addServices(sd);
		 try {
			 DFService.register(this, dfd);
		 }
		 catch (FIPAException fe) {
		 fe.printStackTrace();
		 }		 
		 this.addBehaviour(new TraderService(this));
		 this.findTimeSource();

		 
	}
	
	/**
	 * Implementiert die Kommunikationskomponente eines Traders:
	 *  - Registrieren eines Consumers/Prosumers
	 *  - Empfangen von "Bedarfsmeldungen" (Buy-Offer)
	 *  
	 * @author Thorsten Zoerner
	 *
	 */
	
	private class TraderService extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7298296108595426963L;
		Trader myAgent;
		
		TraderService(Trader myAgent) {
			this.myAgent=myAgent;
			
		}

		@Override
		public void action() {
			ACLMessage msg = myAgent.receive();			 
			if (msg != null) {
				 if(msg.getOntology().equals("delegate-trades")) {
					try {
						DelegateTrades delegation = (DelegateTrades) msg.getContentObject();
						if(delegation.getMeterGateway()!=null) {
							this.myAgent.delegations.put(msg.getSender().getName(), delegation);
							 ACLMessage reply = msg.createReply();		
							 reply.setOntology("delegate-trades");
							 reply.setPerformative(ACLMessage.CONFIRM);
							 myAgent.send(reply);					
							 this.myAgent.internalTrading();							 
							 // TODO: Externes Trading implementieren
						}
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
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
			} else {
				block();
			}			
		}
		
	}

	/**
	 * Sucht im Netzwerk nach der ersten findbaren Zeitquelle und setzt diese als lokal zu verwendende
	 * Sobald eine Quelle gefunden wurde, wird diese zur regelmäßigen Aktualisierung verwendet
	 */
	private void findTimeSource() {
		TickerBehaviour ticker = new TickerBehaviour(this,5000) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -4369694717092608918L;

			@Override
			protected void onTick() {
				Trader trader = (Trader) this.getAgent();
				if(trader.timesource==null) {					
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
						 	
						 	trader.timesource=timeservers[0].getName();						 							 	
						 	// TODO : Fixer Zeitwert von 5 Sekunden für den Abruf der aktuellen Slot - Sollte dynamisch sein
						 	myAgent.addBehaviour( new TickerBehaviour(myAgent,5000) {

								/**
								 * 
								 */
								private static final long serialVersionUID = -4460633271199160363L;

								@Override
								protected void onTick() {
									Trader myAgent = (Trader) this.myAgent;
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

	/**
	 * Versucht intern die Consume/Produce Beziehungen abzubilden auf Basis der bestehenden Delegationen...
	 */
	public void internalTrading() {
		DelegateTrades seller = null; 
		DelegateTrades buyer = null;
		if(this.delegations.size()>1) {
			Iterator<String> iterator = this.delegations.keySet().iterator();
			while(iterator.hasNext()) {
					buyer=seller;
					seller=this.delegations.get(iterator.next());
					if(seller.getSlot()>this.current_network_time_slot) {
						if(buyer!=null) {
							if(buyer.getSlot()==seller.getSlot()) {
									Trade trade = new Trade();
									trade.setCertificate_from(seller.getCertificate());
									trade.setCertificate_to(buyer.getCertificate());
									trade.setFromGateway(seller.getMeterGateway());
									trade.setToGateway(buyer.getMeterGateway());
									trade.setPower(Math.round(Math.random()*500)); // TODO Sinvolle Handelsgröße angeben! Random zur Untersuchung der Kommunikation
									trade.setSlot(buyer.getSlot());
									
									// Dieser Trade jetzt an die beiden Gateways schicken
									ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
									msg.addReceiver(new AID(trade.getFromGateway(),AID.ISGUID));	
									msg.addReceiver(new AID(trade.getToGateway(),AID.ISGUID));
									msg.setOntology("trade");		
									try {
										msg.setContentObject(trade);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									send(msg);
							}							
						}						
					}
			}
			
		}	
	}
}

