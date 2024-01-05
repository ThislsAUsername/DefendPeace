package CommandingOfficers.AW4.IDS;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import Terrain.MapMaster;
import Terrain.Environment.Weathers;
import UI.UIUtils;

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
          "Randomly changes the weather to Sleet (-1 move), Smoke (Fog of War), or Sirocco (-30 attack). The weather lasts for 3 days.\n"));
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
      Weathers[] candidates = { Weathers.SLEET, Weathers.SMOKE, Weathers.SIROCCO };
      int rand = map.game.getRN(candidates.length);

      Weathers chosen = candidates[rand];
      GameEvent weather = new GlobalWeatherEvent(chosen, 3);

      GameEventQueue events = super.getEvents(map);
      events.add(weather);

      return events;
    }
  }
}
