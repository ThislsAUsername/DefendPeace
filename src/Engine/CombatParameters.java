package Engine;

import Units.Unit;

public class CombatParameters {
	public Unit attacker, defender;
	public double baseDamage, attackFactor, attackerHP, defenseFactor, defenderHP, terrainDefenseLevel;
	
	public CombatParameters(Unit pAttacker, Unit pDefender, double pBaseDamage, double pAttackFactor, double pAttackerHP, double pDefenseFactor, double pDefenderHP, double pTerrainDefenseLevel, boolean isCounter) {
		attacker			= pAttacker;
		defender			= pDefender;
		baseDamage			= pBaseDamage;
		attackFactor		= pAttackFactor;
		attackerHP			= pAttackerHP;
		defenseFactor		= pDefenseFactor;
		defenderHP			= pDefenderHP;
		terrainDefenseLevel	= pTerrainDefenseLevel;
	}
	
	public CombatParameters(Unit pAttacker, Unit pDefender, double pTerrainDefenseLevel, boolean isCounter) {
		attacker			= pAttacker;
		defender			= pDefender;
		baseDamage			= DamageChart.chart[defender.model.type.ordinal()][attacker.model.type.ordinal()];
		attackFactor		= attacker.model.COStr;
		attackerHP			= attacker.HP;
		defenseFactor		= defender.model.CODef;
		defenderHP			= defender.HP;
		terrainDefenseLevel	= pTerrainDefenseLevel;
	}
}
