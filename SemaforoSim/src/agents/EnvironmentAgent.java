package agents;

import gui.EnvironmentGui;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import objects.CarByStatus;
import objects.CarObject.CarBehavior;
import objects.CarObject.CarStatus;
import objects.SemaphoreObject.SemaphoreSignal;

public class EnvironmentAgent extends Agent {
	
	private EnvironmentGui environmentGUI;
	
	//Possible Questions
	private String WHAT_SIGNAL = "WHAT IS THE SIGNAL OF THE SEMAPHORE?";
	private String WARN_ME_ABOUT_VIOLATORS = "WARN_ME_ABOUT_VIOLATORS";
	
	// Possible Informs
	private String I_AM_HERE = "I_AM_HERE";
	
	// Request que podem ser feitos
	private String REQ_ADD_CAR = "REQ_ADD_CAR";
	private String REQ_STEP_BACK_ACTION = "REQ_STEP_BACK_ACTION";
	
	// Possible Answer
	// To MeterMaid
	private String CAR_PASSED_ON_RED_SIGN = "CAR_PASSED_ON_RED_SIGN";
	private String CAR_IN_ZEBRA_CROSSING = "CAR_IN_ZEBRA_CROSSING";
	// To CAR
	private String SIGNAL_RED = "SIGNAL_RED";
	private String SIGNAL_GREEN = "SIGNAL_GREEN";
	private String SIGNAL_CHANGED = "SIGNAL_CHANGED";
	
	// Possible Actuators
	private String JUST_GO = "JUST_GO";
	private String STOP = "STOP";
	private String STOP_ON_ZEBRA_CROSSING = "STOP_ON_ZEBRA_CROSSING";
	private String CONTINUE_TO_GO = "CONTINUE_TO_GO";
	private String STEP_BACK = "STEP_BACK";
	
	// Cor atual do semaforo
	SemaphoreSignal currentSemaphoreSignal = SemaphoreSignal.RED;
	
	// Condicoes para serem verificadas
	enum EnvironmentWatchDogs {SIGNAL_COLOR, PASSED_CAR};
	
	// Informa se houve mudancas na cor do semaforo
	boolean changedSemaphoreSignal = false;
	
	// Status atual do carro
	CarByStatus statusCar = null;
	
	protected void setup() {
		
		environmentGUI = new EnvironmentGui(this);
		environmentGUI.show();
		
		addBehaviour(new DealQueryFromAgents());
		//addBehaviour(new WatchDog());
		addBehaviour(new CarManager(this));
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("semphore-simulator");
		sd.setName("SemaphoreSim");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
	}
	
	private class DealQueryFromAgents extends CyclicBehaviour {
		
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				printAgentMessage("Recebeu uma query");
				String question = msg.getContent();
				if (WHAT_SIGNAL.equals(question)) {
					// Responde com a cor atual do semaforo
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(currentSemaphoreSignal == SemaphoreSignal.RED? SIGNAL_RED : SIGNAL_GREEN);
					myAgent.send(reply);
				} else if (WARN_ME_ABOUT_VIOLATORS.equals(question)) {
					// Responde oK para confirmar que ativara WatchDog
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("OK");
					myAgent.send(reply);
					System.out.println("Ativando watchdog para o Guarda");
					addBehaviour(new WatchDog(myAgent, EnvironmentWatchDogs.PASSED_CAR,msg));
				}
			}
		}
		
	}
	
	private class WatchDog extends Behaviour {
		
		private EnvironmentWatchDogs watchDog;
		private ACLMessage msg;
		boolean finishWatchDog = false;
		
		WatchDog(Agent a, EnvironmentWatchDogs watchDog, ACLMessage msg) {
			super(a);
			this.myAgent = a;
			this.watchDog = watchDog;
			this.msg = msg;
			if (msg == null)
				finishWatchDog = true;
		}
		
		public void action() {
			if (watchDog == EnvironmentWatchDogs.PASSED_CAR && 
					statusCar != null && !statusCar.isAlreadyRegistered() &&
					(statusCar.getCarStatus() != CarStatus.NO_CAR &&
							statusCar.getCarStatus() != CarStatus.CAR_MOVING_WITHOUT_PROBLEM)) {
				ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
				inf.addReceiver(msg.getSender());
				if (statusCar.getCarStatus() == CarStatus.CAR_PASSED_RED_SEMAPHORE)
					inf.setContent(CAR_PASSED_ON_RED_SIGN + ";" + statusCar.getIdentifier().getLocalName());
				else if (statusCar.getCarStatus() == CarStatus.CAR_STOPPED_ON_ZEBRA_CROSSING)
					inf.setContent(CAR_IN_ZEBRA_CROSSING + ";" + statusCar.getIdentifier().getLocalName());
				myAgent.send(inf);
				statusCar.setAlreadyRegistered(true);
			} else if (watchDog == EnvironmentWatchDogs.SIGNAL_COLOR && isChangedSignal()) {
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(SIGNAL_CHANGED);
				myAgent.send(reply);
				finishWatchDog = true;
			}
		}
		
		public boolean done() {
			return finishWatchDog;
		}
	}
	
	private class CarManager extends CyclicBehaviour {
		
		public CarManager(Agent a) {
			this.myAgent = a;
		}
		
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				String request_msg = msg.getContent();
				if (REQ_ADD_CAR.equals(request_msg)) {
					printAgentMessage("Carro enviou requisição para ser adicionado ao ambiente!");
					if (statusCar == null) {
						printAgentMessage("Requisição aceita!");
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						reply.setContent("OK");
						myAgent.send(reply);
						ACLMessage msgReceived = null;
						MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
						while (msgReceived == null) {
							msgReceived = myAgent.receive(mt2);
							if (msgReceived != null) {
								printAgentMessage("Mensagem recebida! Carro será adicionado!");
								// Verifica o comportamento
								String behavior = msgReceived.getContent();
								if ("JUST_GO".equals(behavior))
									environmentGUI.addCar(CarBehavior.JUST_GO, msgReceived.getSender());
								else if("STOP_BEFORE_ZEBRA_CROSSING".equals(behavior))
									environmentGUI.addCar(CarBehavior.STOP_BEFORE_ZEBRA_CROSSING, msgReceived.getSender());
								else if ("STOP_ON_ZEBRA_CROSSING".equals(behavior))
									environmentGUI.addCar(CarBehavior.STOP_ON_ZEBRA_CROSSING, msgReceived.getSender());
								else environmentGUI.addCar(msgReceived.getSender());
								printAgentMessage("Carro adicionado ao ambiente!");
								statusCar = new CarByStatus(msgReceived.getSender(), CarStatus.NO_CAR);
							}
						}
					} else {
						printAgentMessage("Carro recusado! Não há espaço para carros!");
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("NO SPACE FOR CARS");
						myAgent.send(reply);
					}
				} else if (REQ_STEP_BACK_ACTION.equals(request_msg)) {
					// Environment ask Car Animation to step back
					// Send the confimation of the operation back to the sender
					
					environmentGUI.stepBackCar();
					
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent("OK");
					myAgent.send(reply);
				}
			}
			
		}
	}

	
	public boolean isChangedSignal() {
		return changedSemaphoreSignal;
	}
	
	public void setChangedSignal(boolean changedSignal) {
		changedSemaphoreSignal = changedSignal;
	}
	
	public void setStatusCar(CarByStatus carByStatus) {
		statusCar = carByStatus;
	}
	
	private void printAgentMessage(String msg) {
		System.out.println("Message from " + getAID().getName() + ": "  + msg);
	}

	public CarByStatus getStatusCar() {
		return statusCar;
	}

	/**
	 * Send the Finish Warning to CarAgent
	 * @param car2BeDeleted
	 */
	public void sendFinishWarn2Car(AID car2BeDeleted) {
		ACLMessage msg = new ACLMessage(ACLMessage.CANCEL);
		msg.addReceiver(car2BeDeleted);
		msg.setContent("FINISH");
		send(msg);
		statusCar = null;
	}
	
}
