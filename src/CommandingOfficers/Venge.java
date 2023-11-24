package CommandingOfficers;

import java.util.ArrayList;

import Engine.Army;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatContext;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitFightStatModifier;
import Engine.UnitMods.UnitInstanceFilter;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.Unit;
import Units.UnitModel;

/*
 * Venge enhances counter-attacks of all sorts.
 */
public class Venge extends Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Venge", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.MISC);
      infoPages.add(new InfoPage(
          "Commander Venge likes to get vengeance for any slight.\n" +
          "Attacking Venge is not always difficult, but you may not like the consequences.\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" +
          "- Baseline stats are lower than normal; -10 defense and attack.\n" +
          "- After being attacked, Venge gets a bonus of +"+VENGEANCE_BOOST+"% attack against the unit that picked the fight.\n" +
          "- Units that Venge can get vengeance on are marked with a V.\n"));
      infoPages.add(new InfoPage(
          IronWill.NAME+" ("+IronWill.COST+"):\n" +
          "Only affects units that have not yet acted.\n" +
          "Grants +"+IronWill.IRONWILL_BOOST+" offense and defense\n" +
          "Your units resist damage and counterattack as if at full HP.\n" +
          "When the power ends, your units lose "+IronWill.IRONWILL_WOUND+" HP (nonlethal)\n"));
      infoPages.add(new InfoPage(
          Retribution.NAME+" ("+Retribution.COST+"):\n" +
          "Gives an attack boost of +"+Retribution.RETRIBUTION_BUFF+"%\n" +
          "Gives a defense penalty of +"+Retribution.RETRIBUTION_NERF+"%\n" +
          "Units now counterattack before they are hit.\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Venge(rules);
    }
  }

  /** A list of all the units that have attacked me since my last turn. */
  private ArrayList<Unit> aggressors = new ArrayList<Unit>();
  private IronWill myIronWill = new IronWill(this);
  /** How much power I get when beating them up */
  public final static int VENGEANCE_BOOST = 60;

  public Venge(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addUnitModifier(new UnitFightStatModifier(-10));

    addCommanderAbility(myIronWill);
    addCommanderAbility(new Retribution(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    GameEventQueue events = super.initTurn(map);
    return events;
  }
  @Override
  public void endTurn()
  {
    super.endTurn();
    aggressors.clear();
  }

  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    // If we can get a vengeance boost against this unit, let our player know.
    if( aggressors.contains(unit) )
      return 'V';
    // If we ever allow COs other than our own to *activate* abilities, then this is gonna have to move to a StateTracker
    if( myIronWill.boostedUnits.contains(unit) )
      return 'I';

    return super.getUnitMarking(unit, activeArmy);
  }

  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    if (aggressors.contains(join.unitDonor))
      aggressors.add(join.unitRecipient);
    return null;
  }

  public static class PreEmptiveCounterMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void changeCombatContext(CombatContext instance)
    {
      // If we're swapping, and we can counter, and we're on the defensive, do the swap.
      if( instance.canCounter && instance.defender.mods.contains(this) )
      {
        instance.swapCombatants();
      }
    }
  }

  public static class IronWillMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    public final int buff;
    public IronWillMod(int buff)
    {
      this.buff = buff;
    }
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      if( params.isCounter )
      {
        params.attackerHP = UnitModel.MAXIMUM_HP;
      }
      params.attackPower += buff;
    }
    @Override
    public void modifyUnitDefenseAgainstUnit(BattleParams params)
    {
      params.defenderHP = UnitModel.MAXIMUM_HP;
      params.defenseSubtraction += buff;
    }
  }

  @Override
  public void modifyUnitAttackOnUnit(BattleParams params)
  {
    if( aggressors.contains(params.defender.unit) )
    {
      // Boost attack if it's time to avenge slights
      params.attackPower += VENGEANCE_BOOST;
    }
  }

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary battleInfo)
  {
    super.receiveBattleEvent(battleInfo);
    // Determine if we were attacked. If so, record this misdeed.
    if( this == battleInfo.defender.CO )
    {
      aggressors.add(battleInfo.attacker.unit);
    }
    return null;
  }

  private static class IronWill extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Iron Will";
    private static final int COST = 4;
    private static final int IRONWILL_BOOST = 40;
    private static final int IRONWILL_WOUND = 2;
    private final ArrayList<Unit> boostedUnits = new ArrayList<Unit>();

    IronWill(Venge venge)
    {
      super(venge, NAME, COST);
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      UnitInstanceFilter uif = new UnitInstanceFilter(new IronWillMod(IRONWILL_BOOST));
      for( Unit unit : myCommander.army.getUnits() )
      {
        if( !unit.isTurnOver )
          uif.instances.add(unit);
      }
      modList.add(uif);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.army.getUnits() )
      {
        if( !unit.isTurnOver )
          boostedUnits.add(unit);
      }
    }
    @Override
    protected void revert(MapMaster gameMap)
    {
      boostedUnits.clear();
    }

    @Override
    public GameEventQueue getRevertEvents(MapMaster gameMap)
    {
      GameEventQueue events = new GameEventQueue();
      events.add(new MassDamageEvent(myCommander, boostedUnits, IRONWILL_WOUND, false));
      return events;
    }
  }

  /**
   * Retribution trades defense for firepower, and allows Venge to counter-attack first.
   */
  private static class Retribution extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Retribution";
    private static final int COST = 8;
    private static final int RETRIBUTION_BUFF = 40; // Trade defense for offense, since we hit before our attacker does.
    private static final int RETRIBUTION_NERF = 20;
    UnitModifier damageMod = null;
    UnitModifier defenseMod = null;

    Retribution(Venge venge)
    {
      super(venge, NAME, COST);
      damageMod = new UnitDamageModifier(RETRIBUTION_BUFF);
      defenseMod = new UnitDefenseModifier(-RETRIBUTION_NERF);
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(damageMod);
      modList.add(defenseMod);
      modList.add(new PreEmptiveCounterMod());
    }
  }
  
}
