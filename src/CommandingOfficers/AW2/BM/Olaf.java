package CommandingOfficers.AW2.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
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

public class Olaf extends AW2Commander
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
      super("Olaf_2", UIUtils.SourceGames.AW2, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Olaf (AW2)\n"
          + "Plans often go awry, but he’s deadly serious.\n"
          + "Winter weather poses no problem for Olaf or his troops. Rain, however, does. His gruff nature belies his overall competence.\n"
          + "(Normal movement in snow, snow movement in rain.)"));
      infoPages.add(new InfoPage(new Blizzard(null, new CommanderAbility.CostBasis(CHARGERATIO_FUNDS)),
            "Causes snow to fall which adversely affects all units except his own.\n"
          + "+10 defense\n"));
      infoPages.add(new InfoPage(new WinterFury(null, new CommanderAbility.CostBasis(CHARGERATIO_FUNDS)),
            "A mighty blizzard reduces enemy movement and causes 2 HP of damage to all deployed enemy troops.\n"
          + "+10 defense\n"));
      infoPages.add(new InfoPage(
            "Hit: Warm Boots\n"
          + "Miss: Rain Clouds"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
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

  private static class Blizzard extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Blizzard";
    private static final int COST = 3;

    Blizzard(Olaf commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = 0; // This would be an interesting power if SCOP only did 1 damage
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

  private static class WinterFury extends AW2Ability
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
