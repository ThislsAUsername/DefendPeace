package CommandingOfficers;

import java.util.ArrayList;

import Units.Unit;
import Units.UnitModel;
import CommandingOfficers.Modifiers.Modifier;


public class Commander {
	public Unit[] units;
	public UnitModel[] unitModels;
	public ArrayList<Modifier> modifiers;
	
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
