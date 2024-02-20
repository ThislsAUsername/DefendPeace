package CommandingOfficers.AW3.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
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

public class Olaf extends AW3Commander
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
      super("Olaf_3", UIUtils.SourceGames.AW3, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Olaf (AW3)\n"
          + "A pompous braggart, but his tactical prowess has earned him the respect of his peers.\n"
          + "Winter weather poses no problem for Olaf or his troops. Snow causes his firepower to rise, and his troops can move with no penalty.\n"
          + "(Normal movement and +20 attack in cold weather.)"));
      infoPages.add(new InfoPage(new Blizzard(null, new CommanderAbility.CostBasis(CHARGERATIO_AW3)),
            "Causes cold weather (doubled fuel consumption) for two days.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new WinterFury(null, new CommanderAbility.CostBasis(CHARGERATIO_AW3)),
            "A mighty blizzard causes two HP of damage to all enemy troops. The cold weather also persists for two days.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Warm Boots\n"
          + "Miss: Rain Clouds"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new Blizzard(this, cb));
    addCommanderAbility(new WinterFury(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.env.weatherType.isCold )
      params.attackPower += 20;
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      uc.moveType.setMoveCost(Weathers.SNOW, terrain, uc.moveType.getMoveCost(Weathers.CLEAR, terrain));
    }
  }

  private static class Blizzard extends AW3Ability
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
      GameEvent event = new GlobalWeatherEvent(Weathers.CHILL, 2);

      GameEventQueue events = new GameEventQueue();
      events.add(event);

      return events;
    }
  }

  private static class WinterFury extends AW3Ability
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
      GameEvent weather = new GlobalWeatherEvent(Weathers.CHILL, 2);

      ArrayList<Unit> victims = COutils.findMassDamageTargets(map, myCommander);
      GameEvent damage = new MassDamageEvent(myCommander, victims, 20, false);

      GameEventQueue events = new GameEventQueue();
      events.add(weather);
      events.add(damage);

      return events;
    }
  }

}
