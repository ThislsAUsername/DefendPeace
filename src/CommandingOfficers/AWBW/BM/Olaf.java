package CommandingOfficers.AWBW.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import Engine.GameEvents.MassDamageEvent;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitContext;

public class Olaf extends AWBWCommander
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
      super("Olaf", UIUtils.SourceGames.AWBW, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Olaf (AWBW)\n"
          + "Unaffected by snow, but rain affects him the same as snow would for others.\n"));
      infoPages.add(new InfoPage(new Blizzard(null, null),
            "Changes the weather to Snow for 1 day.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new WinterFury(null, null),
            "Enemy units lose 2 HP (to a minimum of 0.1 HP), and the weather changes to snow for 1 day.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Blizzard(this, cb));
    addCommanderAbility(new WinterFury(this, cb));
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

  private static class Blizzard extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Blizzard";
    private static final int COST = 3;

    Blizzard(Olaf commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = 0;
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

  private static class WinterFury extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Winter Fury";
    private static final int COST = 7;

    WinterFury(Olaf commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEvent weather = new GlobalWeatherEvent(Weathers.SNOW, 1);

      ArrayList<Unit> victims = COutils.findMassDamageTargets(map, myCommander);
      GameEvent damage = new MassDamageEvent(myCommander, victims, 20, false);

      GameEventQueue events = new GameEventQueue();
      events.add(weather);
      events.add(damage);

      return events;
    }
  }

}
