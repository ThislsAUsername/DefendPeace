package Terrain;

import Units.Unit;
import CommandingOfficers.Commander;

public class Location {

	private Environment environs = null;
	private Commander owner = null;
	private Unit resident = null;
	private boolean highlightSet = false;
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
	
	/**
	 * @return whether this property can be captured
	 * Also increments the capture counter
	 */
	public boolean isCaptureable()
	{
		if (environs.terrainType != Environment.Terrains.CITY && environs.terrainType != Environment.Terrains.FACTORY && environs.terrainType != Environment.Terrains.HQ) {
			return false;
		}
		return true;
	}
	
	public void setHighlight(boolean val)
	{
		highlightSet = val;
	}
	
	public boolean isHighlightSet()
	{
		return highlightSet;
	}
	
	public Location (Environment environment) {
		environs = environment;
		owner = null;
		resident = null;
	}
}
