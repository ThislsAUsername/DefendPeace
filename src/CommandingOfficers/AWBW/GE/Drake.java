package CommandingOfficers.AWBW.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
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

public class Drake extends AWBWCommander
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
      super("Drake", UIUtils.SourceGames.AWBW, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Drake (AWBW)\n"
          + "Naval units gain +1 movement and +10 defense. Air units lose -30% attack. Unaffected by rain (except vision), and has a higher chance of Rain in random weather.\n"));
      infoPages.add(new InfoPage(new Tsunami(null, null),
            "All enemy units lose 1 HP (to a minimum of 0.1 HP) and half their fuel.\n"
          + "+10 attack and defense.\n"));
    infoPages.add(new InfoPage(new Typhoon(null, null),
            "All enemy units lose 2 HP (to a minimum of 0.1 HP) and half their fuel. Weather changes to rain for 1 day.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Tsunami(this, cb));
    addCommanderAbility(new Typhoon(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAirUnit() )
      params.attackPower -= 30;
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.model.isSeaUnit() )
      uc.movePower += 1;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isSeaUnit() )
      params.defenseSubtraction += 10;
  }
  @Override
  public void modifyMoveType(UnitContext uc)
  {
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      uc.moveType.setMoveCost(Weathers.RAIN, terrain, uc.moveType.getMoveCost(Weathers.CLEAR, terrain));
    }
  }

  private static class Tsunami extends AWBWAbility
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

  private static class Typhoon extends AWBWAbility
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
