package Terrain;

import Units.Unit;
import CommandingOfficers.Commander;

public class Location {
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

	private Environment environs;
	private Commander owner;
	private Unit resident;
//	public boolean isFogged;
	
	public Location (Environment environment) {
		environs = environment;
		owner = null;
		resident = null;
	}
}
