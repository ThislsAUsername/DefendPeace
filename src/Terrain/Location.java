package Terrain;

import Units.Unit;
import CommandingOfficers.Commander;

public class Location {

	private Environment environs = null;
	private Commander owner = null;
	private Unit resident = null;
//	public boolean isFogged = false;
	
	public Environment getEnvironment() {
		return environs;
	}

	public void setEnvironment(Environment environment) {
		this.environs = environment;
	}

	public Commander getOwner() {
		return owner;
	}

	public void setOwner(Commander owner) {
		this.owner = owner;
	}

	public Unit getResident() {
		return resident;
	}

	public void setResident(Unit resident) {
		this.resident = resident;
	}
	
	public Location (Environment environment) {
		environs = environment;
		owner = null;
		resident = null;
	}
}
