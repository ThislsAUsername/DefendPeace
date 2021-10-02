package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.DynamicModifier;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatContext;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.UnitMods.UnitModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;

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
      super("Venge");
      infoPages.add(new InfoPage(
          "Commander Venge likes to get vengeance for any slight.\n" +
          "Attacking Venge is not always difficult, but you may not like the consequences.\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" + 
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
  private IronWill myIronWill = new IronWill();
  /** How much power I get when beating them up */
  public final static int VENGEANCE_BOOST = 50;

  public Venge(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(myIronWill);
    addCommanderAbility(new Retribution());
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
  public char getUnitMarking(Unit unit)
  {
    // If we can get a vengeance boost against this unit, let our player know.
    if (aggressors.contains(unit))
      return 'V';
    // If we ever allow COs other than our own to *activate* abilities, then this is gonna have to move to a StateTracker
    if (myIronWill.boostedUnits.contains(unit))
      return 'I';
    
    return super.getUnitMarking(unit);
  }

  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    if (aggressors.contains(join.unitDonor))
      aggressors.add(join.unitRecipient);
    return null;
  }

  public static class PreEmptiveCounterMod implements UnitModifier
  {
    public final Commander co;
    public PreEmptiveCounterMod(Commander co)
    {
      super();
      this.co = co;
    }

    @Override
    public void changeCombatContext(CombatContext instance)
    {
      // If we're swapping, and we can counter, and we're on the defensive, do the swap.
      if( instance.canCounter && co == instance.defender.CO )
      {
        UnitContext minion = instance.defender;

        instance.defender = instance.attacker;
        instance.attacker = minion;
      }
    }
  }

  public static class IronWillMod implements UnitModifier
  {
    public final int buff;
    public IronWillMod(int buff)
    {
      super();
      this.buff = buff;
    }
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      if( params.isCounter )
      {
        params.attackerHP = params.attacker.model.maxHP;
      }
      params.attackPower += buff;
    }
    @Override
    public void modifyUnitDefenseAgainstUnit(BattleParams params)
    {
      params.defenderHP = params.defender.model.maxHP;
      params.defensePower += buff;
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
    private static final int IRONWILL_BOOST = 30;
    private static final int IRONWILL_WOUND = -2;
    private final ArrayList<Unit> boostedUnits = new ArrayList<Unit>();

    IronWill()
    {
      super(NAME, COST);
    }

    @Override
    protected void enqueueCOMods(Commander co, MapMaster gameMap, ArrayList<COModifier> modList)
    {
      DynamicModifier dym = new DynamicModifier(new IronWillMod(IRONWILL_BOOST));
      for( Unit unit : co.units )
      {
        if( !unit.isTurnOver )
          dym.addApplicable(unit);
      }
      modList.add(dym);
    }

    @Override
    protected void perform(Commander co, MapMaster gameMap)
    {
      for( Unit unit : co.units )
      {
        if( !unit.isTurnOver )
          boostedUnits.add(unit);
      }
    }
    @Override
    protected void revert(Commander co, MapMaster gameMap)
    {
      for( Unit unit : boostedUnits )
      {
        unit.alterHP(IRONWILL_WOUND);
      }
      boostedUnits.clear();
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
    COModifier damageMod = null;
    COModifier defenseMod = null;

    Retribution()
    {
      super(NAME, COST);
      damageMod = new CODamageModifier(RETRIBUTION_BUFF);
      defenseMod = new CODefenseModifier(-RETRIBUTION_NERF);
    }

    @Override
    protected void enqueueCOMods(Commander co, MapMaster gameMap, ArrayList<COModifier> modList)
    {
      modList.add(damageMod);
      modList.add(defenseMod);
      modList.add(new DynamicModifier(new PreEmptiveCounterMod(co)));
    }

    @Override
    protected void perform(Commander co, MapMaster gameMap)
    {}
  }
  
}
