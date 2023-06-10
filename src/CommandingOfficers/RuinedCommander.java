package CommandingOfficers;

import Engine.Army;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GameInstance;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;

import java.awt.Color;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.CommanderAbilityEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.StateTrackers.KillCountsTracker;
import Engine.StateTrackers.StateTracker;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.GameOverlay;
import Units.Unit;
import Units.UnitContext;
import Units.UnitDelta;

public abstract class RuinedCommander extends DeployableCommander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage DOR_MECHANICS_BLURB = new InfoPage(
            "Days of Ruin mechanics:\n"
          + "You have a CO zone that covers the area around your COU, and grants bonuses to your units inside.\n"
          + "The zone expands 1 tile when your meter is half full, and again at full charge.\n"
          + "All units get +10 attack and +10 Damage Division (DD) while in the zone.\n"
          + "Your CO may also have another zone boost that stacks with that boost, but may only apply to some unit types.\n"
          + "The zone becomes global while your power is active, but activating your power requires your COU's action.\n"
          + "Zone boosts apply to other COs in a tag, even when tagged out.\n"
          + "CO Energy gained reflects HP damage you deal, and not on the type of unit you hit.\n"
          + "You can only gain CO energy for combat where the initiator is inside the zone.\n"
          + "  (This means an enemy in your zone attacking outside your zone can give you charge from your counterattack)\n"
          + "However, your CO ability does not increase in cost as you use it.\n"
          + "Veterancy:\n"
          + "On making a kill, your unit will level up.\n"
          + "There are 3 veterancy levels; their stat boosts are +5/0, +10/0, and +20/20 attack/DD.\n"
          + "Your COU is always max level.\n"
          + "Veterancy bonuses do not apply if this CO is tagged out, but kills are always tracked.\n"
          );
  public static final int CHARGERATIO_HP = 900; // Funds value of 1 HP damage dealt, for the purpose of power charge

  public static final int GENERIC_STAT_BUFF = 10;
  /** If the unit type matches any flag in this mask, it receives my zone stat boosts */
  public long canBoostMask = Long.MAX_VALUE;
  public int zonePow;
  public int zoneDef;
  public final int zoneBaseRadius;
  public int zoneRadius;
  public boolean zoneIsGlobal = false;
  ZoneBoostMod myZoneBoost;

  @Override
  public int getCOUCount() {return 1;}

  public RuinedCommander(int radius, int atk, int def, CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    zonePow = atk;
    zoneDef = def;
    zoneBaseRadius = radius;
    zoneRadius = radius;
    deployCostPercent = 50;
    luck = 11;
  }

  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    killCounts = StateTracker.instance(game, KillCountsTracker.class);
    myZoneBoost = new ZoneBoostMod(this);
    this.army.addUnitModifier(myZoneBoost);
  }
  @Override
  public void deInitForGame(GameInstance game)
  {
    super.deInitForGame(game);
    this.army.removeUnitModifier(myZoneBoost);
  }

  // Tell MapController and friends that we never have abilities ready, since those go through the COU
  @Override
  public ArrayList<CommanderAbility> getReadyAbilities()
  {
    return new ArrayList<CommanderAbility>();
  }
  @Override
  public void onCOULost(Unit minion)
  {
    modifyAbilityStars(-42);
    zoneIsGlobal = false;
  }
  // Hook: Update current zone radius every time energy changes
  @Override
  public void modifyAbilityPower(int amount)
  {
    super.modifyAbilityPower(amount);
    zoneRadius = zoneBaseRadius;

    double maxCost = getMaxAbilityPower();
    if( maxCost > 0 )
    {
      double abilityPower = getAbilityPower();

      // Expansions at 100% and 50%
      if( abilityPower >= maxCost / 2 )
        ++zoneRadius;
      if( abilityPower >= maxCost )
        ++zoneRadius;
    }
  }
  @Override
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    if( zoneIsGlobal )
      return 0; // No charging while the zone is global, that's a little OP
    if( minion == null || enemy == null )
      return 0;

    // isCounter tells us who the attacker is, so we can figure out which one we care about being in the zone
    UnitContext chargeSource = minion.after;
    if( isCounter )
      chargeSource = enemy.after;
    if( !isInZone(chargeSource) )
      return 0;

    double myHPDealt = enemy.getHPDamage();

    int power = 0; // value in funds of the charge we're getting

    power += myHPDealt * CHARGERATIO_HP;

    return power;
  }
  @Override
  public int calculateMassDamageCharge(Unit minion, int lostHP)
  {
    return 0;
  }

  // Feed those abilities instead into my COU's action list, if available
  @Override
  public void modifyActionList(UnitContext uc)
  {
    if( COUs.contains(uc.unit) )
    {
      ArrayList<CommanderAbility> abilities = super.getReadyAbilities();
      for( CommanderAbility toCast : abilities )
        uc.actionTypes.add(new UseAbilityFactory(toCast));
    }
    super.modifyActionList(uc);
  }

  public boolean isInZone(UnitContext uc)
  {
    if( zoneIsGlobal )
      return true;

    if( COUs.contains(uc.unit) )
      return true;

    if( null == uc.coord )
      return false;

    for( Unit cou : COUs )
    {
      int distance = uc.coord.getDistance(cou);
      if( distance <= zoneRadius )
        return true;
    }

    return false;
  }
  public ArrayList<GameOverlay> getMyOverlays(GameMap gameMap, boolean amIViewing)
  {
    Color fill = new Color(0, 0, 0, 100);

    ArrayList<GameOverlay> overlays = super.getMyOverlays(gameMap, amIViewing);
    if( zoneIsGlobal )
      return overlays;

    for( Unit cou : COUs )
    {
      if( COUsLost.contains(cou) )
        continue;

      final XYCoord coCoord = new XYCoord(cou);
      GameOverlay coZone = new GameOverlay(
          coCoord, Utils.findLocationsInRange(gameMap, coCoord, 0, zoneRadius),
          fill, myColor);
      overlays.add(coZone);
    }
    return overlays;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    VeteranRank rank = getRank(params.attacker.unit);
    params.attackPower += rank.attack;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    VeteranRank rank = getRank(params.defender.unit);
    params.defenseDivision += rank.defense;
    params.terrainGivesSubtraction = false;
  }

  public static class ZoneBoostMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;
    public final RuinedCommander zoneSource;

    public ZoneBoostMod(RuinedCommander zoneSource)
    {
      this.zoneSource = zoneSource;
    }

    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      if( zoneSource.isInZone(params.attacker) )
      {
        params.attackPower += GENERIC_STAT_BUFF;
        if( params.attacker.model.isAny(zoneSource.canBoostMask) )
          params.attackPower += zoneSource.zonePow;
      }
    }
    @Override
    public void modifyUnitDefenseAgainstUnit(BattleParams params)
    {
      if( zoneSource.isInZone(params.defender) )
      {
        params.defenseDivision += GENERIC_STAT_BUFF;
        if( params.defender.model.isAny(zoneSource.canBoostMask) )
          params.defenseDivision += zoneSource.zoneDef;
      }
    }
  }

  KillCountsTracker killCounts;
  public static enum VeteranRank
  {
    NONE('\0', 0, 0), LEVEL1('1', 5, 0), LEVEL2('2', 10, 0), LEVEL3('V', 20, 20);
    public final char mark;
    public final int attack, defense;
    private VeteranRank(char mark, int attack, int defense)
    {
      this.mark    = mark;
      this.attack  = attack;
      this.defense = defense;
    }
  }
  public VeteranRank getRank(Unit unit)
  {
    if( COUs.contains(unit) )
      return VeteranRank.LEVEL3;

    VeteranRank rank = VeteranRank.NONE;
    int level = killCounts.getCountFor(unit);
    if( level > 2 )
      rank = VeteranRank.LEVEL3;
    else if( level > 1 )
      rank = VeteranRank.LEVEL2;
    else if( level > 0 )
      rank = VeteranRank.LEVEL1;

    return rank;
  }
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    char mark = super.getUnitMarking(unit, activeArmy);
    // Prefer non-veterancy marks, like "COU"
    if( '\0' != mark )
      return mark;
    if( this != unit.CO )
      return mark;

    return getRank(unit).mark;
  }

  protected static class RuinedAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    public static final int COST = 6;
    RuinedCommander COcast;

    protected RuinedAbility(RuinedCommander commander, String name)
    {
      super(commander, name, COST);
      COcast = commander;
    }

    @Override
    protected void adjustCost() {}
    
    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.zoneIsGlobal = true;
    }

    @Override
    protected void revert(MapMaster gameMap)
    {
      COcast.zoneIsGlobal = false;
    }
  }

  //////////////////////////////////////////////////////////
  // Action definition happens after this point
  //////////////////////////////////////////////////////////

  public static class UseAbilityFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    final CommanderAbility toCast;
    public UseAbilityFactory(CommanderAbility ability)
    {
      toCast = ability;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new UseAbilityAction(this, actor, movePath, toCast), false);
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return toCast.toString();
    }
  } // ~UseAbilityFactory

  public static class UseAbilityAction extends GameAction
  {
    final UseAbilityFactory type;
    final Unit actor;
    private GamePath movePath;
    private CommanderAbility abilityToUse;
    final XYCoord destination;

    public UseAbilityAction(UseAbilityFactory type, Unit unit, GamePath path, CommanderAbility ability)
    {
      this.type = type;
      actor = unit;
      movePath = path;
      destination = new XYCoord(unit.x, unit.y);
      if( null == ability )
        throw new InvalidParameterException("Non-null ability required");
      abilityToUse = ability;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      // USE Ability actions consist of
      //   MOVE
      //   ABILITY
      GameEventQueue abilityEvents = new GameEventQueue();

      boolean isValid = null != abilityToUse;

      isValid &= null != actor && !actor.isTurnOver;
      isValid &= null != map;
      isValid &= (null != movePath) && (movePath.getPathLength() > 0);

      if( !isValid ) return abilityEvents;

      // Move to the target location.
      if( Utils.enqueueMoveEvent(map, actor, movePath, abilityEvents) )
      {
        // Should mirror AbilityAction.getEvents()
        abilityEvents.add(new CommanderAbilityEvent(abilityToUse));
        abilityEvents.addAll(abilityToUse.getEvents(map));
      }

      return abilityEvents;
    }

    @Override
    public String toString()
    {
      return String.format("[Use Ability %s at %s with %s]", abilityToUse, destination, actor.toStringWithLocation());
    }

    @Override
    public Unit getActor()
    {
      return actor;
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return destination;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return destination;
    }
  } // ~UseAbilityAction

  // No event is needed
}
