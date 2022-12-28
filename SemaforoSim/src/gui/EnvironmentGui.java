package gui;

import jade.core.AID;

import javax.swing.JFrame;

import objects.CarByStatus;
import objects.CarObject;
import objects.CarObject.CarBehavior;
import objects.CarObject.CarStatus;

import agents.EnvironmentAgent;


public class EnvironmentGui {
	
	private EnvironmentAgent agentEnv;
	private EnvironmentPanel envPanel;

	public EnvironmentGui() { }
	
	public EnvironmentGui(EnvironmentAgent agentEnv) {
		this.agentEnv = agentEnv;
	}
		
	public void show() {
		envPanel = new EnvironmentPanel();
		
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(envPanel);
		//window.setSize(envPanel.getPreferredSize());
		window.pack();
		window.setVisible(true);
		envPanel.startAnimation();
		envPanel.setEnvironmentAgent(agentEnv);
	}
	
	public void addCar(AID senderId) {
		
		System.out.println("Adicionando Carro ao Ambiente");
		agentEnv.setStatusCar(new CarByStatus(senderId, CarStatus.CAR_MOVING_WITHOUT_PROBLEM));
		envPanel.setCar(new CarObject(envPanel, senderId));
		
	}
	
	public void addCar(CarBehavior carBehavior, AID senderId) {
		
		agentEnv.setStatusCar(new CarByStatus(senderId, CarStatus.CAR_MOVING_WITHOUT_PROBLEM));
		envPanel.setCar(new CarObject(envPanel, senderId));
		
	}

	public void stepBackCar() {
		envPanel.getCar().stepBack();
	}
	
	
	
}
