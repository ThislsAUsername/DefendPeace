package Engine.GameEvents;

import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * BattleEvent handles a single battle event between two units. The outcome is
 * calculated as soon as the BattleEvent is created; the result is applied when
 * performEvent is called. The results can be seen via the provided functions.
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
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveBattleEvent( battleInfo );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    // Apply the battle results that we calculated previously.
    Unit attacker = battleInfo.attacker;
    Unit defender = battleInfo.defender;
    battleInfo.attackerWeapon.fire(); // expend ammo
    defender.damageHP( battleInfo.defenderHPLoss );

    // Handle counter-attack if relevant.
    if( battleInfo.attackerHPLoss > 0 )
    {
      battleInfo.defenderWeapon.fire();
      attacker.damageHP( battleInfo.attackerHPLoss );
    }

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
  
  @Override // there's no known way for this to fail after the GameAction is constructed
  public boolean shouldPreempt(MapMaster gameMap )
  {
    return false;
  }
}
