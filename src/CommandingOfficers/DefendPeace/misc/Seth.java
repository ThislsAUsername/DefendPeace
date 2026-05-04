package CommandingOfficers.DefendPeace.misc;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.CommanderAbilityRevertEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.StateTrackers.StateTracker;
import Engine.UnitActionLifecycles.TransformLifecycle.TransformEvent;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.GBAFEUnits.ClassStatsBuilder;
import Units.GBAFEUnits.GBAFEUnitModel;
import Units.GBAFEWeapons;
import Units.Unit;
import Units.UnitDelta;
import Units.UnitModel;
import Units.GBAFEActions.GBAFEExperienceTracker;
import Units.GBAFEActions.GBAFEStatsTracker;
import lombok.var;

public class Seth extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Seth", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.MISC);
      infoPages.add(new InfoPage(
          "Your first cavalier becomes Seth.\n" +
          "Only works in GBA Emblem."));
      infoPages.add(new InfoPage(
          "Vulnerary (3 uses):\n" +
          "Seth heals 3 HP."));
      infoPages.add(new InfoPage(
          "Silver Lance (20 uses):\n" +
          "Seth equips a Silver Lance. Each combat consumes a use, but activation does not."));
      infoPages.add(new InfoPage(
          "Concept credit:\n" +
          "@sum_buddy Discord ID 927387040478285824"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Seth(rules);
    }
  }

  GBAFEExperienceTracker expTracker = null;
  final GBAFEUnitModel cav;

  Unit COU = null;
  int silverUses = 20;
  boolean silverActive = false;

  public Seth(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    var tonk = getUnitModel(UnitModel.TANK | UnitModel.ASSAULT, false);
    if( tonk instanceof GBAFEUnitModel )
      cav = (GBAFEUnitModel) tonk;
    else
      cav = null;
    // You get your abilities once you have COU to use them on.
  }

  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    expTracker = StateTracker.instance(game, GBAFEExperienceTracker.class);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  // Transform the first cav, and buy it for free
  @Override
  public int getBuyCost(UnitModel um, XYCoord coord)
  {
    if( null != COU )
      return super.getBuyCost(um, coord);
    if( um != cav )
      return super.getBuyCost(um, coord);
    return 0;
  }
  @Override
  public GameEventQueue receiveCreateUnitEvent(Unit unit)
  {
    if( null != COU )
      return null;
    if( unit.CO != this )
      return null;
    if( unit.model != cav )
      return null;

    COU = unit;
    expTracker.promote(unit, cav.promotesTo);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 30;
    bases.baseStr = 14;
    bases.baseSkl = 13;
    bases.baseSpd = 12;
    bases.baseDef = 11;
    bases.baseRes =  8;
    bases.baseLck = 13;
    bases.growthHP  =  90;
    bases.growthStr =  50;
    bases.growthSkl =  45;
    bases.growthSpd =  45;
    bases.growthLck =  25;
    bases.growthDef =  40;
    bases.growthRes =  30;
    var stats = bases.build(0); // Lie about promotion so he doesn't get free stats
    stats.promoted = true;
    expTracker.statsTracker.statsMap.put(unit, stats);

    addCommanderAbility(new SilverLance(this));
    addCommanderAbility(new Vulnerary(this));

    GameEventQueue events = new GameEventQueue();
    events.add(new TransformEvent(unit, cav.promotesTo));
    events.add(new ResupplyEvent(null, unit));
    return events;
  }

  private static class Vulnerary extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Vulnerary";
    protected int maxUses = 3;
    protected Seth COcast;

    Vulnerary(Seth co)
    {
      super(co, NAME, 0);
      COcast = co;
      AIFlags = 0; // The AI doesn't know how to use this, so it shouldn't try
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      if( costBasis.numCasts >= maxUses )
        COcast.myAbilities.remove(this);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEventQueue events = new GameEventQueue();
      events.add(new HealUnitEvent(COcast.COU, 30, null));
      return events;
    }
  }

  // Effects of the silver lance power
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( !silverActive || params.attacker.unit != COU )
      return;
    GBAFEStatsTracker statTracker = StateTracker.instance(params.map.game, GBAFEStatsTracker.class);
    var stats = statTracker.getStats(params.attacker.unit);
    var gun = new GBAFEWeapons.SilverLance(stats);

    // Assume we're shooting terrain, since the base damage will get overwritten later otherwise
    params.baseDamage = gun.getDamage(stats, params.map.getEnvironment(params.targetCoord).terrainType);
  }

  @Override
  public void modifyUnitAttackOnUnit(BattleParams params)
  {
    if( !silverActive || params.attacker.unit != COU )
      return;
    GBAFEStatsTracker statTracker = StateTracker.instance(params.map.game, GBAFEStatsTracker.class);
    var stats = statTracker.getStats(params.attacker.unit);
    var gun = new GBAFEWeapons.SilverLance(stats);
    var defender = (GBAFEUnitModel) params.defender.model;
    var def_stats = statTracker.getStats(params.defender.unit);

    params.baseDamage = gun.getDamage(stats, defender, def_stats);
  }
//  @Override
//  public void modifyUnitAttack(StrikeParams params)
//  {
//    if( silverActive && params.attacker.unit == COU )
//      params.attackPower += 40;
//  }
//  @Override
//  public void modifyUnitDefenseAgainstUnit(BattleParams params)
//  {
//    if( silverActive && params.defender.unit == COU )
//      params.defenseSubtraction += 10;
//  }
  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary summary)
  {
    if( !silverActive )
      return null;
    boolean amAttacking = this == summary.attacker.CO;
    if( !amAttacking && this != summary.defender.CO )
      return null; // not attacking and not the defender -> not involved

    // We only care who the units belong to, not who picked the fight.
    final UnitDelta minion;
    final UnitDelta enemy;
    if( amAttacking )
    {
      minion = summary.attacker;
      enemy = summary.defender;
    }
    else
    {
      minion = summary.defender;
      enemy = summary.attacker;
    }

    if( COU != minion.unit )
      return null;
    if( enemy.deltaHealth >= 0 )
      return null; // We clearly did not stab

    --silverUses;
    if( silverUses <= 0 )
    {
      silverActive = false;
      GameEventQueue events = new GameEventQueue();
      events.add(new CommanderAbilityRevertEvent(myActiveAbility));
      myAbilities.remove(myActiveAbility);
      myActiveAbility = null;
      return events;
    }

    return null;
  }

  private static class SilverLance extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Silver Lance";
    protected Seth COcast;

    SilverLance(Seth co)
    {
      super(co, NAME, 0);
      COcast = co;
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.silverActive = true;
    }
    @Override
    protected void revert(MapMaster gameMap)
    {
      COcast.silverActive = false;
    }
  }

}
