package CommandingOfficers.AW2.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
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

public class Drake extends AW2Commander
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
      super("Drake_2", UIUtils.SourceGames.AW2, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Drake (AW2)\n"
          + "A bighearted former pirate, he often calms the waters between Jess and Eagle.\n"
          + "Naval units move 1 space more than other armies' do, and their defenses are higher (+10). Movement isn’t affected by rain.\n"
          + "(-30 attack for air)\n"));
      infoPages.add(new InfoPage(new Tsunami(null, new CommanderAbility.CostBasis(CHARGERATIO_FUNDS)),
            "Causes a tidal wave that does 1 HP to all enemy units and reduces their fuel by half.\n"
          + "+10 defense\n"));
      infoPages.add(new InfoPage(new Typhoon(null, new CommanderAbility.CostBasis(CHARGERATIO_FUNDS)),
            "Causes a tidal wave that does 2 HP of damage to all enemy units and reduces fuel by half. Hinders enemy movement.\n"
          + "+10 defense\n"));
      infoPages.add(new InfoPage(
            "Hit: The open sea\n"
          + "Miss: High Places"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
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

  private static class Tsunami extends AW2Ability
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

  private static class Typhoon extends AW2Ability
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
      GameEvent weather = new GlobalWeatherEvent(Weathers.RAIN, 1);

      // TODO: Toss this to AW1 Drake
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
