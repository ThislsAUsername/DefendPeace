package CommandingOfficers.IDS;

import Terrain.Environment.Weathers;
import Terrain.MapMaster;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GlobalWeatherEvent;
import Terrain.TerrainType;
import Units.UnitModel;

public class PennyRNG extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Penny RNG");
      infoPages.add(new InfoPage(
          "Hey, you're not allowed to see this...\n"
          + "Get out of here with your shenanigans!"));
    }
    @Override
    public Commander create()
    {
      return new PennyRNG();
    }
  }

  public PennyRNG()
  {
    super(coInfo);

    for( UnitModel um : unitModels.values() )
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
      GameEvent event = new GlobalWeatherEvent((Math.random() < 0.5) ? Weathers.SNOW : Weathers.RAIN, 1);
      event.performEvent(gameMap);
      GameEventListener.publishEvent(event);
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
      GameEvent event = new GlobalWeatherEvent((Math.random() < 0.5) ? Weathers.SNOW : Weathers.RAIN, 3);
      event.performEvent(gameMap);
      GameEventListener.publishEvent(event);
    }
  }
}

