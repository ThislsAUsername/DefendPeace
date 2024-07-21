package CommandingOfficers.AW1.BM;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.UnitContext;

public class Olaf extends AW1Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Olaf", UIUtils.SourceGames.AW1, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Olaf (AW1)\n"
          + "Plans often go awry, but he's deadly serious.\n"
          + "Strong in the snow, weak in the rain. A solid CO of above-average ability.\n"
          + "(Normal movement in snow, snow movement in rain.)"));
      infoPages.add(new InfoPage(new Blizzard(null),
            "Causes it snow creating favorable conditions for his units.\n"
          + "1.1x/0.9x damage dealt/taken.\n"));
      infoPages.add(new InfoPage(
            "Hit: Warm Boots\n"
          + "Miss: Rain Clouds"));
      infoPages.add(AW1_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Olaf(rules);
    }
  }

  public Olaf(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    immuneToCold = true;

    addCommanderAbility(new Blizzard(this));
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      uc.moveType.setMoveCost(Weathers.RAIN, terrain, uc.moveType.getMoveCost(Weathers.SNOW, terrain));
      uc.moveType.setMoveCost(Weathers.SNOW, terrain, uc.moveType.getMoveCost(Weathers.CLEAR, terrain));
    }
  }

  private static class Blizzard extends AW1BasicAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Blizzard";
    private static final int COST = 6;

    Blizzard(Olaf commander)
    {
      super(commander, NAME, COST);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEvent event = new GlobalWeatherEvent(Weathers.SNOW, 1);

      GameEventQueue events = new GameEventQueue();
      events.add(event);

      return events;
    }
  }

}
