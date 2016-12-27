package Engine.Combat;

import java.util.ArrayList;

import Terrain.GameMap;
import Units.Unit;

public class CombatEngine
{
  public static ArrayList<CombatModifier> modifiers = new ArrayList<CombatModifier>();

  public static void resolveCombat(Unit attacker, Unit defender, GameMap map)
  {
    // The list of modifiers is updated and culled every turn in GameInstance.
    // It is emptied in MapController when exiting the map.
    // TODO: Figure out a better way to handle their lifecycle?

    CombatParameters params = new CombatParameters(attacker, defender, map);
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