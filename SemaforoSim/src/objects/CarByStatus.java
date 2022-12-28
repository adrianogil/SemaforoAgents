package objects;

import jade.core.AID;
import objects.CarObject.CarStatus;

public class CarByStatus {
	private AID identifier;
	private CarStatus carStatus;
	private boolean alreadyRegistered = false; 
	
	public CarByStatus() {}
	public CarByStatus(AID identifier, CarStatus carStatus) {
		this.identifier = identifier;
		this.carStatus = carStatus;
	}
	
	public AID getIdentifier() {
		return identifier;
	}
	public void setIdentifier(AID identifier) {
		this.identifier = identifier;
	}
	public CarStatus getCarStatus() {
		return carStatus;
	}
	public void setCarStatus(CarStatus carStatus) {
		this.carStatus = carStatus;
	}
	public boolean isAlreadyRegistered() {
		return alreadyRegistered;
	}
	public void setAlreadyRegistered(boolean alreadyRegistered) {
		this.alreadyRegistered = alreadyRegistered;
	}
	
	
	
}