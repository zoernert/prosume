package de.prosume.roles;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import de.prosume.MeterReading;
import de.prosume.DelegateTrades;

/**
 * Implementiert einen Verbraucher mit einen dedizierten Zählerstand zu jedem Zeitpunkt und einem Bedarf für die kommenden Slots
 * 
 * TODO:
 *    Wenn ein Trader angemeldet wird beim Gatway, dann kommt eine Bestätigung oder eine Ablehnung zurück, diese sollte geprüft werden.
 *    
 * @author Thorsten Zoerner
 *
 */
public class Consumer extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3871835569624140539L;
	public MeterReading currentMeterReading = new MeterReading(0,0); // Hält den aktuellen Zählerstand der Messtelle (Zählpunkt)
	
	
	private long last_slot_confirmed=0;
	private String metergateway=null;
	private String trader=null;
	
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
			/**
			 * 
			 */
			private static final long serialVersionUID = -8962381067153533057L;

			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					if(msg.getOntology().equals("timeslot")) {
							Consumer c = (Consumer) myAgent;
							String content = msg.getContent();
							
							long l = Long.parseLong(content);
							if(l!=c.last_slot_confirmed) {
								// neuer Slot erkannt, d.h der Zählerstand kann / muss verändert werden
								c.prosume();
								// Auftrag senden an Trader, für den nächsten Zeitslot
								if(c.trader!=null) {
									DelegateTrades dt = new DelegateTrades(c.metergateway,l+1,c.trader,this.toString()); //TODO Zertifikat wirklich setzen
									ACLMessage trademsg = new ACLMessage(ACLMessage.REQUEST);
									trademsg.addReceiver(new AID(c.trader,AID.ISGUID));
									trademsg.addReceiver(new AID(c.metergateway,AID.ISGUID));
									trademsg.setLanguage("prosume");
									trademsg.setOntology("delegate-trades");
									try {
										trademsg.setContentObject(dt);
										myAgent.send(trademsg);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
								}
							}
							c.last_slot_confirmed=l;
					}
				} else {
					block();
				}
			}			 
		 });
		 // Suche nach einem Metergateway bis wir es finden
		 			 
		this.findMeterGateway();	 
		this.findTrader();
	}
	
	/**
	 * Wird aufgerufen, sobald ein neuer Slot erkannt wurde
	 * In der Referenzimplementierung für einen Consumer wird lediglich der Verbauch weitergeschrieben
	 * 
	 */
	public void prosume() {
		this.consume();		
	}
	
	
	private void findTrader() {
		TickerBehaviour ticker = new TickerBehaviour(this,5000) {

			/**
			 * 
			 */

			@Override
			protected void onTick() {
				Consumer consumer = (Consumer) this.getAgent();
				if(consumer.trader==null) {
					System.out.println("Tryining to find a Trader");
					 DFAgentDescription template = new DFAgentDescription();
					 ServiceDescription sd = new ServiceDescription();
					 sd.setType("trader");
					 template.addServices(sd);
					 AID[] traders = null;
					 int result_length=0;
					 try {
						 DFAgentDescription[] result = DFService.search(this.getAgent(), template);
						 result_length=result.length;
						 
						 traders = new AID[result.length];
						 for (int i = 0; i < result.length; ++i) {						 
							 traders[i] = result[i].getName();
						 }
					 	}
					 	catch (FIPAException fe) {
					 		fe.printStackTrace();
					 	}
					 /*TODO
					   aktuell nehmen wir einen Random ausgewählten Trader
					   Finale Implemenentierung sollte hier den Trader des Vertrauens wählen */
					 if(result_length>0) {
						 	
						 	long sel_mw=Math.round(result_length*Math.random())-1;
						 	if(sel_mw<0) sel_mw=0;
						 	consumer.trader=traders[(int) sel_mw].getName();		
						 	// TODO: Nachricht an das Gateway, dass es für diesen Trader und diesen Slot mit diesem Zertifikat Meldungen akzeptieren darf
					 }
				} else {					
					this.stop();					
				}				
			}
		};
		
		this.addBehaviour(ticker);
		
		
	}
	
	/**
	 * Nutzt den DF Dienst, um ein Metergateway zu finden. 
	 * Verwendet einen Ticker (alle 5 Sekunden), bis ein Gateway aktiv ist
	 * Wird im Rahmen des setup() aufgerufen.
	 * Sobald ein MeterGateway registriert wurde , werden regelmäßig die Zählerstände übermittelt
	 */
	
	private void findMeterGateway() {
		TickerBehaviour ticker = new TickerBehaviour(this,5000) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -4850258900636205628L;

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
						/**
						 * 
						 */
						private static final long serialVersionUID = 6494586873449135655L;

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
		
		if(this.metergateway!=null) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID(this.metergateway,AID.ISGUID));
			msg.setLanguage("prosume");
			msg.setOntology("meter-reading");					
			try {
				msg.setContentObject(this.currentMeterReading);
			} catch (IOException e) {
				System.err.println("Fehler beim Serialisieren des Zählerstandes");
				e.printStackTrace();
			}
			send(msg);
			/* Nach dem Senden des Readings erwarten wir als Feedback vom MG eine Nachricht mit der Slot Nummer 
			 */
		} else {
			System.err.println("Metergateway==null@"+this.getName());			
		}
	}
	
	public void consume() {
		long consume = this.currentMeterReading.getConsume();
		consume+=Math.random()*1000;
		this.currentMeterReading.setConsume(consume);
	}
	
	public void produce() {
		// Nicht zu implementieren bei einem  reinen Consumer		
	}
	
}
