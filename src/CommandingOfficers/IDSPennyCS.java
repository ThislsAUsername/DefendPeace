package CommandingOfficers;

import Terrain.Environment.Weathers;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Units.UnitModel;

public class IDSPennyCS extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Penny\nCS", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new IDSPennyCS();
    }
  }

  public IDSPennyCS()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        um.propulsion.setMoveCost(Weathers.RAIN, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
        um.propulsion.setMoveCost(Weathers.SNOW, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
        um.propulsion.setMoveCost(Weathers.SANDSTORM, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
      }
    }

    addCommanderAbility(new Stormfront(this));
    addCommanderAbility(new Enigma(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Stormfront extends CommanderAbility
  {
    private static final String NAME = "Stormfront";
    private static final int COST = 3;

    Stormfront(Commander commander)
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
          loc.setForecast(Weathers.SNOW, gameMap.commanders.length - 1);
        }
      }
    }
  }

  private static class Enigma extends CommanderAbility
  {
    private static final String NAME = "Enigma";
    private static final int COST = 8;

    Enigma(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
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
          loc.setForecast(Weathers.RAIN, gameMap.commanders.length * 3 - 1);
        }
      }
    }
  }
}
