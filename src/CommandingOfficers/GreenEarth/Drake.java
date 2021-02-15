package CommandingOfficers.GreenEarth;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitModel;

public class Drake extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
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
    public Commander create(GameScenario.GameRules rules)
    {
      return new Drake(rules);
    }
  }

  public Drake(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isAirUnit() )
      {
        um.modifyDamageRatio(20);
      }
      if( um.isSeaUnit() )
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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Tsunami";
    private static final int COST = 4;

    Tsunami(Commander commander)
    {
      super(commander, NAME, COST);
      AIFlags |= PHASE_TURN_END;
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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Typhoon";
    private static final int COST = 7;

    Typhoon(Commander commander)
    {
      super(commander, NAME, COST);
      AIFlags |= PHASE_TURN_END;
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
    }
    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue abilityEvents = new GameEventQueue();

      GameEvent event = new GlobalWeatherEvent(Weathers.RAIN, 1);
      abilityEvents.add(event);

      return abilityEvents;
    }
  }
}

