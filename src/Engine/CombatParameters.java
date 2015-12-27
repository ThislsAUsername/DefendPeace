package Engine;

import Units.Unit;

public class CombatParameters {
	public Unit attacker, defender;
	public double baseDamage, attackFactor, attackerHP, defenseFactor, defenderHP, terrainDefenseLevel;
	public boolean isCounter;
	
	public CombatParameters(Unit pAttacker, Unit pDefender, double pBaseDamage, double pAttackFactor, double pAttackerHP, double pDefenseFactor, double pDefenderHP, double pTerrainDefenseLevel, boolean isCounter) {
		attacker			= pAttacker;
		defender			= pDefender;
		baseDamage			= pBaseDamage;
		attackFactor		= pAttackFactor;
		attackerHP			= pAttackerHP;
		defenseFactor		= pDefenseFactor;
		defenderHP			= pDefenderHP;
		terrainDefenseLevel	= pTerrainDefenseLevel;
		this.isCounter = isCounter;
	}
	
	public CombatParameters(Unit pAttacker, Unit pDefender, double pTerrainDefenseLevel, boolean isCounter) {
		attacker			= pAttacker;
		defender			= pDefender;
		terrainDefenseLevel	= pTerrainDefenseLevel;
		this.isCounter = isCounter;
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
	}
}
