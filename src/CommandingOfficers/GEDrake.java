package CommandingOfficers;

import CommandingOfficers.Modifiers.COModifier;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.MapChangeEvent;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class GEDrake extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Drake", new instantiator());
  private static class instantiator extends COMaker
  {
    @Override
    public Commander create()
    {
      return new GEDrake();
    }
  }

  public GEDrake()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.AIR_HIGH || um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(20);
      }
      if( um.chassis == ChassisEnum.SHIP || um.chassis == ChassisEnum.SUBMERGED )
      {
        um.movePower += 1;
        um.modifyDefenseRatio(25);
      }

      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        um.propulsion.setMoveCost(Weathers.RAIN, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
      }
    }

    addCommanderAbility(new Tsunami(this));
    addCommanderAbility(new Typhoon(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }


  private static class Tsunami extends CommanderAbility
  {
    private static final String NAME = "Tsunami";
    private static final int COST = 4;

    Tsunami(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( int i = 0; i < gameMap.mapWidth; i++ )
      {
        for( int j = 0; j < gameMap.mapHeight; j++ )
        {
          Location loc = gameMap.getLocation(i, j);
          Unit victim = loc.getResident();
          if( victim != null && myCommander.isEnemy(victim.CO) )
          {
            victim.alterHP(-1);
            victim.fuel /= 2;
          }
        }
      }
    }
  }

  private static class Typhoon extends CommanderAbility
  {
    private static final String NAME = "Typhoon";
    private static final int COST = 7;

    Typhoon(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( int i = 0; i < gameMap.mapWidth; i++ )
      {
        for( int j = 0; j < gameMap.mapHeight; j++ )
        {
          Location loc = gameMap.getLocation(i, j);
          loc.setForecast(Weathers.RAIN, gameMap.commanders.length-1);
          Unit victim = loc.getResident();
          if( victim != null && myCommander.isEnemy(victim.CO) )
          {
            victim.alterHP(-2);
            victim.fuel /= 2;
          }
        }
      }
      GameEvent event = new MapChangeEvent();
      event.performEvent(gameMap);
      GameEventListener.publishEvent(event);
    }
  }
}
