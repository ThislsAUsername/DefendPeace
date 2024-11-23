package CommandingOfficers.AWBW.IDS;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import UI.UIUtils;
import Units.Unit;
import Units.UnitContext;

public class Penny extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Penny", UIUtils.SourceGames.AW4, UIUtils.IDS);
      infoPages.add(new InfoPage(
          "The youngest child of Dr. Caulder. Numerous experiments have left her mind permanently shattered.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: 3\n"
        + "Units are unaffected by weather. This applies even when not in the CO Zone.\n"));
      infoPages.add(new InfoPage(new Stormfront(null),
          "Randomly changes the weather to Sleet (-1 move), Smoke (Fog of War), or Sirocco (-30 attack). The weather lasts for 3 days.\n"
        + "Won't roll the weather active on the COU's tile.\n"
        + "If Sleet or Sirocco is rolled, disables temporary fog.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Penny(rules);
    }
  }
  public static final int RADIUS  = 3;
  public static final int POWER   = 0;
  public static final int DEFENSE = 0;

  public Penny(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    immuneToCold   = true;
    immuneToClouds = true;
    immuneToSand   = true;

    addCommanderAbility(new Stormfront(this));
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

  private static class Stormfront extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Stormfront";

    Stormfront(Penny commander)
    {
      super(commander, NAME);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      ArrayList<Weathers> candidates = new ArrayList<>();
      candidates.add(Weathers.SLEET);
      candidates.add(Weathers.SMOKE);
      candidates.add(Weathers.SIROCCO);
      for( Unit u : COcast.COUs ) // Don't roll the same weather that's currently active
        candidates.remove(map.getEnvironment(u.x, u.y).weatherType);
      int rand = map.game.getRN(candidates.size());

      Weathers chosen = candidates.get(rand);
      GlobalWeatherEvent weather = new GlobalWeatherEvent(chosen, 3);
      weather.canCancelFog = true;

      GameEventQueue events = super.getEvents(map);
      events.add(weather);

      return events;
    }
  }
}
