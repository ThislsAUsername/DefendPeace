package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatContext;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
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
          "- After being attacked, Venge gets a bonus of "+VENGEANCE_BOOST+"% attack against the unit that picked the fight.\n" +
          "- Units that Venge can get vengeance on are marked with a V.\n"));
      infoPages.add(new InfoPage(
          IronWill.NAME+" ("+IronWill.COST+"):\n" +
          "Gives a defense boost of "+IronWill.IRONWILL_BUFF+"%\n" +
          "Units now deal counterattacks as if they had not taken damage from the hit.\n"));
      infoPages.add(new InfoPage(
          Retribution.NAME+" ("+Retribution.COST+"):\n" +
          "Gives an attack boost of "+Retribution.RETRIBUTION_BUFF+"%\n" +
          "Gives a defense penalty of "+Retribution.RETRIBUTION_NERF+"%\n" +
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
  /** How much power I get when beating them up */
  public final static int VENGEANCE_BOOST = 50;
  public boolean counterAtFullPower = false;
  public boolean counterFirst = false;

  public Venge(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new IronWill(this));
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
    counterAtFullPower = false;
    counterFirst = false;
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
    
    return super.getUnitMarking(unit);
  }

  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    if (aggressors.contains(join.unitDonor))
      aggressors.add(join.unitRecipient);
    return null;
  }

  @Override
  public void changeCombatContext(CombatContext instance)
  {
    // If we're swapping, and we can counter, and we're on the defensive, do the swap.
    if (counterFirst && instance.canCounter && this == instance.defender.CO )
    {
      UnitContext minion = instance.defender;

      instance.defender = instance.attacker;
      instance.attacker = minion;
    }
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
      if( counterAtFullPower && params.isCounter )
      {
        // counterattack as if the unit had not taken damage.
        params.attackerHP = params.attacker.unit.getHP();
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
      aggressors.add(battleInfo.attacker);
    }
    return null;
  }

  /**
   * Iron Will buffs defense and grants any unit that survives being attacked full counter-damage.
   */
  private static class IronWill extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Iron Will";
    private static final int COST = 3;
    private static final int IRONWILL_BUFF = 30; // Get a nice defense boost, since we can't counter-attack if we're dead.
    COModifier defenseMod = null;
    Venge COcast;

    IronWill(Venge commander)
    {
      super(NAME, COST);
      COcast = commander;
      defenseMod = new CODefenseModifier(IRONWILL_BUFF);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void enqueueCOMods(Commander co, MapMaster gameMap, ArrayList<COModifier> modList)
    {
      modList.add(defenseMod);
    }

    @Override
    protected void perform(Commander co, MapMaster gameMap)
    {
      // TODO: UnitModifier
      COcast.counterAtFullPower = true;
    }
  }

  /**
   * Retribution trades defense for firepower, and allows Venge to counter-attack first.
   */
  private static class Retribution extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Retribution";
    private static final int COST = 6;
    private static final int RETRIBUTION_BUFF = 40; // Trade defense for offense, since we hit before our attacker does.
    private static final int RETRIBUTION_NERF = 20;
    COModifier damageMod = null;
    COModifier defenseMod = null;
    Venge COcast;

    Retribution(Venge commander)
    {
      super(NAME, COST);
      COcast = commander;
      damageMod = new CODamageModifier(RETRIBUTION_BUFF);
      defenseMod = new CODefenseModifier(-RETRIBUTION_NERF);
    }

    @Override
    protected void enqueueCOMods(Commander co, MapMaster gameMap, ArrayList<COModifier> modList)
    {
      modList.add(damageMod);
      modList.add(defenseMod);
    }

    @Override
    protected void perform(Commander co, MapMaster gameMap)
    {
      // TODO: UnitModifier
      COcast.counterFirst = true;
    }
  }
  
}
