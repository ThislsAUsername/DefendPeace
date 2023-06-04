package CommandingOfficers;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.CommanderAbilityEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;

public abstract class RuinedCommander extends DeployableCommander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage ZONE_MECHANICS_BLURB = new InfoPage(
            "Days of Ruin mechanics:\n"
          + "You have a CO zone that covers the area around your COU, and grants bonuses to your units inside.\n"
          + "The zone expands 1 tile when your meter is half full, and again at full charge.\n"
          + "All units get +10 attack and +10 Damage Division (DD) while in the zone.\n"
          + "Your CO may also have another zone boost that stacks with that boost, but may only apply to some unit types.\n"
          + "The zone becomes global while your power is active, but activating your power requires your COU's action.\n"
          + "CO Energy gained reflects HP damage you deal, and not on the type of unit you hit.\n"
          + "You can only gain CO energy for combat where the initiator is inside the zone.\n"
          + "  (This means an enemy in your zone attacking outside your zone can give you charge from your counterattack)\n"
          + "However, your CO ability does not increase in cost as you use it.\n"
          + "Veterancy:\n"
          + "On making a kill, your unit will level up.\n"
          + "There are 3 veterancy levels; their stat boosts are +5/0, +10/0, and +20/20 attack/DD.\n"
          + "Your COU is always max level.\n"
          + "Veterancy bonuses do not apply if this CO is tagged out\n"
          );

  public final int zonePow;
  public final int zoneDef;
  public final int zoneBaseRadius;
  public int zoneRadius;
  public boolean zoneIsGlobal = false;

  @Override
  public int getCOUCount() {return 1;}

  public RuinedCommander(int atk, int def, int radius, CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    zonePow = atk;
    zoneDef = def;
    zoneBaseRadius = radius;
    zoneRadius = radius;
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
    modifyAbilityPower(-42);
    zoneIsGlobal = false;
  }
  // Hook: Update current zone radius every time energy changes
  @Override
  public void modifyAbilityPower(double amount)
  {
    super.modifyAbilityPower(amount);
    zoneRadius = zoneBaseRadius;

    double[] abilityCosts = getAbilityCosts();
    if( abilityCosts.length > 0 )
    {
      double maxCost = 0;
      double abilityPower = getAbilityPower();
      for( double cost : abilityCosts )
        maxCost = Math.max(maxCost, cost);

      // Expansions at 100% and 50%
      if( abilityPower >= maxCost / 2 )
        ++zoneRadius;
      if( abilityPower >= maxCost )
        ++zoneRadius;
    }
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

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( COUs.contains(params.attacker.unit) )
      params.attackPower += zonePow;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    // TODO: DD, zone, vets
    if( COUs.contains(params.defender.unit) )
      params.defensePower += zoneDef;
  }

  protected static class RuinedAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    RuinedCommander COcast;

    protected RuinedAbility(RuinedCommander commander, String name, int cost)
    {
      super(commander, name, cost);
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
      isValid &= abilityToUse.myCommander.getReadyAbilities().contains(abilityToUse);

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
