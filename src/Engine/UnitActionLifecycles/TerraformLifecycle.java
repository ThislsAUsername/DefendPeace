package Engine.UnitActionLifecycles;

import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitContext;

public abstract class TerraformLifecycle
{
  public static class TerraformFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final TerrainType startType, endType;
    public final String name;

    public TerraformFactory(TerrainType typeFrom, TerrainType typeTo, String displayName)
    {
      startType = typeFrom;
      endType = typeTo;
      name = displayName;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        if( startType == map.getLocation(moveLocation).getEnvironment().terrainType
            && actor.materials > 0 )
        {
          return new GameActionSet(new TerraformAction(actor, movePath, this), false);
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return name;
    }
  } // ~Factory

  public static class TerraformAction extends WaitLifecycle.WaitAction
  {
    private Unit actor = null;
    private TerraformFactory type;

    public TerraformAction(Unit unit, GamePath path, TerraformFactory pType)
    {
      super(unit, path);
      actor = unit;
      type = pType;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      // TERRAFORM actions consist of
      //   MOVE
      //   TERRAFORM
      GameEventQueue terraformEvents = super.getEvents(map);

      if( terraformEvents.size() < 1 ) // Fail out if invalid
        return terraformEvents;
      if( actor.materials < 1 ) // Fail out if we don't have materials
        return terraformEvents;

      GameEvent moveEvent = terraformEvents.peek();
      if( !moveEvent.getEndPoint().equals(getMoveLocation()) ) // Fail out if pre-empted
        return terraformEvents;

      terraformEvents.add(new TerraformEvent(actor, getMoveLocation(), type.endType));

      return terraformEvents;
    }

    @Override
    public String toString()
    {
      return String.format("[Terraform %s to %s at %s with %s]", type.startType, type.endType, getMoveLocation(),
          actor.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~Action

  public static class TerraformEvent extends MapChangeEvent
  {
    private Unit unit = null;
    final int terraformProgress;
    final int priorTerraformAmount;

    public TerraformEvent(Unit u, XYCoord loc, TerrainType endType)
    {
      super(loc, Environment.getTile(endType, Weathers.CLEAR));
      unit = u;
      XYCoord unitXY = new XYCoord(u.x, u.y);
      priorTerraformAmount = (unitXY.equals(loc) ? unit.getCaptureProgress() : 0);
      UnitContext uc = new UnitContext(unit);
      terraformProgress = uc.calculateCapturePower();
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      // Only attempt to do the action if it is valid to do so.
      if( unit.capture(gameMap) )
      {
        unit.materials -= 1;
        super.performEvent(gameMap);
      }
    }
  } // ~Event
}
