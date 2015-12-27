package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import Units.Infantry;
import Units.Unit;
import Units.UnitModel;
import CommandingOfficers.Modifiers.COModifier;
import Engine.DamageChart;


public class Commander {
	public Unit[] units;
	public UnitModel[] unitModels;
	public ArrayList<COModifier> modifiers;
	public Color myColor;
	
	public void doAbilityMinor(){}
	public void doAbilityMajor(){}
	
	public Commander()
	{
		// TODO Obviously we don't want to hard-code the UnitModel array.
		unitModels = new UnitModel[1];
		unitModels[0] = new Infantry();
	}

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
