package Engine;

import Terrain.Location;
import Units.Unit;

public class CombatParameters {
	public Unit attacker, defender;
	public double baseDamage, attackFactor, attackerHP, defenseFactor, defenderHP, terrainDefenseLevel;
	public Location[][] map;
	public boolean isCounter, canCounter;
	
	public CombatParameters(Unit pAttacker, Unit pDefender, Location[][] pMap,  double pBaseDamage, double pAttackFactor, double pAttackerHP, double pDefenseFactor, double pDefenderHP, double pTerrainDefenseLevel, boolean isCounter, boolean canCounter) {
		attacker			= pAttacker;
		defender			= pDefender;
		map 				= pMap;
		baseDamage			= pBaseDamage;
		attackFactor		= pAttackFactor;
		attackerHP			= pAttackerHP;
		defenseFactor		= pDefenseFactor;
		defenderHP			= pDefenderHP;
		terrainDefenseLevel	= pTerrainDefenseLevel;
		this.isCounter 		= isCounter;
		this.canCounter 	= canCounter;
	}
	
	public CombatParameters(Unit pAttacker, Unit pDefender, Location[][] pMap, boolean isCounter, boolean canCounter) {
		attacker		= pAttacker;
		defender		= pDefender;
		map 			= pMap;
		this.isCounter	= isCounter;
		this.canCounter = canCounter;
		CalculateParameters();
	}
	
	public double CalculateDamage() {
//		[B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
		double overallPower = (baseDamage*attackFactor/100/*+Random factor?*/)*(attackerHP/100);
		double overallDefense = ((200-(defenseFactor+terrainDefenseLevel*defenderHP))/100);
		return overallPower*overallDefense;
	}
	
	/**
	 * Makes the attacker the defender, inverts the counter flag, and recalculates the rest of the parameters.
	 */
	public void Swap() {
		if (!canCounter) {
			System.out.println("Error in CombatParameters.Swap()! Attack is noted as being uncounterable, but swapping is happening.");
		}
		Unit temp = attacker;
		attacker = defender;
		defender = temp;
		isCounter = !isCounter;
		CalculateParameters();
	}
	
	private void CalculateParameters() {
		baseDamage			= DamageChart.chart[defender.model.type.ordinal()][attacker.model.type.ordinal()];
		attackFactor		= attacker.model.COStr;
		attackerHP			= attacker.HP;
		defenseFactor		= defender.model.CODef;
		defenderHP			= defender.HP;
		terrainDefenseLevel	= map[defender.x][defender.y].getEnvironment().getDefLevel();
	}
}
