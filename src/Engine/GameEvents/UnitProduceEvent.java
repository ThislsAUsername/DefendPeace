package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;

public class UnitProduceEvent implements GameEvent
{
  private final Commander myCommander;
  private Unit builder;
  private final Unit myNewUnit;

  public UnitProduceEvent(Commander commander, Unit unit, UnitModel model)
  {
    myCommander = commander;
    builder = unit;

    // TODO: Consider breaking the fiscal part into its own event.
    if( model.getCost() <= commander.money )
    {
      myNewUnit = new Unit(myCommander, model);
    }
    else
    {
      System.out.println("WARNING! Attempting to build unit with insufficient funds.");
      myNewUnit = null;
    }
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    if( null != myNewUnit )
    {
      listener.receiveCreateUnitEvent(myNewUnit);
    }
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( null != myNewUnit )
    {
      myCommander.money -= myNewUnit.model.getCost();
      builder.materials -= 1;
      myCommander.units.add(myNewUnit);
      builder.heldUnits.add(myNewUnit);
      builder.isTurnOver = true;
    }
    else
    {
      System.out.println("Warning! Attempting to build unit with insufficient funds.");
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(builder.x, builder.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return new XYCoord(builder.x, builder.y);
  }
}
