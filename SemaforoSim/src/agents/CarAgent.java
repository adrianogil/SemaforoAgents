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


import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;

/**
   This example shows how to implement the responder role in 
   a FIPA-contract-net interaction protocol. In this case in particular 
   we use a <code>ContractNetResponder</code>  
   to participate into a negotiation where an initiator needs to assign
   a task to an agent among a set of candidates.
   @author Giovanni Caire - TILAB
 */
public class CarAgent extends Agent {

	// Request que podem ser feitos
	private String REQ_ADD_CAR = "REQ_ADD_CAR";
	private String REQ_STEP_BACK_ACTION = "REQ_STEP_BACK_ACTION";
	private String environment = "";
	
	protected void setup() {
		printAgentMessage("Requisitando uma conexao com o Ambiente");
		Object args[] = getArguments();
		
		if (args != null && args.length > 0) {
			environment = (String) args[0];
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID(environment, AID.ISLOCALNAME));
			msg.setContent(REQ_ADD_CAR);
			send(msg);
			
			ACLMessage msgReceived = null;
			while (msgReceived == null) {
				msgReceived = receive();
				if (msgReceived != null) {
					printAgentMessage("Recebeu uma resposta do ambiente!");
					if (msgReceived.getPerformative() == ACLMessage.REFUSE) {
						printAgentMessage("Requisição recusada!");
						doDelete();
					}
					else if (msgReceived.getPerformative() == ACLMessage.AGREE) {
						printAgentMessage("Requisição aceita !");
						ACLMessage msg2AddCar = msgReceived.createReply();
						msg2AddCar.setPerformative(ACLMessage.INFORM);
						msg2AddCar.setContent(getCarBehavior()); // Envia comportamento
						send(msg2AddCar);
					} else { 
						msgReceived = null;
						printAgentMessage("Reposta do ambiente não compreendida!");
					}
				}
			}
		}
		
		addBehaviour(new WaitFinish());
		
		printAgentMessage("Aguardando CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		addBehaviour(new ContractNetResponder(this, template) {
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				printAgentMessage("CFP recebido de " + cfp.getSender().getName() + ". \nAção a ser executada: "+cfp.getContent());
				int proposal = evaluateAction();
				if (proposal > 6) {
					// We provide a proposal
					System.out.println("Proposta: Problemas técnicos.");
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent("Problemas técnicos.");
					return propose;
				}
				else if (proposal < 3) {
					// We provide a proposal
					System.out.println("Proposta: Recuo sem problemas.");
					
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent("Recuo sem problemas.");
					return propose;
				}
				else {
					// We refuse to provide a proposal
					printAgentMessage("Recusa proposta");
					throw new RefuseException("Não estou afim!");
				}
			}

			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
				printAgentMessage("Proposta aceita!");
				if (performAction()) {
					printAgentMessage("Ação realizada com sucesso: Carro saiu de cima da faixa");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				}
				else {
					System.out.println(": Ação falhou. Carro não conseguiu recuar.");
					throw new FailureException("Não conseguiu recuar");
				}	
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
				printAgentMessage("Proposta rejeitada! Vou ser multado!");
			}
		} );
	}

	
	private class WaitFinish extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				String subjectt = msg.getContent();
				if ("FINISH".equals(subjectt)) {
					doDelete();
				}
			}
		}
	}
	
	private String getCarBehavior() {
		Random random = new Random();
		int behavior = random.nextInt(100);
		if (behavior < 20)
			return "JUST_GO";
		else if (behavior < 40)
			return "STOP_BEFORE_ZEBRA_CROSSING";
		return "STOP_ON_ZEBRA_CROSSING";
	}

	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		return (int) (Math.random() * 10);
	}

	private boolean performAction() {
		// Primeiro envia acao aos seus atuadores (EnvironmentAgent)
		ACLMessage act = new ACLMessage(ACLMessage.REQUEST);
		act.addReceiver(new AID(environment, AID.ISLOCALNAME));
		act.setContent(REQ_STEP_BACK_ACTION);
		send(act);
		return true;
	}
	
	private void printAgentMessage(String msg) {
		System.out.println("Message from " + getAID().getName() + ": "  + msg);
	}
}

