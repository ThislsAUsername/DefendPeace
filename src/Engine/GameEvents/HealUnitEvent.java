package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class HealUnitEvent implements GameEvent
{
  private Unit unit;
  public final int repairPowerHP;
  public final Commander payer;

  public HealUnitEvent(Unit aTarget, int HP, Commander pPayer)
  {
    unit = aTarget;
    repairPowerHP = HP;
    payer = pPayer;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    // TODO: Consider making all repairs/healing go through this event before making a listen event for this
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if (null == payer)
      unit.alterHP(repairPowerHP);
    else if( unit.isHurt() )
    {
      int costPerHP = (int) (unit.model.getCost() / unit.model.maxHP * payer.getRepairCostFactor());

      int actualRepair = repairPowerHP;
      if( costPerHP > 0 )
      {
        int affordableHP = payer.money / costPerHP;
        actualRepair = Math.min(repairPowerHP, affordableHP);
      }

      int deltaHP = unit.alterHP(actualRepair);
      payer.money -= deltaHP * costPerHP;
    }
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
