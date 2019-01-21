package CommandingOfficers;

import java.util.ArrayList;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.Combat.BattleSummary;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.Combat.BattleInstance.CombatContext;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.Weapons.Weapon;

/*
 * Venge enhances counter-attacks of all sorts.
 */
public class CommanderVenge extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Venge", new instantiator());
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new CommanderVenge();
    }
  }

  /** A list of all the units that have attacked me since my last turn. */
  private ArrayList<Unit> aggressors = new ArrayList<Unit>();
  /** How much power I get when beating them up */
  public final int VENGEANCE_BOOST = 50;
  public boolean counterAtFullPower = false;
  public boolean counterFirst = false;

  public CommanderVenge()
  {
    super(coInfo);

    addCommanderAbility(new IronWill(this));
    addCommanderAbility(new Retribution(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public void initTurn(GameMap map)
  {
    super.initTurn(map);
    counterAtFullPower = false;
    counterFirst = false;
  }
  @Override
  public void endTurn()
  {
    super.endTurn();
    aggressors.clear();
  }

  @Override
  public void changeCombatContext(CombatContext instance)
  {
    // If we're swapping, and we can counter, and we're on the defensive, do the swap.
    if (counterFirst && instance.canCounter && this == instance.defender.CO )
    {
      // Store our unit. Since defenders don't move, we have defenderX/Y already.
      Unit minion = instance.defender;
      Weapon myWeapon = instance.defenderWeapon;
      
      instance.defender = instance.attacker;
      instance.defenderWeapon = instance.attackerWeapon;
      instance.defenderX = instance.attackerX;
      instance.defenderY = instance.attackerY;
      
      instance.attacker = minion;
      instance.attackerWeapon = myWeapon;
      instance.attackerX = minion.x;
      instance.attackerY = minion.y;
    }
  }

  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    if( amITheAttacker )
    {
      if( counterAtFullPower && params.isCounter )
      {
        // counterattack as if the unit had not taken damage.
        params.attackerHP = params.attacker.getHP();
      }

      if( aggressors.contains(params.defender) )
      {
        // Boost attack if it's time to avenge slights
        params.attackFactor += VENGEANCE_BOOST;
      }
    }
  }

  @Override
  public void receiveBattleEvent(BattleSummary battleInfo)
  {
    super.receiveBattleEvent(battleInfo);
    // Determine if we were attacked. If so, record this misdeed.
    if( this == battleInfo.defender.CO )
    {
      aggressors.add(battleInfo.attacker);
    }
  }

  /**
   * Iron Will buffs defense and grants any unit that survives being attacked full counter-damage.
   */
  private static class IronWill extends CommanderAbility
  {
    private static final String NAME = "Iron Will";
    private static final int COST = 3;
    private static final int IRONWILL_BUFF = 30; // Get a nice defense boost, since we can't counter-attack if we're dead.
    COModifier defenseMod = null;
    CommanderVenge COcast;

    IronWill(CommanderVenge commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
      defenseMod = new CODefenseModifier(IRONWILL_BUFF);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.counterAtFullPower = true;
      COcast.addCOModifier(defenseMod);
    }
  }

  /**
   * Retribution trades defense for firepower, and allows Venge to counter-attack first.
   */
  private static class Retribution extends CommanderAbility
  {
    private static final String NAME = "Retribution";
    private static final int COST = 6;
    private static final int RETRIBUTION_BUFF = 40; // Trade defense for offense, since we hit before our attacker does.
    private static final int RETRIBUTION_NERF = 20;
    COModifier damageMod = null;
    COModifier defenseMod = null;
    CommanderVenge COcast;

    Retribution(CommanderVenge commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
      damageMod = new CODamageModifier(RETRIBUTION_BUFF);
      defenseMod = new CODefenseModifier(-RETRIBUTION_NERF);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.counterFirst = true;
      COcast.addCOModifier(damageMod);
      COcast.addCOModifier(defenseMod);
    }
  }
  
}
