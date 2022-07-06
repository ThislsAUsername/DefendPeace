package Engine.Combat;

import java.util.HashMap;
import java.util.Map;
import Engine.GamePath;
import Engine.XYCoord;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitActionLifecycles.BattleLifecycle.BattleEvent;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;

/**
 * CombatEngine serves as the general-purpose interface into the combat calculation logic.
 */
public class CombatEngine
{
  /**
   * Static function to calculate the outcome of a battle between two units. It builds an
   * object to represent the specific combat instance and returns the result it calculates.
   * Requires perfect map info just in case the COs need to get weird.
   */
  public static BattleSummary calculateBattleResults( UnitContext attacker, UnitContext defender, MapMaster map )
  {
    return calculateBattleResults(attacker, defender, map, false);
  }

  /**
   * Assuming Commanders get weird, this allows for you to check the results of combat without perfect map info.
   * This also provides un-capped damage estimates, so perfect HP info isn't revealed by the map.
   */
  public static BattleSummary simulateBattleResults( Unit attacker, Unit defender, GameMap map, int attackerX, int attackerY )
  {
    return simulateBattleResults(attacker, defender, map, new XYCoord(attackerX, attackerY));
  }
  public static BattleSummary simulateBattleResults( Unit attacker, Unit defender, GameMap map, XYCoord moveCoord )
  {
    UnitContext attackerContext = new UnitContext(map, attacker, null, null, moveCoord);
    UnitContext defenderContext = new UnitContext(map, defender, null, null, new XYCoord(defender.x, defender.y));
    return calculateBattleResults(attackerContext, defenderContext, map, true);
  }
  public static BattleSummary simulateBattleResults( Unit attacker, Unit defender, GameMap map, GamePath path )
  {
    UnitContext attackerContext = new UnitContext(map, attacker, null, path, path.getEndCoord());
    UnitContext defenderContext = new UnitContext(map, defender, null, null, new XYCoord(defender.x, defender.y));
    return calculateBattleResults(attackerContext, defenderContext, map, true);
  }

  public static StrikeParams calculateTerrainDamage( Unit attacker, GamePath path, MapLocation target, GameMap map )
  {
    final XYCoord targetCoord = target.getCoordinates();
    int battleRange = path.getEndCoord().getDistance(targetCoord);
    UnitContext uc = new UnitContext(attacker);
    uc.map = map;
    uc.setPath(path);
    uc.chooseWeapon(target, battleRange);
    return StrikeParams.buildStrikeParams(uc, target, map, battleRange, targetCoord, false);
  }

  /**
   * Calculate and return the results of a battle.
   * <p>This will not actually apply the damage taken; that is done later in {@link BattleEvent}.
   * <p>Requires the coord field be defined for both attacker and defender.
   * @param isSim Determines whether to cap damage at the HP of the victim in question
   * @return A BattleSummary object containing all relevant details from this combat instance.
   */
  public static BattleSummary calculateBattleResults(UnitContext attacker, UnitContext defender,
                                                     GameMap map, boolean isSim)
  {
    int attackerX = attacker.coord.xCoord;
    int attackerY = attacker.coord.yCoord;
    int defenderX = defender.coord.xCoord;
    int defenderY = defender.coord.yCoord;

    int battleRange = Math.abs(attackerX - defenderX) + Math.abs(attackerY - defenderY);

    CombatContext context = CombatContext.build(map, attacker, defender, battleRange);

    // Provides a simple way to correlate start state and end state of each combatant.
    // Uses a map to make it easy to pass information coherently between this function's local context
    //   and the context of the CombatContext (which can be altered in unpredictable ways).
    Map<UnitContext, UnitContext> unitStateMap = new HashMap<UnitContext, UnitContext>();

    // Starting assumption is that nothing changed in the "combat"
    unitStateMap.put(attacker, new UnitContext(attacker));
    unitStateMap.put(defender, new UnitContext(defender));

    // From here on in, use context variables only

    BattleParams attackInstance = context.getAttack();

    double damage = attackInstance.calculateDamage();
    if( damage < 0 )
      damage = 0;
    unitStateMap.get(context.attacker).fire(context.attacker.weapon);
    unitStateMap.get(context.defender).damageHP(damage, isSim);

    // New battle instance with defender counter-attacking.
    BattleParams defendInstance = context.getCounterAttack(damage, isSim);
    if( null != defendInstance )
    {
      double counterDamage = defendInstance.calculateDamage();
      unitStateMap.get(context.defender).fire(context.defender.weapon);
      unitStateMap.get(context.attacker).damageHP(counterDamage, isSim);
    }

    // Consider throwing in a final hook here for UnitModifiers to change the result post-calculations.

    // Calculations complete.
    // Since we are setting up our BattleSummary, use non-CombatContext variables
    //   so consumers of the Summary will see results consistent with the current board/map state
    //   (e.g. the Unit 'attacker' actually belongs to the CO whose turn it currently is)
    return new BattleSummary(attacker, unitStateMap.get(attacker),
                             defender, unitStateMap.get(defender));
  }

  public static double calculateOneStrikeDamage( Unit attacker, int battleRange, Unit defender, GameMap map, int terrainStars, boolean attackerMoved )
  {
    if( attacker.model.weapons.size() < 1 )
      return 0;
    XYCoord dest = new XYCoord(attacker);
    UnitContext attackerContext = new UnitContext(map, attacker, null, null, dest);
    attackerContext.chooseWeapon(defender.model, battleRange, attackerMoved);
    return StrikeParams.buildBattleParams(
        attackerContext,
        new UnitContext(map, defender),
        map, battleRange,
        false).calculateDamage();
  }
}
