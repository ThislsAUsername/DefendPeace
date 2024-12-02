package CommandingOfficers.AWBW.IDS;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import UI.UIUtils;
import Units.UnitContext;

public class Penny extends AWBWCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Penny", UIUtils.SourceGames.AWBW, UIUtils.IDS);
      infoPages.add(new InfoPage(
          "Penny (AWBW)\n"
        + "Units are unaffected by weather.\n"));
      infoPages.add(new InfoPage(new Enigma(null, null),
          "Summons Snow. Disables temporary fog.\n"
        + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new Stormfront(null, null),
          "Summons Rain for 3 days. Disables temporary fog.\n"
        + "+10 attack and defense.\n"));
      infoPages.add(AWBWCommander.AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Penny(rules);
    }
  }

  public Penny(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    immuneToCold   = true;
    immuneToClouds = true;
    immuneToSand   = true;

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Enigma(this, cb));
    addCommanderAbility(new Stormfront(this, cb));
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      final int clearCost = uc.moveType.getMoveCost(Weathers.CLEAR, terrain);
      uc.moveType.setMoveCost(terrain, clearCost);
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Enigma extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Enigma";
    private static final int COST = 2;

    Enigma(Penny commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GlobalWeatherEvent weather = new GlobalWeatherEvent(Weathers.SNOW, 1);
      weather.canCancelFog = true;

      GameEventQueue events = super.getEvents(map);
      events.add(weather);

      return events;
    }
  }

  private static class Stormfront extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Stormfront";
    private static final int COST = 6;

    Stormfront(Penny commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GlobalWeatherEvent weather = new GlobalWeatherEvent(Weathers.RAIN, 3);
      weather.canCancelFog = true;

      GameEventQueue events = super.getEvents(map);
      events.add(weather);

      return events;
    }
  }
}
