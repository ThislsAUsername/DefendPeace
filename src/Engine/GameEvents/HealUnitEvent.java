package Engine.GameEvents;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class HealUnitEvent implements GameEvent
{
  private Unit unit;
  public final int repairPowerHealth;
  private int healAmount = 0;
  public final Army payer;
  public final boolean canOverheal;

  public HealUnitEvent(Unit aTarget, int health, Army pPayer)
  {
    this(aTarget, health, pPayer, false);
  }
  public HealUnitEvent(Unit aTarget, int health, Army pPayer, boolean canOverheal)
  {
    unit = aTarget;
    repairPowerHealth = health;
    payer = pPayer;
    this.canOverheal = canOverheal;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveHealEvent(payer, unit, repairPowerHealth, healAmount);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    healAmount = healAtCost(payer, unit, repairPowerHealth, unit.CO.roundUpRepairs, canOverheal);
  }

  /**
   * @return The (visible) health increase
   */
  public static int healAtCost(Army payer, Unit unit, int healHealth, boolean roundUp, boolean canOverheal)
  {
    if (null == payer)
    {
      return unit.alterHealth(healHealth, roundUp, canOverheal);
    }

    int costPerHP = unit.getRepairCost() / 10;

    int actualRepair = healHealth;
    if( costPerHP > 0 )
    {
      int affordableHealth = (payer.money / costPerHP) * 10;
      actualRepair = Math.min(healHealth, affordableHealth);
    }

    int deltaHealth = unit.alterHealth(actualRepair, roundUp, canOverheal);
    payer.money -= (deltaHealth / 10) * costPerHP;
    return deltaHealth;
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }
}
