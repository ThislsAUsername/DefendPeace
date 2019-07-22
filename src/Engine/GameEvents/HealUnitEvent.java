package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class HealUnitEvent implements GameEvent
{
  private Unit unit;
  public final int repairPower = 1;
  public final boolean liableForCosts;

  public HealUnitEvent(Unit aTarget, boolean payUp)
  {
    unit = aTarget;
    liableForCosts = payUp;
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
    if (!liableForCosts)
      unit.alterHP(repairPower);
    else
    {
      double HP = unit.getPreciseHP();
      int maxHP = unit.model.maxHP;

      if( HP < unit.model.maxHP )
      {
        int neededHP = Math.min(maxHP - unit.getHP(), repairPower); // Only pay for whole HPs
        double proportionalCost = unit.model.getCost() / maxHP;
        int repairedHP = neededHP;
        while (unit.CO.money < repairedHP * proportionalCost) // Only repair what we can afford
        {
          repairedHP--;
        }
        unit.CO.money -= repairedHP * proportionalCost;
        unit.alterHP(repairedHP);

        // Top off HP if there's excess power but we hit the HP cap
        if (repairedHP < repairPower && unit.getHP() == maxHP)
          unit.alterHP(1);
      }
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
