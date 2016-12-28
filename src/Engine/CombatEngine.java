package Engine;

import Terrain.GameMap;
import Units.Unit;

public class CombatEngine
{
  public static void resolveCombat(Unit attacker, Unit defender, GameMap map)
  {
    // Set up our combat scenario.
    CombatParameters params = new CombatParameters(attacker, defender, map);

    // Last-minute adjustments.
    attacker.CO.applyCombatModifiers(params);
    defender.CO.applyCombatModifiers(params);

    // Start bullets (shells, missiles, whatever) flying.
    params.defender.damageHP(params.calculateDamage());
    params.attacker.fire(params); // Lets the unit know that it has actually fired a shot.
    if( params.canCounter )
    {
      params.swap();

      // TODO: Let the COs take another pass at modifying things?

      if( params.attackerHP > 0 ) // stops counterattacks from dead units.
      {
        params.defender.damageHP(params.calculateDamage());
        params.attacker.fire(params);
      }
    }
  }
}
//getWeapon(pDefender.model.type);