package agents;
/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
   This example shows how to implement the initiator role in 
   a FIPA-contract-net interaction protocol. In this case in particular 
   we use a <code>ContractNetInitiator</code>  
   to assign a dummy task to the agent that provides the best offer
   among a set of agents (whose local
   names must be specified as arguments).
   @author Giovanni Caire - TILAB
 */
public class MeterMaidAgent extends Agent {
	private int nResponders;
	String car = "";
	String environment = "";
	
	// Possible Answer
	// To MeterMaid
	private String CAR_PASSED_ON_RED_SIGN = "CAR_PASSED_ON_RED_SIGN";
	private String CAR_IN_ZEBRA_CROSSING = "CAR_IN_ZEBRA_CROSSING";
	
	private String WARN_ME_ABOUT_VIOLATORS = "WARN_ME_ABOUT_VIOLATORS";
	
	enum meterMaidPerception { 
		CAR_STOP_ON_ZEBRA_CROSSING, 
		CAR_IGNORED_RED_SIGN, 
		NOTHING_TO_SAY };
	
	meterMaidPerception currentPerception = meterMaidPerception.NOTHING_TO_SAY;
	
	protected void setup() { 
  	// Read names of responders as arguments
  	Object[] args = getArguments();
  	if (args != null && args.length > 0) {
  		nResponders = args.length;
  		environment = (String) args[0];
  		
  		ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
  		msg.addReceiver(new AID(environment, AID.ISLOCALNAME));
  		msg.setContent(WARN_ME_ABOUT_VIOLATORS);
  		send(msg);
  		ACLMessage msg2 = null;
  		while (msg2 == null) {
  			msg2 = receive();
  			if (msg2 != null && "OK".equals(msg2.getContent())) {
  				printAgentMessage("Guarda de transito inicia ronda!");
  				addBehaviour(new LookforViolator(this));
  			}
  			else msg2 = null;
  		}
  		
  	}
  	else {
  		System.out.println("No responder specified.");
  	}
  } 
	
	
	private meterMaidPerception getPerceptions() {
//		Random random = new Random();
//		int rnd_perception = random.nextInt(3);
//		if (rnd_perception == 0)
//			return meterMaidPerception.NOTHING_TO_SAY;
//		else if (rnd_perception == 1)
//			return meterMaidPerception.CAR_IGNORED_RED_SIGN;
//		else if (rnd_perception == 2)
//			return meterMaidPerception.CAR_STOP_ON_ZEBRA_CROSSING;
		
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = receive(mt);
		if (msg != null && msg.getContent() != null) {
			String[] perceptions = msg.getContent().split(";");
			if (perceptions.length < 2)
				return meterMaidPerception.NOTHING_TO_SAY;
			car = perceptions[1];
			if (CAR_PASSED_ON_RED_SIGN.equals(perceptions[0]))
				return meterMaidPerception.CAR_IGNORED_RED_SIGN;
			else if (CAR_IN_ZEBRA_CROSSING.equals(perceptions[0]))
				return meterMaidPerception.CAR_STOP_ON_ZEBRA_CROSSING;
		}
		
		return meterMaidPerception.NOTHING_TO_SAY;
	}
	
	private void printAgentMessage(String msg) {
		System.out.println("Message from " + getAID().getName() + ": "  + msg);
	}
	
	/**
	 * Comportamento que busca por infratores.
	 * @author adriano
	 *
	 */
	private class LookforViolator extends CyclicBehaviour {
		
		public LookforViolator(Agent a) {
			super(a);
		}

		public void action() {
			currentPerception = getPerceptions();
			
			switch (currentPerception) {
			
				case CAR_IGNORED_RED_SIGN:
					System.out.println("Carro ignorou o sinal vermelho.");
					printAgentMessage("Carro ultrapassou o sinal vermelho. Receberá uma multa!");
					break;
				case CAR_STOP_ON_ZEBRA_CROSSING:
					System.out.println("Carro está em cima da faixa de pedestres");
					// Fill the CFP message
			  		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		  			msg.addReceiver(new AID(car, AID.ISLOCALNAME));
					msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
					// O infrator tem 10 segundos para responder
					msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
					msg.setContent("Por que parou em cima da faixa dos pedestres? Recue ou será multado!");
					addBehaviour(new DealZebraCrossingViolator(myAgent, msg));
					break;
			}
		}
		
	} // fim da inner class LookforViolator
	
	private class DealZebraCrossingViolator extends ContractNetInitiator {
		
		public DealZebraCrossingViolator(Agent a, ACLMessage cfp) {
			super(a, cfp);
			// TODO Auto-generated constructor stub
		}

		protected void handlePropose(ACLMessage propose, Vector v) {
			printAgentMessage("Agente " + propose.getSender().getName() + " enviou a resposta: " + propose.getContent());
		}
		
		protected void handleRefuse(ACLMessage refuse) {
			printAgentMessage("Agente " + refuse.getSender().getName() + " enviou uma recusa. Logo será multado!");
		}
		
		protected void handleFailure(ACLMessage failure) {
			if (failure.getSender().equals(myAgent.getAMS())) {
				// FAILURE notification from the JADE runtime: the receiver
				// does not exist
				printAgentMessage("Não houve respostas");
			}
			else {
				System.out.println("Agente "+failure.getSender().getName()+" falhou na comunicação. Será multado!");
			}
			// Immediate failure --> we will not receive a response from this agent
			nResponders--;
		}
		
		@SuppressWarnings("unchecked")
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			if (responses.size() == 0) {
				// Some responder didn't reply within the specified timeout
				printAgentMessage("Expirou o tempo de resposta do infrator. Será multado!");
				return;
			}
			// Avalia resposta
			String responseViolator;
			AID bestProposer = null;
			Enumeration e = responses.elements();
			if (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();
				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					acceptances.addElement(reply);
					responseViolator = msg.getContent();
					if ("Recuo sem problemas.".equals(responseViolator)) {
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						printAgentMessage("Proposta aceita! Infrator não receberá multa pois aceitou recuar.");
					} else {
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						printAgentMessage("Proposta recusada! Infrator será multado.");
					}
				}
			}
					
		}
		
		protected void handleInform(ACLMessage inform) {
			System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
		}
		
	} // fim da inner class DealZebraCrossingViolator
	
}



