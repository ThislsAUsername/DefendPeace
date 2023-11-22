package Units;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.CreateUnitEvent.AnimationStyle;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.StateTrackers.StateTracker;
import Engine.UnitActionLifecycles.TransformLifecycle.TransformEvent;
import Engine.UnitActionLifecycles.UnloadLifecycle.UnloadEvent;
import Engine.UnitActionLifecycles.JoinLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Engine.UnitActionLifecycles.LoadLifecycle.LoadEvent;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.GBAFEUnits.GBAFEUnitModel;

public class GBAFEActions
{
  public static final boolean VARIABLE_PROMO_COST = false;
  public static final int PROMOTION_COST = 5000;
  public static final boolean PROMOTION_REQUIRES_LEVELS = true;

  public static class GBAFEExperienceTracker extends StateTracker
  {
    private static final long serialVersionUID = 1L;
    private static final int PROMO_LEVEL_BONUS = 20; // 20 levels before promotion. A real shocker, I know.
    // ref https://fireemblemwiki.org/wiki/Class_relative_power
    private static final int BASE_DAMAGE_XP    = 31;
    private static final int BASE_KILL_XP      = 20;
    private static final int THIEF_KILL_XP     = 20; // Don't ask me why
    private static final int PROMO_KILL_XP     = 60; // Except for some classes... again, don't ask me why
    private static final int EXP_MULTIPLIER    =  3; // To make it actually feasible to level units

    public HashMap<Unit, Integer> experience = new HashMap<>();

    public GameEventQueue receiveUnitJoinEvent(JoinLifecycle.JoinEvent event)
    {
      int donorXP = 0;
      if( experience.containsKey(event.unitDonor) )
        donorXP = experience.remove(event.unitDonor);
      if( donorXP > getExperience(event.unitRecipient) )
        experience.put(event.unitRecipient, donorXP);
      return null;
    };
    @Override
    public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
    {
      experience.remove(victim);
      return null;
    }

    public GameEventQueue receiveCaptureEvent(Unit unit, MapLocation location)
    {
      addExperience(unit, 10);
      return null;
    };
    @Override
    public GameEventQueue receiveBattleEvent(BattleSummary battleInfo)
    {
      experiencize(battleInfo.attacker, battleInfo.defender);
      experiencize(battleInfo.defender, battleInfo.attacker);
      return null;
    }
    private void experiencize(UnitDelta attacker, UnitDelta defender)
    {
      if( defender.deltaPreciseHP > -0.1 )
      {
        addExperience(attacker.unit, 1);
        return; // No damage? Count it as a whiff
      }
      GBAFEUnitModel attackerType = (GBAFEUnitModel) attacker.model;
      GBAFEUnitModel defenderType = (GBAFEUnitModel) defender.model;
      experiencize(attacker.unit, defender.after.getHP() == 0,
                   attackerType, defenderType);
    }
    private void experiencize(Unit profiteer, boolean isLethal, GBAFEUnitModel attacker, GBAFEUnitModel defender)
    {
      int profit = calcDamageXP(attacker, defender);
      if(isLethal)
        profit += calcKillXP(attacker, defender);
      addExperience(profiteer, attacker.baseXP, profit);
    }
    private static int calcDamageXP(GBAFEUnitModel unit, GBAFEUnitModel target)
    {
      int unitLevel = unit.stats.level;
      if( unit.stats.promoted ) unitLevel += PROMO_LEVEL_BONUS;
      int crp = unit.classRelativePower;
      int targetLevel = target.stats.level;
      if( target.stats.promoted ) targetLevel += PROMO_LEVEL_BONUS;

      int profit = (BASE_DAMAGE_XP + targetLevel - unitLevel) / crp;
      return Math.max(0, profit);
    }
    private static int calcKillXP(GBAFEUnitModel unit, GBAFEUnitModel target)
    {
      int unitLevel = unit.stats.level;
      int levelXP = -1 * unitLevel * unit.classRelativePower;
      int targetLevel = target.stats.level;
      levelXP += targetLevel * target.classRelativePower;

      int bonusKillXP = 0;
      if( target.visionRange == GBAFEUnits.VISION_THIEF )
        bonusKillXP += THIEF_KILL_XP;
      bonusKillXP -= promoKillBonus(unit);
      bonusKillXP += promoKillBonus(target);

      int profit = BASE_KILL_XP + bonusKillXP + levelXP;
      return Math.max(0, profit);
    }
    private static int promoKillBonus(GBAFEUnitModel target)
    {
      int bonusKillXP = 0;
      if( target.stats.promoted )
      {
        bonusKillXP += PROMO_KILL_XP;
        if( target.reducedPromoKillBonus )
          bonusKillXP -= THIEF_KILL_XP; // I don't know whyyyyyyy
      }
      return bonusKillXP;
    }

    public int getExperience(Unit profiteer)
    {
      if( experience.containsKey(profiteer) )
        return experience.get(profiteer);
      GBAFEUnitModel profiteerType = (GBAFEUnitModel) profiteer.model;
      return addExperience(profiteer, profiteerType.baseXP, 0);
    }
    public int addExperience(Unit profiteer, int profit)
    {
      GBAFEUnitModel profiteerType = (GBAFEUnitModel) profiteer.model;
      return addExperience(profiteer, profiteerType.baseXP, profit);
    }
    private int addExperience(Unit profiteer, int base, int profit)
    {
      if( !experience.containsKey(profiteer) )
        experience.put(profiteer, base);
      int xp = experience.get(profiteer);
      int finalVal = xp + profit*EXP_MULTIPLIER;
      if( finalVal > PROMO_LEVEL_BONUS*100 )
        finalVal = PROMO_LEVEL_BONUS*100;
      experience.put(profiteer, finalVal);
      return finalVal;
    }
    @Override
    public CustomStatData getCustomStat(Unit unit)
    {
      Color tc = Color.white;
      int level = getExperience(unit) / 100;
      String text = "" + level;
      if( level >= 10 && null != ((GBAFEUnitModel) unit.model).promotesTo )
        tc = Color.green; // If we have a promotion available, do fancy text
      return new CustomStatData('L', Color.white, tc, text);
    }
  }

  public static class PromotionFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final UnitModel destType;

    public PromotionFactory(UnitModel type)
    {
      destType = type;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        boolean validPromo = true;
        validPromo &= actor.CO.army.money >= getPromoCost(actor);
        MapLocation destInfo = map.getLocation(moveLocation);
        validPromo &= !actor.CO.isEnemy(destInfo.getOwner());
        if( validPromo && PROMOTION_REQUIRES_LEVELS )
        {
          GBAFEExperienceTracker xp = StateTracker.instance(map.game, GBAFEExperienceTracker.class);
          validPromo &= xp.getExperience(actor) > 999;
        }
        if( validPromo )
          return new GameActionSet(new PromotionAction(actor, movePath, this), false);
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "~" + destType.name + " ("+getPromoCost(actor)+")";
    }
    public int getPromoCost(Unit actor)
    {
      if( VARIABLE_PROMO_COST )
        return destType.costBase - actor.model.costBase;
      return PROMOTION_COST;
    }
  }

  /** Effectively a WAIT that costs money, and the unit ends up as a different unit at the end of it. */
  public static class PromotionAction extends WaitLifecycle.WaitAction
  {
    private PromotionFactory type;
    Unit actor;

    public PromotionAction(Unit unit, GamePath path, PromotionFactory pType)
    {
      super(unit, path);
      type = pType;
      actor = unit;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue transformEvents = super.getEvents(gameMap);

      if( transformEvents.size() > 0 ) // if we successfully made a move action
      {
        GameEvent moveEvent = transformEvents.peek();
        if( moveEvent.getEndPoint().equals(getMoveLocation()) ) // make sure we shouldn't be pre-empted
        {
          transformEvents.add(new ModifyFundsEvent(actor.CO.army, -1 * type.getPromoCost(actor)));
          transformEvents.add(new TransformEvent(actor, type.destType));
          transformEvents.add(new HealUnitEvent(actor, 10, null)); // "Free" fullheal included, for tactical spice
          transformEvents.add(new ResupplyEvent(null, actor));     //   and also resupply, since we use this for ballistae
        }
      }
      return transformEvents;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and promote to %s]", actor.toStringWithLocation(), getMoveLocation(),
          type.destType);
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~PromotionAction

  public static abstract class SupportActionFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final String name;
    public final int rangeMin, rangeMax;

    public SupportActionFactory(String name)
    {
      this(name, 1, 1);
    }
    public SupportActionFactory(String name, int rangeMin, int rangeMax)
    {
      this.name     = name;
      this.rangeMin = rangeMin;
      this.rangeMax = rangeMax;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        ArrayList<GameAction> options = new ArrayList<GameAction>();
        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, rangeMin, rangeMax);

        // For each location, see if there is a friendly unit to repair.
        for( XYCoord loc : locations )
        {
          // If there's a friendly unit there who isn't us, we can repair them.
          Unit other = map.getLocation(loc).getResident();
          if( other != null
              && !actor.CO.isEnemy(other.CO) && other != actor
              && canSupport(map, actor, movePath, other)
              )
          {
            options.addAll(getSupportAll(map, actor, movePath, other));
          }
        }

        // Only add this action set if we actually have a target
        if( !options.isEmpty() )
        {
          // Bundle our attack options into an action set
          return new GameActionSet(options);
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return name;
    }

    public abstract boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other);
    // Implementers must override one of these
    public ArrayList<GameAction> getSupportAll(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      ArrayList<GameAction> result = new ArrayList<>();
      result.add(getSupportSingle(map, actor, movePath, other));
      return result;
    }
    public GameAction getSupportSingle(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return null;
    }
  }

  /**
   * Repair, but:<p>
   * It's free<p>
   * It has variable heal quantity<p>
   * It has variable range<p>
   * It doesn't take target HP into account because it'd be very annoying to play with<p>
   * It doesn't work on boats (I have stupid plans for boats)<p>
   */
  public static class HealStaffFactory extends SupportActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final int quantity;

    public HealStaffFactory(String name, int healHP, int range)
    {
      super(name, 1, range);
      quantity = healHP;
    }
    @Override
    public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return !other.model.isAny(UnitModel.SHIP) && (!other.isFullySupplied() || other.isHurt());
    }
    @Override
    public GameAction getSupportSingle(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return new HealStaffAction(this, actor, movePath, other);
    }
  }

  public static class HealStaffAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    private XYCoord repairCoord;
    Unit benefactor;
    Unit beneficiary;
    HealStaffFactory type;

    public HealStaffAction(HealStaffFactory type, Unit actor, GamePath path, Unit target)
    {
      this.type = type;
      benefactor = actor;
      beneficiary = target;
      movePath = path;
      if( benefactor != null && null != beneficiary )
      {
        startCoord = new XYCoord(actor.x, actor.y);
        repairCoord = new XYCoord(target.x, target.y);
      }
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = movePath.getEndCoord();
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // Repair actions consist of
      //   MOVE
      //   HEAL
      GameEventQueue healEvents = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != repairCoord) && gameMap.isLocationValid(startCoord)
          && gameMap.isLocationValid(repairCoord) )
      {
        isValid &= benefactor != null && !benefactor.isTurnOver;
        isValid &= isValid && null != beneficiary && !benefactor.CO.isEnemy(beneficiary.CO);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, benefactor, movePath, healEvents) )
        {
          // No surprises in the fog.
          healEvents.add(new HealUnitEvent(beneficiary, type.quantity, null));
          healEvents.add(new AddExperienceEvent(benefactor, 11));
        }
      }
      return healEvents;
    }

    @Override public Unit getActor() { return benefactor; }
    @Override public XYCoord getMoveLocation() { return moveCoord; }
    @Override public XYCoord getTargetLocation() { return repairCoord; }
    @Override public UnitActionFactory getType() { return type; }
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and use %s to heal %s]", benefactor.toStringWithLocation(), moveCoord,
          type.name, beneficiary.toStringWithLocation());
    }
  } // ~HealStaffAction


  /**
   * Uses hacker wizard powers to cheat the turn system<p>
   */
  public static class ReactivateUnitFactory extends SupportActionFactory
  {
    private static final long serialVersionUID = 1L;

    public ReactivateUnitFactory(String name)
    {
      super(name);
    }
    @Override
    public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return other.isTurnOver && actor.CO.army == other.CO.army;
    }
    @Override
    public GameAction getSupportSingle(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      return new ReactivateUnitAction(this, actor, movePath, other);
    }
  }

  public static class ReactivateUnitAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    private XYCoord targetCoord;
    Unit benefactor;
    Unit beneficiary;
    ReactivateUnitFactory type;

    public ReactivateUnitAction(ReactivateUnitFactory type, Unit actor, GamePath path, Unit target)
    {
      this.type = type;
      benefactor = actor;
      beneficiary = target;
      movePath = path;
      if( benefactor != null && null != beneficiary )
      {
        startCoord = new XYCoord(actor.x, actor.y);
        targetCoord = new XYCoord(target.x, target.y);
      }
      if( null != path && (path.getEnd() != null) )
      {
        moveCoord = movePath.getEndCoord();
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // action consists of
      //   MOVE
      //   REACTIVATE
      GameEventQueue healEvents = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != targetCoord) && gameMap.isLocationValid(startCoord)
          && gameMap.isLocationValid(targetCoord) )
      {
        isValid &= benefactor != null && !benefactor.isTurnOver;
        isValid &= isValid && null != beneficiary && benefactor.CO.army == beneficiary.CO.army;
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, benefactor, movePath, healEvents) )
        {
          // No surprises in the fog.
          healEvents.add(new ReactivateUnitEvent(beneficiary));
          healEvents.add(new AddExperienceEvent(benefactor, 10));
        }
      }
      return healEvents;
    }

    @Override public Unit getActor() { return benefactor; }
    @Override public XYCoord getMoveLocation() { return moveCoord; }
    @Override public XYCoord getTargetLocation() { return targetCoord; }
    @Override public UnitActionFactory getType() { return type; }
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and use %s to reactivate %s]", benefactor.toStringWithLocation(), moveCoord,
          type.name, beneficiary.toStringWithLocation());
    }
  } // ~ReactivateUnitAction

  public static class ReactivateUnitEvent implements GameEvent
  {
    private Unit unit;

    public ReactivateUnitEvent(Unit aTarget)
    {
      unit = aTarget;
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      if( unit.isStunned )
        unit.isStunned = false;
      else
        unit.isTurnOver = false;
    }

    @Override public GameAnimation getEventAnimation(MapView mapView) { return null; }
    @Override public GameEventQueue sendToListener(GameEventListener listener) { return null; }
    @Override public XYCoord getStartPoint() { return new XYCoord(unit.x, unit.y); }
    @Override public XYCoord getEndPoint() { return new XYCoord(unit.x, unit.y); }
  }

  public static class AddExperienceEvent implements GameEvent
  {
    private Unit unit;
    private int exp;

    public AddExperienceEvent(Unit aTarget, int xp)
    {
      unit = aTarget;
      exp = xp;
    }

    @Override
    public void performEvent(MapMaster map)
    {
      GBAFEExperienceTracker xp = StateTracker.instance(map.game, GBAFEExperienceTracker.class);
      xp.addExperience(unit, exp);
    }

    @Override public GameAnimation getEventAnimation(MapView mapView) { return null; }
    @Override public GameEventQueue sendToListener(GameEventListener listener) { return null; }
    @Override public XYCoord getStartPoint() { return new XYCoord(unit.x, unit.y); }
    @Override public XYCoord getEndPoint() { return new XYCoord(unit.x, unit.y); }
  }

  public static class SummonTracker extends StateTracker
  {
    private static final long serialVersionUID = 1L;

    public HashMap<Unit, Unit> summons = new HashMap<>();

    private void removeSummon(Unit tbr)
    {
      if( !summons.containsValue(tbr) )
        return;
      for( Unit key : summons.keySet() )
      {
        if( tbr != summons.get(key) )
          continue;
        summons.remove(key);
        break;
      }
    }
    public GameEventQueue receiveUnitJoinEvent(JoinLifecycle.JoinEvent event)
    {
      removeSummon(event.unitDonor);
      return null;
    };
    @Override
    public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
    {
      removeSummon(victim);
      return null;
    }
  }

  public static class SummonPhantomFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public static final String name = "SUMMON";
    public final UnitModel summonType;

    public SummonPhantomFactory(UnitModel type)
    {
      summonType = type;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      SummonTracker st = StateTracker.instance(map.game, SummonTracker.class);
      if( st.summons.containsKey(actor) )
        return null;

      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        ArrayList<GameAction> summonOptions = new ArrayList<GameAction>();
        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, moveLocation, 1, 1);

        for( XYCoord loc : locations )
        {
          Environment env = map.getEnvironment(loc);
          if( map.isLocationEmpty(actor, loc) && actor.getMoveFunctor().canStandOn(env) )
          {
            summonOptions.add(new SummonPhantomAction(this, actor, movePath, loc));
          }
        }

        // Only add this action set if we actually have a target
        if( !summonOptions.isEmpty() )
        {
          return new GameActionSet(summonOptions);
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return name;
    }
  }

  public static class SummonPhantomAction extends GameAction
  {
    private GamePath movePath;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    Unit summoner;
    XYCoord target;
    private SummonPhantomFactory type;

    public SummonPhantomAction(SummonPhantomFactory type, Unit actor, GamePath path, XYCoord target)
    {
      this.type = type;
      summoner = actor;
      this.target = target;
      movePath = path;
      startCoord = new XYCoord(actor);
      moveCoord = movePath.getEndCoord();
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // action consists of
      //   MOVE
      //   SUMMON
      GameEventQueue healEvents = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != target) && gameMap.isLocationValid(startCoord)
          && gameMap.isLocationValid(target) )
      {
        isValid &= summoner != null && !summoner.isTurnOver;
        isValid &= gameMap.isLocationEmpty(summoner, target);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, summoner, movePath, healEvents) )
        {
          // No surprises in the fog.
          healEvents.add(new CreateUnitEvent(summoner.CO, type.summonType, target, AnimationStyle.NONE, true, false));
          healEvents.add(new AddExperienceEvent(summoner, 10));
          healEvents.add(new AddSummonEvent(summoner, target));
        }
      }
      return healEvents;
    }

    @Override public Unit getActor() { return summoner; }
    @Override public XYCoord getMoveLocation() { return moveCoord; }
    @Override public XYCoord getTargetLocation() { return target; }
    @Override public UnitActionFactory getType() { return type; }
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and summon at %s]", summoner.toStringWithLocation(), moveCoord, target);
    }
  } // ~SummonPhantomAction

  public static class AddSummonEvent implements GameEvent
  {
    private Unit unit;
    XYCoord target;

    public AddSummonEvent(Unit caster, XYCoord target)
    {
      unit = caster;
      this.target = target;
    }

    @Override
    public void performEvent(MapMaster map)
    {
      SummonTracker st = StateTracker.instance(map.game, SummonTracker.class);
      st.summons.put(unit, map.getResident(target));
    }

    @Override public GameAnimation getEventAnimation(MapView mapView) { return null; }
    @Override public GameEventQueue sendToListener(GameEventListener listener) { return null; }
    @Override public XYCoord getStartPoint() { return new XYCoord(unit.x, unit.y); }
    @Override public XYCoord getEndPoint() { return new XYCoord(unit.x, unit.y); }
  }

  // rescue/drop stuff

  /**
   * Provides an alternate method to get units loaded onto other units; includes canto
   */
  public static class RescueUnitFactory extends SupportActionFactory
  {
    private static final long serialVersionUID = 1L;
    public static final RescueUnitFactory instance = new RescueUnitFactory();
    private Object readResolve() { return instance; }
    public RescueUnitFactory()
    {
      super("RESCUE");
    }
    @Override
    public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      if( actor.heldUnits.size() >= actor.model.baseCargoCapacity )
        return false;
      // Can't rescue ships or units with cargo
      return !other.model.isAny(UnitModel.SHIP) && !(other.heldUnits.size() > 0);
    }
    @Override
    public ArrayList<GameAction> getSupportAll(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      ArrayList<GameAction> result = new ArrayList<>();

//      boolean includeOccupiedDestinations = false;
//      boolean canTravelThroughEnemies = false;
//      int movePoints = actor.getMovePower(map) - movePath.getFuelCost(actor, map);
//      ArrayList<XYCoord> destinations = Utils.findFloodFillArea(movePath.getEndCoord(), actor, actor.CO.army, actor.getMoveFunctor(),
//                                     Math.min(movePoints, actor.fuel), map, includeOccupiedDestinations, canTravelThroughEnemies);
//
//      for( XYCoord coord : destinations )
//      {
        // The GameInput machinery doesn't expect multiple relevant actions on the same target, and having 200 options isn't amazing either.
        XYCoord coord = movePath.getEndCoord();
        GamePath canto = Utils.findShortestPath(actor, coord, map);
        result.add(new RescueUnitAction(actor, movePath, other, canto));
//      }

      return result;
    }
  }

  public static class RescueUnitAction extends GameAction
  {
    private GamePath movePath, canto;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    Unit grabber;
    XYCoord target;

    public RescueUnitAction(Unit actor, GamePath path, Unit target, GamePath canto)
    {
      grabber = actor;
      this.target = new XYCoord(target); // lazy/10
      movePath = path;
      this.canto = canto;
      startCoord = new XYCoord(actor);
      moveCoord = movePath.getEndCoord();
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // action consists of
      //   MOVE
      //   RESCUE
      //   CANTO
      GameEventQueue events = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != target) && gameMap.isLocationValid(startCoord)
          && gameMap.isLocationValid(target) )
      {
        isValid &= grabber != null && !grabber.isTurnOver;
        isValid &= !gameMap.isLocationEmpty(grabber, target);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, grabber, movePath, events) )
        {
          // No surprises in the fog.
          events.add(new LoadEvent(gameMap.getResident(target), grabber));
          Utils.enqueueMoveEvent(gameMap, grabber, canto, events, true);
        }
      }
      return events;
    }

    @Override public Unit getActor() { return grabber; }
    @Override public XYCoord getMoveLocation() { return moveCoord; }
    @Override public XYCoord getTargetLocation() { return target; }
    @Override public UnitActionFactory getType() { return RescueUnitFactory.instance; }
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and rescue a unit at %s]", grabber.toStringWithLocation(), moveCoord, target);
    }
  }

  /**
   * Steal another unit's cargo; includes a drop and canto
   */
  public static class TakeUnitFactory extends SupportActionFactory
  {
    private static final long serialVersionUID = 1L;
    public static final TakeUnitFactory instance = new TakeUnitFactory();
    private Object readResolve() { return instance; }

    public TakeUnitFactory()
    {
      super("TAKE");
    }
    @Override
    public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      if( actor.heldUnits.size() >= actor.model.baseCargoCapacity )
        return false;
      // Can't take cargo that doesn't exist or that has cargo
      return other.heldUnits.size() > 0 && !(other.heldUnits.get(0).heldUnits.size() > 0);
    }
    @Override
    public ArrayList<GameAction> getSupportAll(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      Unit cargo = other.heldUnits.get(0); // Always grab the first one

      ArrayList<GameAction> result = new ArrayList<>();

      ArrayList<XYCoord> dropoffLocations = Utils.findUnloadLocations(map, actor, movePath.getEndCoord(), cargo);
      // MapController hates this
      // dropoffLocations.add(null); // If we can't drop, just canto

//      boolean includeOccupiedDestinations = false;
//      boolean canTravelThroughEnemies = false;
//      int movePoints = actor.getMovePower(map) - movePath.getFuelCost(actor, map);
//      ArrayList<XYCoord> destinations = Utils.findFloodFillArea(movePath.getEndCoord(), actor, actor.CO.army, actor.getMoveFunctor(),
//                                     Math.min(movePoints, actor.fuel), map, includeOccupiedDestinations, canTravelThroughEnemies);
//
//      for( XYCoord coord : destinations )
//      {
      // The GameInput machinery doesn't expect multiple relevant actions on the same target, and having 200 options isn't amazing either.
      XYCoord coord = movePath.getEndCoord();
        GamePath canto = Utils.findShortestPath(actor, coord, map);
        for( XYCoord dropLoc : dropoffLocations )
          if( !coord.equals(dropLoc) )
            result.add(new TakeUnitAction(actor, movePath, other, cargo, dropLoc, canto));
//      }

      return result;
    }
  }
  public static class TakeUnitAction extends GameAction
  {
    public GamePath movePath, canto;
    public XYCoord startCoord;
    public XYCoord moveCoord;
    public Unit grabber, target, cargo;
    public XYCoord dropLoc;

    public TakeUnitAction(Unit actor, GamePath path, Unit target, Unit cargo, XYCoord dropLoc, GamePath canto)
    {
      grabber = actor;
      this.target = target;
      this.cargo  = cargo;
      this.dropLoc = dropLoc;
      movePath = path;
      this.canto = canto;
      startCoord = new XYCoord(actor);
      moveCoord = movePath.getEndCoord();
      if( null != canto ) // Does this make sense?
        moveCoord = canto.getEndCoord();
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // action consists of
      //   MOVE
      //   TAKE
      //   [DROP]
      //   CANTO
      GameEventQueue events = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != target) && gameMap.isLocationValid(startCoord) )
      {
        isValid &= grabber != null && !grabber.isTurnOver;
        isValid &= target != null && cargo != null;
        isValid &= null == dropLoc || gameMap.isLocationEmpty(grabber, dropLoc);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
        isValid &= (canto != null) && (canto.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, grabber, movePath, events) )
        {
          // No surprises in the fog.
          events.add(new TakeUnitEvent(grabber, target, cargo));
          if( null != dropLoc )
            events.add(new UnloadEvent(grabber, cargo, dropLoc));
          Utils.enqueueMoveEvent(gameMap, grabber, canto, events);
        }
      }
      return events;
    }

    @Override public Unit getActor() { return grabber; }
    @Override public XYCoord getMoveLocation() { return moveCoord; }
    @Override public XYCoord getTargetLocation() { return dropLoc; }
    @Override public UnitActionFactory getType() { return TakeUnitFactory.instance; }
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s, take a unit from %s and drop it at %s]", grabber.toStringWithLocation(), moveCoord, target.toStringWithLocation(), dropLoc);
    }
  }
  public static class TakeUnitEvent implements GameEvent
  {
    private Unit unit, target, cargo;

    public TakeUnitEvent(Unit caster, Unit target, Unit cargo)
    {
      unit = caster;
      this.target = target;
      this.cargo  = cargo;
    }

    @Override
    public void performEvent(MapMaster map)
    {
      target.heldUnits.remove(cargo);
      unit.heldUnits.add(cargo);
    }

    @Override public GameAnimation getEventAnimation(MapView mapView) { return null; }
    @Override public GameEventQueue sendToListener(GameEventListener listener) { return null; }
    @Override public XYCoord getStartPoint() { return new XYCoord(unit); }
    @Override public XYCoord getEndPoint() { return new XYCoord(target); }
  }

  /**
   * Give another unit your cargo; includes canto
   */
  public static class GiveUnitFactory extends SupportActionFactory
  {
    private static final long serialVersionUID = 1L;
    public static final GiveUnitFactory instance = new GiveUnitFactory();
    private Object readResolve() { return instance; }

    public GiveUnitFactory()
    {
      super("GIVE");
    }
    @Override
    public boolean canSupport(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      if( actor.heldUnits.size() < 1 )
        return false;
      // I'm just going to ignore the possibility of giving a unit to a ship that already has cargo.
      return other.model.baseCargoCapacity > 0 && other.heldUnits.size() < 1;
    }
    @Override
    public ArrayList<GameAction> getSupportAll(GameMap map, Unit actor, GamePath movePath, Unit other)
    {
      Unit cargo = actor.heldUnits.get(0); // Always give the first one

      ArrayList<GameAction> result = new ArrayList<>();

//      boolean includeOccupiedDestinations = false;
//      boolean canTravelThroughEnemies = false;
//      int movePoints = actor.getMovePower(map) - movePath.getFuelCost(actor, map);
//      ArrayList<XYCoord> destinations = Utils.findFloodFillArea(movePath.getEndCoord(), actor, actor.CO.army, actor.getMoveFunctor(),
//                                     Math.min(movePoints, actor.fuel), map, includeOccupiedDestinations, canTravelThroughEnemies);
//
//      for( XYCoord coord : destinations )
//      {
        // The GameInput machinery doesn't expect multiple relevant actions on the same target, and having 200 options isn't amazing either.
        XYCoord coord = movePath.getEndCoord();
        GamePath canto = Utils.findShortestPath(actor, coord, map);
        result.add(new GiveUnitAction(actor, movePath, other, cargo, canto));
//      }

      return result;
    }
  }
  public static class GiveUnitAction extends GameAction
  {
    public GamePath movePath, canto;
    public XYCoord startCoord;
    public XYCoord moveCoord;
    public Unit giver, target, cargo;

    public GiveUnitAction(Unit actor, GamePath path, Unit target, Unit cargo, GamePath canto)
    {
      giver = actor;
      this.target = target;
      this.cargo  = cargo;
      movePath = path;
      this.canto = canto;
      startCoord = new XYCoord(actor);
      moveCoord = movePath.getEndCoord();
      if( null != canto ) // Does this make sense?
        moveCoord = canto.getEndCoord();
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // action consists of
      //   MOVE
      //   GIVE
      //   CANTO
      GameEventQueue events = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && (null != target) && gameMap.isLocationValid(startCoord) )
      {
        isValid &= giver != null && !giver.isTurnOver;
        isValid &= target != null && cargo != null;
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
        isValid &= (canto != null) && (canto.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, giver, movePath, events) )
        {
          // No surprises in the fog.
          events.add(new TakeUnitEvent(target, giver, cargo)); // Flip the recipient and donor
          Utils.enqueueMoveEvent(gameMap, giver, canto, events);
        }
      }
      return events;
    }

    @Override public Unit getActor() { return giver; }
    @Override public XYCoord getMoveLocation() { return moveCoord; }
    @Override public XYCoord getTargetLocation() { return new XYCoord(target); }
    @Override public UnitActionFactory getType() { return GiveUnitFactory.instance; }
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s, give a unit to %s]", giver.toStringWithLocation(), moveCoord, target.toStringWithLocation());
    }
  }

  /**
   * Provides an alternate method to get units unloaded from units; includes canto
   */
  public static class DropUnitFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public static final String name = "DROP";
    public static final DropUnitFactory instance = new DropUnitFactory();

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      if( actor.heldUnits.size() <= 0 )
        return null;
      Unit cargo = actor.heldUnits.get(0); // Always drop the first one

      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        ArrayList<GameAction> options = new ArrayList<GameAction>();

        ArrayList<XYCoord> dropoffLocations = Utils.findUnloadLocations(map, actor, movePath.getEndCoord(), cargo);

//        boolean includeOccupiedDestinations = false;
//        boolean canTravelThroughEnemies = false;
//        int movePoints = actor.getMovePower(map) - movePath.getFuelCost(actor, map);
//        ArrayList<XYCoord> destinations = Utils.findFloodFillArea(movePath.getEndCoord(), actor, actor.CO.army, actor.getMoveFunctor(),
//                                       Math.min(movePoints, actor.fuel), map, includeOccupiedDestinations, canTravelThroughEnemies);
//
//        for( XYCoord coord : destinations )
//        {
          // The GameInput machinery doesn't expect multiple relevant actions on the same target, and having 200 options isn't amazing either.
          XYCoord coord = movePath.getEndCoord();
          GamePath canto = Utils.findShortestPath(actor, coord, map);
          for( XYCoord dropLoc : dropoffLocations )
            if( !coord.equals(dropLoc) )
              options.add(new DropUnitAction(actor, movePath, cargo, dropLoc, canto));
//        }

        // Only add this action set if we actually have a target
        if( !options.isEmpty() )
        {
          return new GameActionSet(options);
        }
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return name;
    }
    private Object readResolve()
    {
      return instance;
    }
  }
  public static class DropUnitAction extends GameAction
  {
    private GamePath movePath, canto;
    private XYCoord startCoord;
    private XYCoord moveCoord;
    Unit grabber, cargo;
    XYCoord dropLoc;

    public DropUnitAction(Unit actor, GamePath path, Unit cargo, XYCoord dropLoc, GamePath canto)
    {
      grabber = actor;
      this.cargo  = cargo;
      this.dropLoc = dropLoc;
      movePath = path;
      this.canto = canto;
      startCoord = new XYCoord(actor);
      moveCoord = movePath.getEndCoord();
      if( null != canto ) // Does this make sense?
        moveCoord = canto.getEndCoord();
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // action consists of
      //   MOVE
      //   DROP
      //   CANTO
      GameEventQueue events = new GameEventQueue();

      boolean isValid = true;

      if( (null != gameMap) && (null != startCoord) && gameMap.isLocationValid(startCoord)
          && gameMap.isLocationValid(dropLoc) )
      {
        isValid &= grabber != null && !grabber.isTurnOver;
        isValid &= gameMap.isLocationEmpty(grabber, dropLoc);
        isValid &= (movePath != null) && (movePath.getPathLength() > 0);
        isValid &= (canto != null) && (canto.getPathLength() > 0);
      }
      else
        isValid = false;

      if( isValid )
      {
        if( Utils.enqueueMoveEvent(gameMap, grabber, movePath, events) )
        {
          // No surprises in the fog.
          events.add(new UnloadEvent(grabber, cargo, dropLoc));
          Utils.enqueueMoveEvent(gameMap, grabber, canto, events);
        }
      }
      return events;
    }

    @Override public Unit getActor() { return grabber; }
    @Override public XYCoord getMoveLocation() { return moveCoord; }
    @Override public XYCoord getTargetLocation() { return dropLoc; }
    @Override public UnitActionFactory getType() { return DropUnitFactory.instance; }
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and drop a unit at %s]", grabber.toStringWithLocation(), moveCoord, dropLoc);
    }
  }
}
