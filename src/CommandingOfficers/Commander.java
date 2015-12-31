package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import Units.APCModel;
import Units.InfantryModel;
import Units.MechModel;
import Units.Unit;
import Units.UnitModel;
import CommandingOfficers.Modifiers.COModifier;
import Units.UnitModel.UnitEnum;


public class Commander {
	public ArrayList<Unit> units;
	public UnitModel[] unitModels;
	public ArrayList<COModifier> modifiers;
	public Color myColor;
	public static final int DEFAULTSTARTINGMONEY = 1000;
	public int money = 0;
	public int incomePerCity = 100;
	
	public void doAbilityMinor(){}
	public void doAbilityMajor(){}
	
	public Commander()
	{
		// TODO Obviously we don't want to hard-code the UnitModel array.
		unitModels = new UnitModel[3];
		unitModels[0] = new InfantryModel();
		unitModels[1] = new MechModel();
		unitModels[2] = new APCModel();
		modifiers = new ArrayList<COModifier>();
		units = new ArrayList<Unit>();
		money = DEFAULTSTARTINGMONEY;
	}

	public void initTurn(GameMap map) {
		// Accrue income for each city under your control.
		int turnIncome = incomePerCity; // plus one for the HQ.
		for(int w = 0; w < map.mapWidth; ++w)
		{
			for(int h = 0; h < map.mapHeight; ++h)
			{
				Location loc = map.getLocation(w, h);
				if(loc.getEnvironment().terrainType == Environment.Terrains.CITY && loc.getOwner() == this)
				{
					turnIncome += incomePerCity;
				}
			}
		}
		money += turnIncome;

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

	public UnitModel getUnitModel(UnitEnum unitType)
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
	
	public UnitEnum[] getShoppingList()
	{ // TODO: will eventually need to take in terrainType so it can separate out air/ground/navy
		ArrayList<UnitEnum> arrList = new ArrayList<UnitEnum>();
			for (int i = 0; i < unitModels.length; i++) {
				arrList.add(unitModels[i].type);
			}
		UnitEnum[] returned = new UnitEnum[0];
		return arrList.toArray(returned);
	}
}
