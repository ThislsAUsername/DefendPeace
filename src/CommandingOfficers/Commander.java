package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import Units.InfantryModel;
import Units.MechModel;
import Units.Unit;
import Units.UnitModel;
import CommandingOfficers.Modifiers.COModifier;
import Engine.DamageChart;


public class Commander {
	public ArrayList<Unit> units;
	public UnitModel[] unitModels;
	public ArrayList<COModifier> modifiers;
	public Color myColor;
	public int money = 1000; // TODO set money for real
	
	public void doAbilityMinor(){}
	public void doAbilityMajor(){}
	
	public Commander()
	{
		// TODO Obviously we don't want to hard-code the UnitModel array.
		unitModels = new UnitModel[2];
		unitModels[0] = new InfantryModel();
		unitModels[1] = new MechModel();
		modifiers = new ArrayList<COModifier>();
		units = new ArrayList<Unit>();
	}

	public void initTurn() {
		money += 250; // TODO real income would be great
		for(int i = 0; i < modifiers.size(); i++) {
			if (modifiers.get(i).done) {
				modifiers.remove(i);
			}
		}
		for(int i = 0; i < modifiers.size(); i++) {
			modifiers.get(i).initTurn();
		}
		
		for (int j = 0; j < units.size(); j++) {
			units.get(j).initTurn();
		}
	}

	public UnitModel getUnitModel(DamageChart.UnitEnum unitType)
	{
		UnitModel um = null;

		for(int i = 0; i < unitModels.length; ++i)
		{
			if(unitModels[i].type == unitType)
			{
				um = unitModels[i];
				break;
			}
		}

		return um;
	}
}
