package Engine.Combat;

import java.util.ArrayList;

import CommandingOfficers.Modifiers.COModifier;
import Terrain.GameMap;
import Units.Unit;

public class CombatEngine
{

  public static void resolveCombat(Unit attacker, Unit defender, GameMap map)
  {
    CombatParameters params = new CombatParameters(attacker, defender, map);
    
    // first do the attacker's modifiers
    ArrayList<COModifier> modifiers = params.attacker.CO.modifiers;
    for( int i = 0; i < modifiers.size(); i++ )
    {
      modifiers.get(i).alterCombat(params);
    }
    // then the defender's modifiers
    modifiers = params.defender.CO.modifiers;
    for( int i = 0; i < modifiers.size(); i++ )
    {
      modifiers.get(i).alterCombat(params);
    }
    
    params.defender.damageHP(params.calculateDamage());
    params.attacker.fire(params); // Lets the unit know that it has actually fired a shot.
    if( params.canCounter )
    {
      params.swap();
      for( int i = 0; i < modifiers.size(); i++ )
      {
        modifiers.get(i).alterCombat(params);
      }
      if( params.attackerHP > 0 ) // stops counterattacks from dead units unless a CombatModifier allows it
      {
        params.defender.damageHP(params.calculateDamage());
        params.attacker.fire(params);
      }
    }
  }
}
//getWeapon(pDefender.model.type);