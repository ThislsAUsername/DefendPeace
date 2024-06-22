package CommandingOfficers.AW3.GE;

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
import Terrain.Environment.Weathers;
import Units.Unit;

public class Drake extends AW3Commander
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
      super("Drake", UIUtils.SourceGames.AW3, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Drake (AW3)\n"
          + "A bighearted former pirate who hates fighting. Also a great surfer. Dude!\n"
          + "Naval units have superior firepower (+20) but air units have weaker attacks (-10).\n"));
      infoPages.add(new InfoPage(new Tsunami(null, new CommanderAbility.CostBasis(CHARGERATIO_AW3)),
            "Causes a tidal wave that does one HP of damage to all enemy units and reduces their fuel by half.\n"
          + "+10 attack and defense.\n"));
    infoPages.add(new InfoPage(new Typhoon(null, new CommanderAbility.CostBasis(CHARGERATIO_AW3)),
            "Causes a tidal wave that does two HP of damage to all enemy units and reduces their fuel by half. Produces Fog of War for one full day.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: The sea\n"
          + "Miss: High places"));
      infoPages.add(AW3_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Drake(rules);
    }
  }

  public Drake(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new Tsunami(this, cb));
    addCommanderAbility(new Typhoon(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAirUnit() )
      params.attackPower -= 10;
    if( params.attacker.model.isSeaUnit() )
      params.attackPower += 20;
  }

  private static class Tsunami extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Tsunami";
    private static final int COST = 4;

    Tsunami(Drake commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = 0; // Less efficient damage than SCOP
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      // TODO: Toss this to AW1 Drake
      ArrayList<Unit> victims = COutils.findMassDamageTargets(map, myCommander);

      GameEvent damage = new MassDamageEvent(myCommander, victims, 10, false);

      GameEventQueue events = new GameEventQueue();
      events.add(damage);

      return events;
    }
    @Override
    protected void perform(MapMaster map)
    {
      super.perform(map);
      ArrayList<Unit> victims = COutils.findMassDamageTargets(map, myCommander);
      for( Unit vic : victims )
      {
        vic.fuel /= 2; // Does this rate an event?
      }
    }
  }

  private static class Typhoon extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Typhoon";
    private static final int COST = 7;

    Typhoon(Drake commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEvent weather = new GlobalWeatherEvent(Weathers.SMOKE, 1);

      ArrayList<Unit> victims = COutils.findMassDamageTargets(map, myCommander);
      GameEvent damage = new MassDamageEvent(myCommander, victims, 20, false);

      GameEventQueue events = new GameEventQueue();
      events.add(weather);
      events.add(damage);

      return events;
    }
    @Override
    protected void perform(MapMaster map)
    {
      super.perform(map);
      ArrayList<Unit> victims = COutils.findMassDamageTargets(map, myCommander);
      for( Unit vic : victims )
      {
        vic.fuel /= 2; // Does this rate an event?
      }
    }
  }

}
