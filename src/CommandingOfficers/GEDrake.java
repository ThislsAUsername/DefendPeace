package CommandingOfficers;

import CommandingOfficers.Modifiers.COModifier;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class GEDrake extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Drake", new instantiator());
  private static class instantiator implements COMaker
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


  private static class Tsunami extends CommanderAbility implements COModifier
  {
    private static final String NAME = "Tsunami";
    private static final int COST = 4;
    GameMap map;

    Tsunami(Commander commander)
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
          Unit victim = loc.getResident();
          if( victim != null && myCommander.isEnemy(victim.CO) )
          {
            victim.alterHP(-1);
            victim.fuel /= 2;
          }
        }
      }
    }

    @Override
    public void revert(Commander commander)
    {}
  }

  private static class Typhoon extends CommanderAbility implements COModifier
  {
    private static final String NAME = "Typhoon";
    private static final int COST = 7;
    GameMap map;

    Typhoon(Commander commander)
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
          loc.setEnvironment(Environment.getTile(loc.getEnvironment().terrainType, Weathers.RAIN));
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
