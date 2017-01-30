package Engine.GameEvents;

import Engine.CombatEngine;
import Engine.Combat.BattleSummary;
import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * AttackEvent handles a single battle event between two units. The outcome is
 * calculated as soon as the AttackEvent is created; the result is applied when
 * performEvent is called. The results can be fetched
 */
public class BattleEvent implements GameEvent
{
  private final BattleSummary battleInfo;

  public BattleEvent(Unit attacker, Unit defender, int attackerX, int attackerY, GameMap map)
  {
    // Calculate the result of the battle immediately. This will allow us to plan the animation.
    battleInfo = CombatEngine.calculateBattleResults(attacker, defender, map, attackerX, attackerY);
  }

  public boolean attackerDies()
  {
    return battleInfo.attacker.getPreciseHP() - battleInfo.attackerHPLoss <= 0;
  }

  public boolean defenderDies()
  {
    return battleInfo.defender.getPreciseHP() - battleInfo.defenderHPLoss <= 0;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildBattleAnimation( battleInfo );
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    // Apply the battle results that we calculated previously.
    Unit attacker = battleInfo.attacker;
    Unit defender = battleInfo.defender;
    attacker.fire( defender ); // Lets the unit know that it has actually fired a shot.
    defender.damageHP( battleInfo.defenderHPLoss );

    // Handle counter-attack if relevant.
    if( battleInfo.attackerHPLoss > 0 )
    {
      defender.fire( attacker );
      attacker.damageHP( battleInfo.attackerHPLoss );
    }

    // TODO: Handle ammo.

    if( attacker.getHP() <= 0 )
    {
      gameMap.removeUnit(attacker);
      attacker.CO.units.remove(attacker);
    }
    if( defender.getHP() <= 0 )
    {
      gameMap.removeUnit(defender);
      defender.CO.units.remove(defender);
    }
  }
}
