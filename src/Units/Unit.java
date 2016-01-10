package Units;

import java.util.ArrayList;
import java.util.Vector;

import CommandingOfficers.Commander;
import Engine.CombatParameters;
import Engine.GameAction;
import Engine.Utils;
import Terrain.GameMap;
import Terrain.Location;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;

public class Unit {
	public Vector<Unit> heldUnits;
	public UnitModel model;
	public int x;
	public int y;
	public int fuel;
	private int captureProgress;
	private Location captureTarget;
	public Commander CO;
	public boolean isTurnOver;
	private double HP;
	public Weapon[] weapons;

	public Unit(Commander co, UnitModel um)
	{
		System.out.println("Creating a " + um.type);
		CO = co;
		model = um;
		fuel = model.maxFuel;
		isTurnOver = true;
		HP = model.maxHP;
		captureProgress = 0;
		captureTarget = null;
		if (um.weaponModels != null)
		{
			weapons = new Weapon[um.weaponModels.length];
			for (int i = 0; i < um.weaponModels.length; i++)
			{
				weapons[i] = new Weapon(um.weaponModels[i]);
			}
		}
		if (model.holdingCapacity > 0)
			heldUnits = new Vector<Unit>(model.holdingCapacity);
	}

	public void initTurn(Location locus) {
		isTurnOver = false;
		fuel -= model.idleFuelBurn;
		if (captureTarget != null && captureTarget.getResident() != this) {
			captureTarget = null;
			captureProgress = 0;
		}
		if (HP < model.maxHP) {
			if (model.canRepairOn(locus) && locus.getOwner() == CO) {
				int neededHP = Math.min(model.maxHP - getHP(), 2); // will be 0, 1, 2
				double proportionalCost = model.moneyCost/model.maxHP;
				if (CO.money >= neededHP * proportionalCost) {
					CO.money -= neededHP * proportionalCost;
					alterHP(2);
				} else if (CO.money >= proportionalCost) {
					// case will only be used if neededHP is 2
					CO.money -= proportionalCost;
					alterHP(1);
				}
			}
		}
	}

	public double getDamage(Unit target) {
		return getDamage(target, x, y);
	}

	/**
	 * @return how much base damage the target would take if this unit tried to attack it
	 */
	public double getDamage(Unit target, int xLoc, int yLoc)
	{
		if (weapons == null)
			return 0;
		Weapon chosen = null;
		for (int i = 0; i < weapons.length && chosen == null; i++)
		{
		  double damage = weapons[i].getDamage(xLoc, yLoc, target); 
			if( damage != 0)
			{
				return damage;
			}
		}
		return 0;
	}
	
	// for the purpose of letting the unit know it has attacked.
	public void fire(final CombatParameters params)
	{
		UnitEnum target = params.defender.model.type;
		int i = 0;
		for (; i < weapons.length; i++)
		{
			if (weapons[i].getDamage(target) != 0)
			{
				break;
			}
		}
		if (i == weapons.length)
			System.out.println("In "+model.name+"'s fire(): no valid weapon found");
		weapons[i].fire();
	}
	
	public int getHP() {
		return (int) Math.ceil(HP);
	}
	public double getPreciseHP() {
		return HP;
	}

	public void damageHP(double damage) {
		HP -= damage;
	}
	public void alterHP(int change) {
		HP = Math.max(1, Math.min(10, getHP() + change));
	}

	public void capture(Location target)
	{
		if (!target.isCaptureable())
		{
			System.out.println("ERROR`! Attempting to capture an uncapturable Location!");
			return;
		}
		
		if (target != captureTarget)
		{
			captureTarget = target;
			captureProgress = 0;
		}
		captureProgress += getHP();
		if (captureProgress >= 20)
		{
			target.setOwner(CO);
			captureProgress = 0;
		}
	}

	// Removed for the forseeable future; may be back
/*	public static double getAttackPower(final CombatParameters params) {
//		double output = model
//		return [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100] ;
		return 0;
	}
	public static double getDefensePower(Unit unit, boolean isCounter) {
		return 0;
	}*/

	/** Compiles and returns a list of all actions this unit could perform on map from location (xLoc, yLoc). */
	public GameAction.ActionType[] getPossibleActions(GameMap map, int xLoc, int yLoc)
	{
		ArrayList<GameAction.ActionType> actions = new ArrayList<GameAction.ActionType>();
		if( map.isLocationEmpty(this, xLoc, yLoc) )
		{
			for (int i = 0; i < model.possibleActions.length; i++) {
				switch (model.possibleActions[i])
				{
				case ATTACK:
					// highlight the tiles in range, and check them for targets
					Utils.findActionableLocations(this, GameAction.ActionType.ATTACK, xLoc, yLoc, map);
					boolean found = false;
					for (int w = 0; w < map.mapWidth; w++)
					{
						for (int h = 0; h < map.mapHeight; h++)
						{
							if (map.getLocation(w, h).isHighlightSet())
							{
								actions.add(GameAction.ActionType.ATTACK);
								found = true;
								break; // We just need one target to know attacking is possible.
							}
						}
						if (found) break;
					}
					map.clearAllHighlights();
					break;
				case CAPTURE:
					if(map.getLocation(xLoc, yLoc).getOwner() != CO && map.getLocation(xLoc, yLoc).isCaptureable())
					{
						actions.add(GameAction.ActionType.CAPTURE);
					}
					break;
				case WAIT:
					actions.add(GameAction.ActionType.WAIT);
					break;
				case LOAD:
					// Don't add - there's no unit there to board.
					break;
				case UNLOAD:
					if (heldUnits.size() > 0) {
						actions.add(GameAction.ActionType.UNLOAD);
					}
					break;
				default:
					System.out.println("getPossibleActions: Invalid action in model's possibleActions["+i+"]: " + model.possibleActions[i]);
				}
			}
		}
		else // There is a unit in the space we are evaluating. Only Load actions are supported in this case.
		{
			actions.add(GameAction.ActionType.LOAD);
		}
		GameAction.ActionType[] returned = new GameAction.ActionType[0];
		
		return actions.toArray(returned);
	}
	
	public boolean hasCargoSpace(UnitEnum type)
	{
		return (model.holdingCapacity > 0 && heldUnits.size() < model.holdingCapacity && model.holdables.contains(type));
	}
}
