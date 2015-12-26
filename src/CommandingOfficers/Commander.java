package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import Units.Unit;
import Units.UnitModel;
import CommandingOfficers.Modifiers.COModifier;


public class Commander {
	public Unit[] units;
	public UnitModel[] unitModels;
	public ArrayList<COModifier> modifiers;
	public Color myColor;
	
	public void doAbilityMinor(){}
	public void doAbilityMajor(){}
	
	public void initTurn() {
		for(int i = 0; i < modifiers.size(); i++) {
			if (modifiers.get(i).done) {
				modifiers.remove(i);
			}
		}
		for(int i = 0; i < modifiers.size(); i++) {
			modifiers.get(i).initTurn();
		}
	}
}
