package CommandingOfficers;

import CommandingOfficers.Modifiers.COModifier;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel;
import Units.MoveTypes.OlafFlight;
import Units.MoveTypes.OlafFloatHeavy;
import Units.MoveTypes.OlafFloatLight;
import Units.MoveTypes.OlafFootMech;
import Units.MoveTypes.OlafFootStandard;
import Units.MoveTypes.OlafTires;
import Units.MoveTypes.OlafTread;

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
          switch (um.chassis)
          {
            case AIR_HIGH:
            case AIR_LOW:
              um.propulsion = new OlafFlight();
              break;
            case SHIP:
              um.propulsion = new OlafFloatHeavy();
              break;
            case TANK:
              um.propulsion = new OlafTread();
            break;
            default:
              break;
          }
        }
        getUnitModel(UnitModel.UnitEnum.INFANTRY).propulsion = new OlafFootStandard();
        getUnitModel(UnitModel.UnitEnum.MECH).propulsion = new OlafFootMech();

        getUnitModel(UnitModel.UnitEnum.RECON).propulsion = new OlafTires();
        getUnitModel(UnitModel.UnitEnum.ROCKETS).propulsion = new OlafTires();
        getUnitModel(UnitModel.UnitEnum.MOBILESAM).propulsion = new OlafTires();

        getUnitModel(UnitModel.UnitEnum.LANDER).propulsion = new OlafFloatLight();

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
    private static final int COST = 5;
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
    private static final int COST = 9;
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
