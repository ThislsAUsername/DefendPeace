package Engine.GameEvents;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;

public class HealUnitEvent implements GameEvent
{
  private Unit unit;
  public final int repairPowerHealth;
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
    // TODO: Consider making all repairs/healing go through this event before making a listen event for this
    return null;
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if (null == payer)
    {
      unit.alterHealth(repairPowerHealth, unit.CO.roundUpRepairs, canOverheal);
      return;
    }

    int costPerHealth = unit.getRepairCost() / UnitModel.MAXIMUM_HEALTH;

    int actualRepair = repairPowerHealth;
    if( costPerHealth > 0 )
    {
      int affordableHealth = payer.money / costPerHealth;
      actualRepair = Math.min(repairPowerHealth, affordableHealth);
    }

    int deltaHealth = unit.alterHealth(actualRepair, unit.CO.roundUpRepairs, canOverheal);
    payer.money -= deltaHealth * costPerHealth;
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
