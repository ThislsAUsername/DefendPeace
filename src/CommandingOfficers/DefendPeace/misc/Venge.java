package CommandingOfficers.DefendPeace.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Engine.Army;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatContext;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitFightStatModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import UI.UIUtils;
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
      super("Venge", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.MISC);
      infoPages.add(new InfoPage(
          "Commander Venge likes to get vengeance for any slight.\n" +
          "Attacking Venge is not always difficult, but you may not like the consequences.\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" +
          "-10 attack.\n" +
          "Units that attack you or capture your property are marked for vengeance.\n" +
          "Gain +"+VENGEANCE_BOOST+"% attack against marked units.\n"));
      infoPages.add(new InfoPage(new Prevenge(null, null),
          "Your units take their own revenge.\n" +
          "Gives "+Prevenge.ATTACK+"/"+Prevenge.DEFENSE+" stats\n" +
          "Your units counterattack before they are hit.\n"));
      infoPages.add(new InfoPage(new Collectivenge(null, null),
          "Collective punishment is the most effective punishment.\n" +
          "Deal "+Collectivenge.HEALTH_DAMAGE+"% damage to enemies within radius "+Collectivenge.RADIUS+" of any marked unit.\n" +
          "+10/10 stats\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Venge(rules);
    }
  }
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  /** Units I can take vengeance on. */
  private ArrayList<Unit> aggressors = new ArrayList<Unit>();
  /** How much power I get when beating them up */
  public final static int VENGEANCE_BOOST = 60;

  public Venge(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new Prevenge(this, cb));
    addCommanderAbility(new Collectivenge(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower -= 10;
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
  public GameEventQueue receiveTurnEndEvent(Army army, int turn)
  {
    if( army == this.army )
      aggressors.clear();
    return null;
  }

  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    // If we can get a vengeance boost against this unit, let our player know.
    if( aggressors.contains(unit) )
      return 'V';

    return super.getUnitMarking(unit, activeArmy);
  }

  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    if (aggressors.contains(join.unitDonor))
      aggressors.add(join.unitRecipient);
    return null;
  }
  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary battleInfo)
  {
    // Determine if we were attacked. If so, record this misdeed.
    if( this == battleInfo.defender.CO )
      aggressors.add(battleInfo.attacker.unit);
    return super.receiveBattleEvent(battleInfo);
  }
  @Override
  public GameEventQueue receiveCaptureEvent(Unit unit, Commander prevOwner, MapLocation location)
  {
    if( this == prevOwner )
      aggressors.add(unit);
    return null;
  }

  public static class PreEmptiveCounterMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void changeCombatContext(CombatContext instance, UnitContext buffOwner)
    {
      // If we're swapping, and we can counter, and we're on the defensive, do the swap.
      if( instance.canCounter && instance.defender == buffOwner )
      {
        instance.swapCombatants();
      }
    }
  }

  private static class Prevenge extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Prevenge";
    private static final int COST = 3;
    private static final int ATTACK = 40; // Trade defense for offense, since we hit before our attacker does.
    private static final int DEFENSE = -20;
    UnitModifier damageMod = null;
    UnitModifier defenseMod = null;

    Prevenge(Venge venge, CostBasis basis)
    {
      super(venge, NAME, COST, basis);
      damageMod  = new UnitDamageModifier(ATTACK);
      defenseMod = new UnitDefenseModifier(DEFENSE);
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(damageMod);
      modList.add(defenseMod);
      modList.add(new PreEmptiveCounterMod());
    }
  }

  private static class Collectivenge extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Collectivenge";
    UnitModifier statMod = new UnitFightStatModifier(10);
    private static final int COST = 5;
    private static final int HEALTH_DAMAGE = 60;
    private static final int RADIUS = 2;
    Venge coCast;

    Collectivenge(Venge venge, CostBasis cb)
    {
      super(venge, NAME, COST, cb);
      coCast = venge;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue events = new GameEventQueue();
      events.add(new MassDamageEvent(myCommander, findVictims(gameMap), HEALTH_DAMAGE, false));
      return events;
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      for( Unit victim : findVictims(gameMap) )
      {
        XYCoord coord = new XYCoord(victim);
        output.add(new DamagePopup(
                       coord,
                       myCommander.myColor,
                       Math.min(victim.getHealth()-1, HEALTH_DAMAGE) + "%"));
      }

      return output;
    }

    public HashSet<Unit> findVictims(GameMap gameMap)
    {
      Set<XYCoord> tilesInRange = Utils.findLocationsNearUnits(gameMap, coCast.aggressors, RADIUS);
      HashSet<Unit> victims = new HashSet<Unit>(); // Find all of our unlucky participants
      for( XYCoord coord : tilesInRange )
      {
        Unit victim = gameMap.getResident(coord);
        if( null != victim && myCommander.isEnemy(victim.CO) )
          victims.add(victim);
      }
      return victims;
    }
  }
  
}
