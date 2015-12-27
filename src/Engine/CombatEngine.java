package Engine;

import java.util.ArrayList;

import Terrain.Location;
import Units.Unit;

public class CombatEngine {
	public static ArrayList<CombatModifier> modifiers;
	
	public static void ResolveCombat(Unit attacker, Unit defender, Location[][] map, boolean canCounter) {
		// TODO: make sure to clean up unneeded modifiers
//		for(int i = 0; i < modifiers.size(); i++) {
//			if (modifiers.get(i).done) {
//				modifiers.get(i).initTurn();
//				modifiers.remove(i);
//			}
//		}
		
		CombatParameters params = new CombatParameters(attacker, defender, map, false, canCounter);
		for(int i = 0; i < modifiers.size(); i++) {
			modifiers.get(i).alterCombat(params);
		}
		params.defender.HP -= params.CalculateDamage();
		params.attacker.fire(params); // Lets the unit know that it has actually fired a shot.
		if (canCounter) {
			params.Swap();
			for(int i = 0; i < modifiers.size(); i++) {
				modifiers.get(i).alterCombat(params);
			}
			params.defender.HP -= params.CalculateDamage();
			params.attacker.fire(params);
		}
	}
}
//getWeapon(pDefender.model.type);