package Engine.UnitActionLifecycles;

import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;

/**
 * Effectively a wait, but the unit ends up as a different unit at the end of it.
 * This action type requires a parameter (the unit to transform into), and thus
 * cannot be represented as a static global constant.
 */
public abstract class TransformLifecycle
{
  public static class TransformFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final UnitModel destinationType;
    public final String name;

    public TransformFactory(UnitModel type, String displayName)
    {
      destinationType = type;
      name = displayName;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new TransformAction(actor, movePath, this), false);
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return name;
    }
  }

  /** Effectively a WAIT, but the unit ends up as a different unit at the end of it. */
  public static class TransformAction extends WaitLifecycle.WaitAction
  {
    private TransformFactory type;
    Unit actor;

    public TransformAction(Unit unit, GamePath path, TransformFactory pType)
    {
      super(unit, path);
      type = pType;
      actor = unit;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue transformEvents = super.getEvents(gameMap);

      if( transformEvents.size() > 0 ) // if we successfully made a move action
      {
        GameEvent moveEvent = transformEvents.peek();
        if( moveEvent.getEndPoint().equals(getMoveLocation()) ) // make sure we shouldn't be pre-empted
        {
          transformEvents.add(new TransformEvent(actor, type.destinationType));
        }
      }
      return transformEvents;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and transform to %s]", actor.toStringWithLocation(), getMoveLocation(),
          type.destinationType);
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~TransformAction

  public static class TransformEvent implements GameEvent
  {
    private Unit unit;
    private UnitModel oldType;
    private UnitModel destinationType;

    public TransformEvent(Unit unit, UnitModel destination)
    {
      this.unit = unit;
      oldType = unit.model;
      destinationType = destination;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return null;
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return listener.receiveUnitTransformEvent(unit, oldType);
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      // TODO: Consider fiddling with ammo count
      unit.model = destinationType;
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
  } // ~Event

}
