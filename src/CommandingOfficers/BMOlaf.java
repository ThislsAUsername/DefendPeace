package CommandingOfficers;

import CommandingOfficers.Modifiers.COModifier;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

public class BMOlaf extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Olaf", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BMOlaf();
    }
  }

  public BMOlaf()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        um.propulsion.setMoveCost(Weathers.RAIN, terrain, um.propulsion.getMoveCost(Weathers.SNOW, terrain));
        um.propulsion.setMoveCost(Weathers.SNOW, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
      }
    }

    addCommanderAbility(new Blizzard(this));
    addCommanderAbility(new WinterFury(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  /*
   * Sear causes 1 mass damage to Cinder's own troops, in exchange for refreshing them.
   */
  private static class Blizzard extends CommanderAbility implements COModifier
  {
    private static final String NAME = "Blizzard";
    private static final int COST = 3;
    GameMap map;

    Blizzard(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      map = gameMap;
      myCommander.addCOModifier(this);
    }

    @Override // COModifier interface.
    public void apply(Commander commander)
    {
      for( int i = 0; i < map.mapWidth; i++ )
      {
        for( int j = 0; j < map.mapHeight; j++ )
        {
          Location loc = map.getLocation(i, j);
          loc.setEnvironment(Environment.getTile(loc.getEnvironment().terrainType, Weathers.SNOW));
        }
      }
    }

    @Override
    public void revert(Commander commander)
    {
      for( int i = 0; i < map.mapWidth; i++ )
      {
        for( int j = 0; j < map.mapHeight; j++ )
        {
          Location loc = map.getLocation(i, j);
          loc.setEnvironment(Environment.getTile(loc.getEnvironment().terrainType, Weathers.CLEAR));
        }
      }
    }
  }

  /*
   * Witchfire causes Cinder's troops to automatically refresh after attacking, at the cost of 1 HP
   */
  private static class WinterFury extends CommanderAbility implements COModifier
  {
    private static final String NAME = "Winter Fury";
    private static final int COST = 7;
    GameMap map;

    WinterFury(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      map = gameMap;
      myCommander.addCOModifier(this);
    }

    @Override // COModifier interface.
    public void apply(Commander commander)
    {
      for( int i = 0; i < map.mapWidth; i++ )
      {
        for( int j = 0; j < map.mapHeight; j++ )
        {
          Location loc = map.getLocation(i, j);
          loc.setEnvironment(Environment.getTile(loc.getEnvironment().terrainType, Weathers.SNOW));
          Unit victim = loc.getResident();
          if( victim != null && myCommander.isEnemy(victim.CO) )
          {
            victim.alterHP(-2);
          }
        }
      }
    }

    @Override
    public void revert(Commander commander)
    {
      for( int i = 0; i < map.mapWidth; i++ )
      {
        for( int j = 0; j < map.mapHeight; j++ )
        {
          Location loc = map.getLocation(i, j);
          loc.setEnvironment(Environment.getTile(loc.getEnvironment().terrainType, Weathers.CLEAR));
        }
      }
    }
  }
}
