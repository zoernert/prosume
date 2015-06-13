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

/**
 * Implementiert einen Verbraucher mit einen dedizierten Zählerstand zu jedem Zeitpunkt und einem Bedarf für die kommenden Slots
 * 
 * @author Thorsten Zoerner
 *
 */
public class Consumer extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3871835569624140539L;

	private long current_meter_reading=0;
	private long last_slot_confirmed=0;
	private String metergateway=null;
	
	/**
	 * Anmelden eines Consumer als Agent im Netzwerk
	 */
	public void setup() {
		 // Start des Verbrauchs
		
		
		
		 DFAgentDescription dfd = new DFAgentDescription();
		 dfd.setName(getAID());
		 ServiceDescription sd = new ServiceDescription();
		 sd.setType("consumer");
		 sd.setName("Consumer-"+this.toString());
		 dfd.addServices(sd);
		 
		 
		 // Empfangen von Bestätigungen des MeterGateway
		 this.addBehaviour(new CyclicBehaviour() {			
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					if(msg.getOntology().equals("timeslot")) {
							Consumer c = (Consumer) myAgent;
							String content = msg.getContent();
							
							long l = Long.parseLong(content);
							c.last_slot_confirmed=l;
					}
				}				
			}			 
		 });
		 // Suche nach einem Metergateway bis wir es finden
		 			 
		this.findMeterGateway();	 
		 		 
	}
	
	/**
	 * Nutzt den DF Dienst, um ein Metergateway zu finden. 
	 * Verwendet einen Ticker (alle 5 Sekunden), bis ein Gateway aktiv ist
	 * Wird im Rahmen des setup() aufgerufen.
	 * Sobald ein MeterGateway registriert wurde , werden regelmäßig die Zählerstände übermittelt
	 */
	
	private void findMeterGateway() {
		TickerBehaviour ticker = new TickerBehaviour(this,5000) {

			@Override
			protected void onTick() {
				Consumer consumer = (Consumer) this.getAgent();
				if(consumer.metergateway==null) {
					System.out.println("Tryining to find a MeterGateway");
					 DFAgentDescription template = new DFAgentDescription();
					 ServiceDescription sd = new ServiceDescription();
					 sd.setType("metergateway");
					 template.addServices(sd);
					 AID[] meterGateways = null;
					 int result_length=0;
					 try {
						 DFAgentDescription[] result = DFService.search(this.getAgent(), template);
						 result_length=result.length;
						 
						 meterGateways = new AID[result.length];
						 for (int i = 0; i < result.length; ++i) {						 
							 meterGateways[i] = result[i].getName();
						 }
					 	}
					 	catch (FIPAException fe) {
					 		fe.printStackTrace();
					 	}
					 /*TODO
					   aktuell nehmen wir einen Random ausgewähltes Meter-Gateway
					   Finale Implemenentierung sollte berücksichtigen, welches Gateway diesen Consumer akzeptiert */
					 if(result_length>0) {
						 	
						 	long sel_mw=Math.round(result_length*Math.random())-1;
						 	if(sel_mw<0) sel_mw=0;
						 	consumer.metergateway=meterGateways[(int) sel_mw].getName();						 
					 }
				} else {
					// Wir stoppen, sobald wir ein Gateway haben und senden jetzt die Zählerstände an das MeterGateway
					this.stop();
					// TODO: Dynamische Bestimmung, wie oft eine Ablesung erfolgen soll
					
					TickerBehaviour send_readings = new TickerBehaviour(consumer,5000) {
						protected void onTick() {
								transmitReading();							
						}
					};
					
					consumer.addBehaviour(send_readings);
					
				}				
			}
		};
		this.addBehaviour(ticker);
	}
	
	/**
	 * Übermittlung des aktuellen Zählerstandes an ein Metergateway
	 */
	public void transmitReading() {
		this.consume(); // In der Referenzimplementierung genügt es einfach etwas den Ablesewert zu erhöhen
		
		if(this.metergateway!=null) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID(this.metergateway,AID.ISGUID));
			msg.setLanguage("prosume");
			msg.setOntology("meter-reading");		
			msg.setContent(String.valueOf(this.current_meter_reading));
			send(msg);
			/* Nach dem Senden des Readings erwarten wir als Feedback vom MG eine Nachricht mit der Slot Nummer 
			 */
		} else {
			System.err.println("Metergateway==null@"+this.getName());			
		}
		
	}
	
	public void consume() {
			this.current_meter_reading+=Math.random()*1000;			
	}
	
}
