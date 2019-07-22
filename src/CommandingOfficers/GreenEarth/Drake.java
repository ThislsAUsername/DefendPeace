package CommandingOfficers.GreenEarth;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GlobalWeatherEvent;
import Engine.GameEvents.MapChangeEvent;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class Drake extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Drake");
      infoPages.add(new InfoPage(
          "Drake\r\n" + 
          "  Naval units gain +1 movement and +25% defense, aircraft lose -20% attack. Unaffected by rain (except vision), more likely to rain in random weather\r\n" + 
          "Tsunami -- All enemy units lose 1 HP (to a minimum of 1). Enemy units lose half their fuel\r\n" + 
          "Typhoon -- All enemy units lose 2 HP (to a minimum of 1) and half their fuel. Weather changes to Rain (1 day)"));
    }
    @Override
    public Commander create()
    {
      return new Drake();
    }
  }

  public Drake()
  {
    super(coInfo);

    for( UnitModel um : unitModels.values() )
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
          Unit victim = loc.getResident();
          if( victim != null && myCommander.isEnemy(victim.CO) )
          {
            victim.alterHP(-2);
            victim.fuel /= 2;
          }
        }
      }
      GameEvent event = new GlobalWeatherEvent(Weathers.RAIN, 1);
      event.performEvent(gameMap);
      GameEventListener.publishEvent(event);
    }
  }
}

