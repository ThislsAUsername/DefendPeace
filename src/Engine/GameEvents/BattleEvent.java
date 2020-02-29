package Engine.GameEvents;

import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
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
  private final XYCoord defenderCoords;

  public BattleEvent(Unit attacker, Unit defender, int attackerX, int attackerY, MapMaster map)
  {
    // Calculate the result of the battle immediately. This will allow us to plan the animation.
    battleInfo = CombatEngine.calculateBattleResults(attacker, defender, map, attackerX, attackerY);
    defenderCoords = new XYCoord(defender.x, defender.y);
  }

  public Unit getAttacker()
  {
    return battleInfo.attacker;
  }
  public boolean attackerDies()
  {
    return battleInfo.attacker.getPreciseHP() - battleInfo.attackerHPLoss <= 0;
  }

  public Unit getDefender()
  {
    return battleInfo.defender;
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
    attacker.fire(battleInfo.attackerWeapon); // expend ammo
    defender.damageHP( battleInfo.defenderHPLoss );

    // Handle counter-attack if relevant.
    if( battleInfo.attackerHPLoss > 0 )
    {
      defender.fire(battleInfo.attackerWeapon);
      attacker.damageHP( battleInfo.attackerHPLoss );
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return defenderCoords;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return defenderCoords;
  }
}
