package AI;

import java.util.*;
import java.util.Map.Entry;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.*;
import Engine.GamePath.PathNode;
import Engine.Utils.SearchNode;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.Combat.StrikeParams;
import Engine.Combat.CombatContext.CalcType;
import Engine.UnitActionLifecycles.CaptureLifecycle;
import Engine.UnitActionLifecycles.BattleLifecycle.BattleAction;
import Engine.UnitActionLifecycles.CaptureLifecycle.CaptureAction;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.*;
import Units.*;
import Units.MoveTypes.MoveType;
import lombok.var;

public class WallyAI extends ModularAI
{
  public WallyAI(Army army)
  {
    super(army);
    aiPhases = new ArrayList<AIModule>(
        Arrays.asList(
            new PowerActivator(army, CommanderAbility.PHASE_TURN_START),
            new DrainActionQueue(army, this),
            new GenerateThreatMap(army, this), // FreeRealEstate and Travel need this, and NHitKO/building do too because of eviction

            new WallyCapper(army, this),
            new NHitKO(army, this),
            new SiegeAttacks(army, this),
            new PowerActivator(army, CommanderAbility.PHASE_BUY),
            new BuildStuff(army, this),
            new FreeRealEstate(army, this),
            new Eviction(army, this), // Getting dudes out of the way

            new FillActionQueue(army, this),
            new PowerActivator(army, CommanderAbility.PHASE_TURN_END)
            ));
  }

  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Army army)
    {
      return new WallyAI(army);
    }

    @Override
    public String getName()
    {
      return "Wally";
    }

    @Override
    public String getDescription()
    {
      return
          "Wally values units based on firepower and the area they can threaten.\n" +
          "He tries to keep units out of harm's way, and to protect expensive units with cheaper ones.\n" +
          "He can be overly timid, and thus is a fan of artillery.";
    }
  }
  public static final AIMaker info = new instantiator();

  @Override
  public AIMaker getAIInfo()
  {
    return info;
  }

  // What % funds loss of the unit will make me not build the unit
  private static final int BUILD_SCARE_PERCENT = 50;
  private static final int    UNIT_CAPTURE_RANGE = 4; // number of turns of movement to consider capture goals within
  private static final int    UNIT_HEAL_THRESHOLD = 6; // HP at which units heal
  private static final double UNIT_REFUEL_THRESHOLD = 1.3; // Factor of cost to get to fuel to start worrying about fuel
  private static final double UNIT_REARM_THRESHOLD = 0.25; // Fraction of ammo in any weapon below which to consider resupply
  private static final double AGGRO_EFFECT_THRESHOLD = 0.55; // % of a kill required to want to attack something
  private static final double AGGRO_FUNDS_WEIGHT = 0.9; // Multiplier on damage I need to get before a sacrifice is worth it
  private static final int    YEET_FUNDS_BIAS = 1000; // Bias against yeets
  private static final int    KILL_FUNDS_BIAS = 500; // Bias towards kills
  private static final int    CHIP_FUNDS_BIAS = 500; // Bias towards hitting full HP units
  private static final double RANGE_WEIGHT = 1; // Exponent for how powerful range is considered to be
  private static final double TERRAIN_PENALTY_WEIGHT = 3; // Exponent for how crippling we think high move costs are
  private static final double MIN_SIEGE_RANGE_WEIGHT = 0.8; // Exponent for how much to penalize siege weapon ranges for their min ranges

  private static final double COUNTER_EFFICIENCY_FACTOR = 0.6; // Factor of how well we assume enemy units are countered by existing units
  private static final double BANK_EFFICIENCY_FACTOR = 1.7; // Minimum effectiveness multiplier to consider banking for a better counter
  private static final double MAX_BANK_FUNDS_FACTOR = 2.5; // Maximum to bank compared to a counter you can actually buy
  private static final int    MAX_UNITS_PER_CAP_UNIT = 10; // Max acceptable number of units per capture unit

  private static final double COMPLETE_CAPTURE_WEIGHT = 4; // Roughly corresponds to the number of turns of prop value we expect to lose by not finishing a capture.
  private static final double TERRAIN_FUNDS_WEIGHT = 2.5; // Multiplier for per-city income for adding value to units threatening to cap
  private static final double TERRAIN_INDUSTRY_WEIGHT = 20000; // Funds amount added to units threatening to cap an industry
  private static final double TERRAIN_HQ_WEIGHT = 42000; //                  "                                      HQ

  private static final CalcType CALC = CalcType.PESSIMISTIC;

  private static enum TravelPurpose
  {
    // BUSINESS, PLEASURE,
    WANDER(0), SUPPLIES(7), KILL(13), CONQUER(42), BESIEGE(64), NA(99);
    public final int priority;
    TravelPurpose(int p)
    {
      priority = p;
    }
  }
  private static class ActionPlan
  {
    final Object whodunit;
    final UnitContext actor;
    final GameAction action;
    final XYCoord startPos;
    GamePath path;
    public XYCoord goal;
    boolean isAttack = false;
    int percentDamage = -1; // The damage we expect to deal with our attack
    int score;
    XYCoord clearTile; // The tile we expect to have emptied with our attack, if any; populated when queueing our final action order
    TravelPurpose purpose = TravelPurpose.NA;
    ActionPlan(Object whodunit, UnitContext uc, GameAction action, int pScore)
    {
      this.whodunit = whodunit;
      this.action = action;
      this.actor  = uc;
      this.path   = uc.path;
      startPos    = uc.path.getWaypoint(0).GetCoordinates();
      score       = pScore;
    }
    @Override
    public String toString()
    {
      if( null == clearTile )
        return whodunit.toString() + "\n\t" + action.toString() + "\n";
      return whodunit.toString() + "\n\t" + String.format("%s\n\tclearing %s", action, clearTile) + "\n";
    }
  }
  public ActionPlan lastAction;
  private Queue<ActionPlan> queuedActions = new ArrayDeque<>();
  private static class UnitPrediction
  {
    UnitContext identity; // Must be non-null if toAchieve is; UC's unit may be null.
    HashMap<ActionPlan, Integer> damageInstances = new HashMap<>();
    ActionPlan toAchieve;
  }
  private UnitPrediction[][] mapPlan;
  public PredictionMap predMap; // Owns a reference to the above, to expose its contents to Utils
  private HashMap<Unit, ActionPlan> plansByUnit;
  private HashMap<XYCoord, TravelPurpose> travelPlanCoords = new HashMap<>();;
  private static class TileThreat
  {
    UnitContext identity;
    ArrayList<WeaponModel> relevantWeapons = new ArrayList<>();
    HashSet<Utils.SearchNode> hitFrom = new HashSet<>();
  }
  /**
   * For each X/Y coordinate, stores the enemies that can threaten this tile and what weapon(s) they can do it with
   * <p>Doesn't consider current allied unit positions/blocking
   * <p>Each unit should only have one UnitContext (matching mapPlan) in this map, so predicted damage can propagate to all tiles automagically
   */
  public ArrayList<TileThreat>[][] threatMap;
  private ArrayList<Unit> allThreats;
  private HashMap<ModelForCO, Double> unitEffectiveMove = null; // How well the unit can move, on average, on this map

  private void init(GameMap map)
  {
    lastAction = null;
    if( !queuedActions.isEmpty() )
      queuedActions.clear();
    allThreats = new ArrayList<Unit>(); // implicitly resets ThreatMap
    travelPlanCoords.clear();

    if( null != unitEffectiveMove )
      return;

    capPhase = new CapPhaseAnalyzer(map, myArmy);
    mapPlan = new UnitPrediction[map.mapWidth][map.mapHeight];
    predMap = new PredictionMap(myArmy, mapPlan);
    plansByUnit = new HashMap<>();
    for( int x = 0; x < map.mapWidth; ++x )
      for( int y = 0; y < map.mapHeight; ++y )
      {
        mapPlan[x][y] = new UnitPrediction();
      }

    unitEffectiveMove = new HashMap<>();
    // init all move multipliers before powers come into play
    for( Army army : map.game.armies )
    {
      for( Commander co : army.cos )
      {
        for( UnitModel model : co.unitModels )
        {
          getEffectiveMove(new ModelForCO(co, model));
        }
      }
    }
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    init(gameMap);
    super.initTurn(gameMap);
    log(String.format("[======== Wally initializing turn %s for %s =========]", turnNum, myArmy));
  }

  @Override
  public void endTurn()
  {
    super.endTurn();
    log(String.format("[======== Wally ending turn %s for %s =========]", turnNum, myArmy));
  }

  private void updatePlan(Object whodunit, int score, Unit unit, GamePath path, GameAction action)
  {
    updatePlan(whodunit, score, unit, path, action, false, 42);
  }
  private void updatePlan(Object whodunit, int score, Unit unit, GamePath path, GameAction action, boolean isAttack, int percentDamage)
  {
    UnitContext uc = new UnitContext(unit);
    uc.path = path;
    updatePlan(whodunit, score, uc, action, isAttack, percentDamage);
  }
  private void updatePlan(Object whodunit, int score, UnitContext uc, GameAction action, boolean isAttack, int percentDamage)
  {
    if( null == action )
      return;
    final ActionPlan plan = new ActionPlan(whodunit, uc, action, score);
    plan.isAttack = isAttack;
    plan.percentDamage = percentDamage;
    updatePlan(plan);
  }
  private void updatePlan(ActionPlan plan)
  {
    XYCoord dest = plan.action.getMoveLocation();
    final UnitPrediction destPredictTile = mapPlan[dest.x][dest.y];
    ActionPlan evicted = destPredictTile.toAchieve;
    ActionPlan actorPrevPlan = plansByUnit.get(plan.actor.unit);

    final Unit predResident = predMap.getResident(dest);
    final Unit actorIdentity = plan.actor.unit;
    if( null != predResident && predResident != actorIdentity )
    {
      var clearTiles = predMap.calcPredictedClearTiles();
      clearTiles.clear();
    }

    cancelPlan(evicted);
    cancelPlan(actorPrevPlan);

    destPredictTile.toAchieve = plan;
    destPredictTile.identity = plan.actor;
    plan.actor.coord = dest;
    if( null != actorIdentity )
      plansByUnit.put(actorIdentity, plan);
    if( null != plan.goal )
      travelPlanCoords.put(plan.goal, plan.purpose);

    if( plan.isAttack )
    {
      final XYCoord target = plan.action.getTargetLocation();
      mapPlan[target.x][target.y].damageInstances.put(plan, plan.percentDamage);
    }
  }

  private void cancelPlan(ActionPlan evicted)
  {
    if( null == evicted )
      return;
    if( null != evicted.actor.unit )
      plansByUnit.remove(evicted.actor.unit);
    XYCoord moveLoc = evicted.action.getMoveLocation();
    UnitPrediction predToChange = mapPlan[moveLoc.x][moveLoc.y];
    if( evicted == predToChange.toAchieve )
    {
      predToChange.identity = null;
      predToChange.toAchieve = null;
    }
    // Assume only one attack vs any given target - this is not always true, but I don't care
    if( evicted.action.getType() == UnitActionFactory.ATTACK )
    {
      final XYCoord ctt = evicted.action.getTargetLocation();
      mapPlan[ctt.x][ctt.y].damageInstances.remove(evicted);
    }
    if( null != evicted.goal )
      travelPlanCoords.remove(evicted.goal);
    // Don't mess with canceling attacks that clear tiles, at least for now
    //      final XYCoord cct = canceled.clearTile;
    //      if( null != cct )
    //        mapPlan[cct.x][cct.y].identity =
    // Don't chain cancellations - if Wally is dumb enough to cause that, he can recalculate :P
  }

  /**
   * @return Whether things are going as planned
   */
  private boolean checkIfUnexpected(GameMap gameMap)
  {
    boolean theUnexpected = false;
    if( null != lastAction )
    {
      final GameAction action = lastAction.action;
      final Unit actor = action.getActor();
      if( null != actor ) // Make sure only our unit is in the start and end position
      {
        if( !gameMap.isLocationEmpty(actor, lastAction.startPos) )
        {
          log(String.format("  Unexpected condition:"));
          log(String.format("    Actor didn't move for action: %s", lastAction));
          theUnexpected = true;
        }
        // Consider removing this check if I am yeeting my unit
        if( actor != gameMap.getResident(action.getMoveLocation()) )
        {
          log(String.format("  Unexpected condition:"));
          log(String.format("    Actor died for action: %s", lastAction));
          theUnexpected = true;
        }
      }
      final XYCoord clearTile = lastAction.clearTile;
      if( null != clearTile && null != gameMap.getResident(clearTile) )
      {
        log(String.format("  Unexpected condition:"));
        log(String.format("    Failed to clear tile %s for action: %s", clearTile, lastAction));
        theUnexpected = true;
      }
    }
    return theUnexpected;
  }

  public GameAction pollAndCleanUpAction(GameMap map)
  {
    ActionPlan ae = queuedActions.poll();

    // Check if it's an attack and has been invalidated by RNG shenanigans
    while (null != ae)
    {
      final GameAction action = ae.action;
      XYCoord moveLoc = action.getMoveLocation();
      if( null != moveLoc )
      {
        mapPlan[moveLoc.x][moveLoc.y].toAchieve = null;
        // TODO: Revisit if I add join/load
        if( !map.isLocationEmpty(action.getActor(), moveLoc) )
        {
          log(String.format("  Discarding invalid movement for: %s", ae.action));
          ae = queuedActions.poll();
          continue;
        }
        // If we do an HQ cap, don't surrender the rest of the turn
        if( ae.action.getType() == UnitActionFactory.CAPTURE && !myArmy.isEnemy(map.getLocation(moveLoc).getOwner()) )
        {
          log(String.format("  Discarding invalid capture: %s", ae.action));
          ae = queuedActions.poll();
          continue;
        }
      }

      Unit victim = map.getResident(ae.action.getTargetLocation());
      if( ae.action instanceof GameAction.UnitProductionAction )
      {
        boolean fail = false;
        if( null != victim )
        {
          log(String.format("  Discarding blocked build: %s", ae.action));
          fail = true;
        }
        XYCoord xyc = ae.action.getTargetLocation();
        UnitContext toBuild = ae.actor;
        int cost = toBuild.CO.getBuyCost(toBuild.model, xyc);
        if( cost > myArmy.money )
        {
          log(String.format("  Discarding too-expensive build: %s for %s with %s on hand", ae.action, cost, myArmy.money));
          fail = true;
        }
        if( fail )
        {
          ae = queuedActions.poll();
          continue;
        }
      }
      if( ae.action.getType() == UnitActionFactory.ATTACK && (null == victim || !victim.CO.isEnemy(myArmy)) )
      {
        log(String.format("  Discarding invalid attack: %s", ae.action));
        ae = queuedActions.poll();
        continue;
      }
      break; // Action is valid; ship it
    }

    lastAction = ae;
    if( null == ae )
      return null;
    return ae.action;
  }

  public static class DrainActionQueue implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myArmy;
    public final WallyAI ai;

    public DrainActionQueue(Army co, WallyAI ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap map)
    {
      boolean theUnexpected = ai.checkIfUnexpected(map);
      // If anything we didn't expect happened, scrap and re-plan everything
      if( theUnexpected )
        ai.init(map);

      return ai.pollAndCleanUpAction(map);
    }
  }
  public static class FillActionQueue implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myArmy;
    public final WallyAI ai;

    public FillActionQueue(Army co, WallyAI ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap map)
    {
      ai.log(String.format("  Filling action queue from map plan"));
      HashSet<XYCoord> vacatedTiles = new HashSet<>();
      HashSet<XYCoord> revisitTiles = new HashSet<>();
      HashMap<Unit, ActionPlan> actionsBooked = new HashMap<>();

      for( var plan : ai.plansByUnit.values() )
        {
          var coord = plan.path.getEndCoord();
          int x = coord.x, y = coord.y;
          ActionPlan readyPlan = fetchPlanAndVacateTiles(vacatedTiles, ai, map, x, y);
          if( null != readyPlan )
          {
            final UnitContext actor = ai.mapPlan[x][y].identity;
            if( null != actor.unit )
            {
              if( actionsBooked.containsKey(actor.unit) )
                ai.log(String.format("Warning: Unit is double-booked %s:\n\t%s\n\t%s", actor, readyPlan, actionsBooked.get(actor.unit)));
              else
                actionsBooked.put(actor.unit, readyPlan);
            }
            ai.queuedActions.add(readyPlan);
          }
          else if( null != ai.mapPlan[x][y].toAchieve )
            revisitTiles.add(new XYCoord(x, y));
        }

      XYCoord actionAtCoord = new XYCoord(-1, -1);
      while (null != actionAtCoord)
      {
        actionAtCoord = null;
        for( XYCoord movexyc : revisitTiles )
        {
          int x = movexyc.x, y = movexyc.y;
          ActionPlan readyPlan = fetchPlanAndVacateTiles(vacatedTiles, ai, map, x, y);
          if( null != readyPlan )
          {
            final UnitContext actor = ai.mapPlan[x][y].identity;
            if( null != actor.unit )
            {
              if( actionsBooked.containsKey(actor.unit) )
                ai.log(String.format("Warning: Unit is double-booked %s:\n\t%s\n\t%s", actor, readyPlan, actionsBooked.get(actor.unit)));
              else
                actionsBooked.put(actor.unit, readyPlan);
            }
            ai.queuedActions.add(readyPlan);
            actionAtCoord = movexyc;
            break;
          }
        }
        if( null != actionAtCoord )
          revisitTiles.remove(actionAtCoord);
      }

      // Clear out any stragglers to maybe re-plan
      for( XYCoord movexyc : revisitTiles )
      {
        int x = movexyc.x, y = movexyc.y;
        final Unit unit = ai.mapPlan[x][y].identity.unit;
        ai.mapPlan[x][y].identity = null;
        ai.mapPlan[x][y].toAchieve = null;
        if( null != unit )
          ai.plansByUnit.remove(unit);
      }

      ai.plansByUnit.clear();
      if( ai.queuedActions.isEmpty() )
        return null;

      ai.log(String.format("  Actions:\n%s", ai.queuedActions));
      GameAction action = ai.pollAndCleanUpAction(map);
      return action;
    }

    private static ActionPlan fetchPlanAndVacateTiles(HashSet<XYCoord> vacatedTiles,
                                       WallyAI ai, GameMap map,
                                       int x, int y)
    {
      UnitContext actor = ai.mapPlan[x][y].identity;
      ActionPlan  plan  = ai.mapPlan[x][y].toAchieve;
      if( null == plan )
        return null; // Nothing to do here

      final Unit unit = actor.unit;
      if( null != unit && unit.isTurnOver )
      {
        ai.log(String.format("Warning: Action planned for tired unit %s:\n\t%s", unit, plan));
        return null; // Nothing to do here
      }

      final XYCoord movexyc = new XYCoord(x, y);
      if( !vacatedTiles.contains(movexyc)
          && !map.isLocationEmpty(unit, movexyc)
          )
        return null; // Location is full and won't be emptied by our current confirmed plans

      // Assuming our path has been planned well re:terrain, running into enemies is the only obstacle
      GamePath movePath = plan.path;
      if( null != movePath )
      {
        MoveType fff = actor.calculateMoveType();
        ArrayList<PathNode> waypoints = movePath.getWaypoints();
        // We iterate from 1 because the first waypoint is the unit's initial position.
        for( int i = 1; i < waypoints.size(); i++)
        {
          XYCoord from = waypoints.get(i-1).GetCoordinates();
          XYCoord to   = waypoints.get( i ).GetCoordinates();
          int cost = fff.getTransitionCost(map, from, to, actor.CO.army, false);
          if( cost > actor.movePower )
            if( !vacatedTiles.contains(to) )
              return null;
        }
      }

      Unit gaActor = plan.action.getActor();
      if( gaActor != null && actor.unit != gaActor )
        ai.log(String.format("Warning: plan/action mismatch with %s:\n\t%s\n\t!=\n\t%s", plan, actor.unit, gaActor));
      if( null != plan.startPos )
        vacatedTiles.add(plan.startPos);

      // If we're an attack whose damage sums up to clear the target out, populate that state now.
      if( plan.action.getType() == UnitActionFactory.ATTACK )
      {
        XYCoord tt = plan.action.getTargetLocation();
        final UnitPrediction targetPredictTile = ai.mapPlan[tt.x][tt.y];
        final UnitContext target = targetPredictTile.identity;

        if( null == target )
        {
          plan.clearTile = tt;
          vacatedTiles.add(tt);
        }
        // Predict board state, if we still have the info banging around
        else if( targetPredictTile.damageInstances.containsKey(plan) )
        {
          int percentDamage = targetPredictTile.damageInstances.get(plan);
          targetPredictTile.damageInstances.remove(plan);

          boolean tileCleared = false;
          // If we're already planning to put a different unit there, we can assume it's a kill
          if( target.unit != map.getResident(tt) )
            tileCleared = true;
          else // If not, track state to find out if it's a kill
          {
            target.damageHealth(percentDamage, true); // Destructively modifying the planning state is fine and convenient at this stage
            if( 1 > target.getHP() )
              tileCleared = true; // If we're already planning to put some
          }

          // If we've run out of planned shots and we expect to clear, make it official
          if( tileCleared && targetPredictTile.damageInstances.size() < 1 )
          {
            plan.clearTile = tt;
            vacatedTiles.add(tt);
          }
        }
      }
      return plan;
    }
  }

  public static class WallyCapper extends CapChainActuator<WallyAI>
  {
    private static final long serialVersionUID = 1L;
    public WallyCapper(Army co, WallyAI ai)
    {
      super(co, ai);
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap map)
    {
      GameAction capAction = super.getUnitAction(unit, map);
      int capProgress = unit.getCaptureProgress();
      if( null == capAction )
      {
        if( capProgress == 0 )
          return null;
        XYCoord xyc = new XYCoord(unit);
        ai.futureCapTargets.remove(xyc);
        capAction = new CaptureLifecycle.CaptureAction(map, unit, GamePath.stayPut(xyc));
      }

      XYCoord mc = capAction.getMoveLocation();
      int finalCapAmt = capProgress + new UnitContext(unit).calculateCapturePower();
      boolean willCapture = finalCapAmt >= map.getEnvironment(mc).terrainType.getCaptureThreshold();
      // If there's a threat here, don't assume we can naively capture
      if( !willCapture && 0 < ai.threatMap[mc.x][mc.y].size() )
        return null;

      int capScore = 500;
      if( capAction instanceof CaptureLifecycle.CaptureAction )
        capScore = ai.valueCapture((CaptureLifecycle.CaptureAction) capAction, map);
      ai.updatePlan(this, capScore, unit, Utils.findShortestPath(unit, mc, map), capAction);
      return null;
    }
  }

  public static class SiegeAttacks extends UnitActionFinder<WallyAI>
  {
    private static final long serialVersionUID = 1L;
    public SiegeAttacks(Army army, WallyAI ai)
    {
      super(army, ai);
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap gameMap)
    {
      if( ai.plansByUnit.containsKey(unit) )
        return null;
      boolean isSiege = unit.model.isAny(UnitModel.SIEGE);
      isSiege |= unit.model.hasImmobileWeapon();
      if( !isSiege )
        return null;

      // Find the possible destination.
      XYCoord coord = new XYCoord(unit.x, unit.y);

      if( AIUtils.isFriendlyProduction(ai.predMap, myArmy, coord) || !unit.model.hasImmobileWeapon() )
        return null;
      UnitContext resident = ai.mapPlan[coord.x][coord.y].identity;
      // If we've already made plans here, skip evaluation
      if( null != resident )
        return null;

      GameAction bestAttack = null;
      int percentDamage = 0;
      GamePath movePath = GamePath.stayPut(coord);

      // Figure out what I can do here.
      ArrayList<GameActionSet> actionSets = unit.getPossibleActions(ai.predMap, movePath);
      int bestDamage = 0;
      for( GameActionSet actionSet : actionSets )
      {
        // See if we have the option to attack.
        if( actionSet.getSelected().getType() == UnitActionFactory.ATTACK )
        {
          for( GameAction action : actionSet.getGameActions() )
          {
            MapLocation loc = gameMap.getLocation(action.getTargetLocation());
            Unit target = loc.getResident();
            if( null == target ) continue; // Ignore terrain
            final BattleSummary results = CombatEngine.simulateBattleResults(unit, target, gameMap, movePath, CALC);
            int damage = valueUnit(target, loc, false) * Math.min(target.getHealth(), results.defender.getPreciseHealthDamage()) / UnitModel.MAXIMUM_HEALTH;
            if( damage > bestDamage )
            {
              bestDamage = damage;
              bestAttack = action;
              percentDamage = results.defender.getPreciseHealthDamage();
            }
          }
        }
      }
      if( null != bestAttack )
      {
//        ai.log(String.format("%s is shooting %s",
//            unit.toStringWithLocation(), gameMap.getLocation(bestAttack.getTargetLocation()).getResident()));
      }

      boolean isAttack = true;
      ai.updatePlan(this, bestDamage, unit, movePath, bestAttack, isAttack, percentDamage);

      return null;
    }
  }

  // Try to get confirmed kills with mobile strikes.
  public static class NHitKO implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myArmy;
    public final WallyAI ai;

    public NHitKO(Army army, WallyAI ai)
    {
      myArmy = army;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      HashSet<XYCoord> industries = new HashSet<XYCoord>();
      for( XYCoord coord : myArmy.getOwnedProperties() )
        if( myArmy.cos[0].unitProductionByTerrain.containsKey(gameMap.getEnvironment(coord).terrainType)
            || TerrainType.HEADQUARTERS == gameMap.getEnvironment(coord).terrainType
            || TerrainType.LAB == gameMap.getEnvironment(coord).terrainType )
          industries.add(coord);

      // Initialize to targeting all spaces on or next to industries+HQ, since those are important spots
      HashSet<XYCoord> targets = new HashSet<XYCoord>();

      HashSet<XYCoord> industryBlockers = new HashSet<XYCoord>();
      for( XYCoord coord : industries )
        industryBlockers.addAll(Utils.findLocationsInRange(ai.predMap, coord, 0, 1));

      for( XYCoord coord : industryBlockers )
      {
        Unit resident = ai.predMap.getResident(coord);
        if( null != resident && myArmy.isEnemy(resident.CO) )
          targets.add(coord);
      }

      ArrayList<Unit> attackerOptions = new ArrayList<>(unitQueue);
      attackerOptions.removeAll(ai.plansByUnit.keySet());
      XYCoord targetLoc = null;
      for( XYCoord coord : new ArrayList<XYCoord>(targets) )
      {
        Unit targetID = ai.predMap.getResident(coord);
        if( null == targetID || !myArmy.isEnemy(targetID.CO) )
        {
          ai.log(String.format("    Warning! NHitKO is trying to kill invalid target: %s at %s", targetID, coord));
          continue;
        }
        UnitContext target = new UnitContext(targetID);
        int damageTotal = 0;
        for( int hit : ai.mapPlan[coord.x][coord.y].damageInstances.values() )
          damageTotal += hit;

        targetLoc = coord;
        Map<XYCoord, Unit> neededAttacks = AICombatUtils.findMultiHitKill(ai.predMap, target.unit, attackerOptions, industries);
        if( null == neededAttacks )
          continue;

        // All hits get the same score since they're all needed for a kill.
        int score = valueUnit(targetID, gameMap.getLocation(targetLoc), true) * 100;
        for( XYCoord xyc : neededAttacks.keySet() )
        {
          Unit unit = neededAttacks.get(xyc);
          if( unit.isTurnOver || !gameMap.isLocationEmpty(unit, xyc) )
            continue;
          UnitContext resident = ai.mapPlan[xyc.x][xyc.y].identity;
          if( null != resident && resident.unit.isTurnOver )
          {
            ai.log("    Warning: NHitKO ran into an un-evictable unit");
            continue;
          }

          final GamePath movePath = Utils.findShortestPath(unit, xyc, ai.predMap);
          final UnitContext attacker = new UnitContext(gameMap, unit);
          attacker.setPath(movePath);

          BattleSummary results = CombatEngine.simulateBattleResults(attacker, target, ai.predMap, CALC);
          final int percentDamage = results.defender.getPreciseHealthDamage();
          damageTotal += percentDamage;
//          ai.log(String.format("    %s hits for %s, total: %s", unit.toStringWithLocation(), percentDamage, target));

          boolean isAttack = true;
          final BattleAction attack = new BattleAction(ai.predMap, unit, movePath, targetLoc.x, targetLoc.y);

          ai.updatePlan(this, score, unit, movePath, attack, isAttack, percentDamage);
          attackerOptions.remove(unit);
          if( damageTotal >= target.getHealth() )
            break;
        }
      }

      return null;
    }
  }

  public static class GenerateThreatMap implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public final Army myArmy;
    public final WallyAI ai;

    public GenerateThreatMap(Army co, WallyAI ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      // We're already init'd, and nothing unexpected has happened. No need to recalc.
      if( 0 < ai.allThreats.size() )
        return null;

      // Re-initialize our plans
      for( int x = 0; x < gameMap.mapWidth; ++x )
        for( int y = 0; y < gameMap.mapHeight; ++y )
        {
          // Blank out last turn's plan
          ai.mapPlan[x][y].identity = null;
          ai.mapPlan[x][y].toAchieve = null;
          ai.mapPlan[x][y].damageInstances.clear();
          Unit resident = gameMap.getResident(x, y);
          if( null == resident )
            continue;
          // If we know we can't move this unit, put it in the plan as semi-final
          if( resident.isTurnOver || myArmy != resident.CO.army )
          {
            ai.mapPlan[x][y].identity = new UnitContext(gameMap, resident);
          }
        }
      ai.plansByUnit.clear();

      Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myArmy, gameMap);
      for( Commander co : unitLists.keySet() )
      {
        if( myArmy.isEnemy(co) )
        {
          for( Unit threat : unitLists.get(co) )
          {
            ai.allThreats.add(threat);
          }
        }
      }
      boolean ignoreFriendlyBlockers = true;
      ai.threatMap = buildThreatMap(gameMap, unitLists, ai.mapPlan, myArmy, ignoreFriendlyBlockers);

      return null;
    }
  }

  @SuppressWarnings("unchecked") // Java whines if I use generics and lists at the same time
  public static ArrayList<TileThreat>[][] buildThreatMap(
                                          GameMap map, Map<Commander, ArrayList<Unit>> unitLists,
                                          UnitPrediction[][] mapPlan, Army myArmy,
                                          boolean ignoreFriendlyBlockers)
  {
    ArrayList<TileThreat>[][] threatMap = new ArrayList[map.mapWidth][map.mapHeight];
    for( int x = 0; x < map.mapWidth; ++x )
      for( int y = 0; y < map.mapHeight; ++y )
      {
        threatMap[x][y] = new ArrayList<>();
      }
    for( Commander co : unitLists.keySet() )
    {
      if( myArmy.isEnemy(co) )
      {
        for( Unit threat : unitLists.get(co) )
        {
          // Use the provided UnitContext so that it will be the same instance and receive HP updates
          populateTileThreats(threatMap, map, mapPlan[threat.x][threat.y].identity, ignoreFriendlyBlockers);
        }
      }
    }
    return threatMap;
  }
  public static void populateTileThreats(
                     ArrayList<TileThreat>[][] threatMap,
                     GameMap gameMap, UnitContext threat,
                     boolean ignoreFriendlyBlockers)
  {
    // Temporary cache to re-locate already-threatened tiles' Tile Threats
    Map<XYCoord, TileThreat> shootableTiles = new HashMap<>();
    // We assume the enemy knows how to manage positioning within his turn, and we don't want to recalc when we move units.
    PathCalcParams pcp = new PathCalcParams(threat, gameMap);
    pcp.includeOccupiedSpaces = true;
    pcp.canTravelThroughEnemies = ignoreFriendlyBlockers;
    pcp.findAllValidParents = true;
    ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths(); // Calculate eagerly since arty are rare, and tanks have two weapons
    // Throw in a check for all-immobile weapons here to remove the allpaths call??

    for( Utils.SearchNode dest : destinations )
    {
      for( WeaponModel wep : threat.model.weapons )
      {
        if( !wep.loaded(threat) )
          continue; // Ignore it if it can't shoot
        if( !wep.canFireAfterMoving() && !threat.coord.equals(dest) )
          continue; // Ignore it if it can't shoot

        UnitContext rangeContext = new UnitContext(threat);
        rangeContext.setPath(dest.getMyPath());
        rangeContext.setWeapon(wep);

        ArrayList<XYCoord> hittableTiles = Utils.findLocationsInRange(gameMap, dest, rangeContext);
        // We have our threatened tiles, now copy them to our cache
        for( XYCoord xyc : hittableTiles )
        {
          final TileThreat tt;
          if( shootableTiles.containsKey(xyc) )
            tt = shootableTiles.get(xyc);
          else
          {
            tt = new TileThreat();
            tt.identity = threat;
            shootableTiles.put(xyc, tt);
          }
          if( !tt.relevantWeapons.contains(wep) )
            tt.relevantWeapons.add(wep);
          tt.hitFrom.add(dest);
        }
      }
    }

    // Copy our cache into the real threat map
    for( XYCoord xyc : shootableTiles.keySet() )
      threatMap[xyc.x][xyc.y].add(shootableTiles.get(xyc));
  } // ~populateTileThreats

  // Try to get unit value by capture or attack
  public static class FreeRealEstate implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myArmy;
    public final WallyAI ai;

    public FreeRealEstate(Army army, WallyAI ai)
    {
      myArmy = army;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap map)
    {
      // Keep replanning the entire unit queue until we've planned on a board with all our planned kills.
      int lastClearAttacks = -1, clearAttacks = 0;
      for( ActionPlan plan : ai.plansByUnit.values() )
      {
        if( ai.predMap.helpsClearTile(plan) )
          ++clearAttacks;
      }
      while (lastClearAttacks < clearAttacks)
      {
        for( Unit unit : unitQueue )
        {
          planSeqDebuggably(map, unit);
        }
        lastClearAttacks = clearAttacks;
        clearAttacks = 0;
        for( ActionPlan plan : ai.plansByUnit.values() )
        {
          if( ai.predMap.helpsClearTile(plan) )
            ++clearAttacks;
        }
      }
      return null;
    }

    private void planSeqDebuggably(GameMap map, Unit unit)
    {
      EvictionContext ec = new EvictionContext(this, map, ai.mapPlan, EVICTION_DEPTH, unit);
      ArrayList<ActionPlan> actionSeq = ai.planValueActions(ec, unit);
      if( null == actionSeq )
        return;
      // ai.log(String.format("Planning actionSeq:\n%s", actionSeq));
      // ai.log(String.format("Postrequisites:\n%s", ec.postrequisites));
      for( ActionPlan plan : actionSeq )
      {
        ai.updatePlan(plan);
      }
    }
  } // ~FreeRealEstate

  private static final int EVICTION_DEPTH = 7;
  public static class Eviction extends UnitActionFinder<WallyAI>
  {
    private static final long serialVersionUID = 1L;
    public Eviction(Army co, WallyAI ai)
    {
      super(co, ai);
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap gameMap)
    {
      if( ai.plansByUnit.containsKey(unit) )
        return null;
      final UnitContext evicter = ai.mapPlan[unit.x][unit.y].identity;
      if( null == evicter )
        return null;

//      ai.log(String.format("Evaluating eviction for %s.", unit.toStringWithLocation()));
      EvictionContext ec = new EvictionContext(this, gameMap, ai.mapPlan, EVICTION_DEPTH, unit);
      boolean shouldYeet   = false;
      ArrayList<ActionPlan> travelPlans = ai.planValueActions(ec, unit, shouldYeet);

      shouldYeet = true;
      if( null == travelPlans )
        travelPlans = ai.planValueActions(ec, unit, shouldYeet);
      if( null == travelPlans )
        return null;
      for( ActionPlan plan : travelPlans )
      {
        ai.updatePlan(plan);
      }
      return null;
    }
  }

  public static class BuildStuff implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public final Army myArmy;
    public final WallyAI ai;

    public BuildStuff(Army co, WallyAI ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      Map<XYCoord, UnitModel> builds = ai.queueUnitProduction(gameMap);

      for( XYCoord coord : new ArrayList<XYCoord>(builds.keySet()) )
      {
        ai.log(String.format("Attempting to build %s at %s", builds.get(coord), coord));
        MapLocation loc = gameMap.getLocation(coord);
        Commander buyer = loc.getOwner();
        ArrayList<UnitModel> list = buyer.getShoppingList(loc);
        UnitModel toBuy = builds.get(coord);
        int buyCost = buyer.getBuyCost(toBuy, coord);
        if( buyCost <= myArmy.money && list.contains(toBuy) )
        {
          builds.remove(coord);
          GameAction buyAction = new GameAction.UnitProductionAction(buyer, toBuy, coord);
          Unit fakeUnit = new Unit(buyer, toBuy);
          fakeUnit.x = coord.x;
          fakeUnit.y = coord.y;
          fakeUnit.isTurnOver = false; // To pull a sneaky on FillActionQueue
          UnitContext fakeContext = new UnitContext(fakeUnit);
          fakeContext.path = GamePath.stayPut(fakeUnit);
          ai.updatePlan(this, 2500 + buyCost / 2, fakeContext, buyAction, false, 0);
        }
        else
        {
          ai.log(String.format("  Trying to build %s, but it's unavailable at %s", toBuy, coord));
          continue;
        }
      }

      return null;
    }
  }

  private int prevTravelPrio(XYCoord xyc)
  {
    return travelPlanCoords.getOrDefault(xyc, TravelPurpose.WANDER).priority;
  }

  /** Produces a list of destinations for the unit, ordered by their relative precedence */
  private TravelPurpose fillTravelDestinations(
                                  GameMap gameMap,
                                  ArrayList<XYCoord> goals,
                                  Unit unit )
  {
    UnitContext uc = new UnitContext(gameMap, unit);
    uc.calculateActionTypes();
    TravelPurpose travelPurpose = TravelPurpose.WANDER;

    ArrayList<XYCoord> stations = AIUtils.findRepairDepots(unit);
    // Clean out any stations we already have plans for.
    for( int i = 0; i < stations.size(); )
    {
      XYCoord xyc = stations.get(i);
      if( prevTravelPrio(xyc) >= TravelPurpose.SUPPLIES.priority )
        stations.remove(xyc);
      if( null != predMap.getResident(xyc) )
        stations.remove(xyc);
      else
        ++i;
    }
    Utils.sortLocationsByTravelTime(unit, stations, predMap);

    boolean shouldResupply = false;
    GamePath toClosestStation = null;
    boolean canResupply = stations.size() > 0;
    if( canResupply )
    {
      toClosestStation = new PathCalcParams(unit, predMap).setTheoretical().findShortestPath(stations.get(0));
      canResupply &= null != toClosestStation;
    }
    if( canResupply )
    {
      shouldResupply = unit.getHealth() <= UNIT_HEAL_THRESHOLD;
      shouldResupply |= unit.fuel <= UNIT_REFUEL_THRESHOLD
          * toClosestStation.getFuelCost(unit, predMap);
      shouldResupply |= unit.ammo >= 0 && unit.ammo <= unit.model.maxAmmo * UNIT_REARM_THRESHOLD;
    }

    if( shouldResupply )
    {
      log(String.format("  %s needs supplies.", unit.toStringWithLocation()));
      goals.addAll(stations);
      goals.removeAll(AIUtils.findAlliedIndustries(predMap, myArmy, goals, true));
      if( !goals.isEmpty() )
        travelPurpose = TravelPurpose.SUPPLIES;
    }
    int distThreshold = uc.calculateMovePower() * UNIT_CAPTURE_RANGE;
    if( goals.isEmpty() && uc.model.isAny(UnitModel.CAPTURE) )
    {
      for( XYCoord xyc : futureCapTargets )
      {
        if( prevTravelPrio(xyc) >= TravelPurpose.CONQUER.priority )
          continue;
        // predMap shouldn't meaningfully diverge from reality here, I think
        boolean validCapDest = !AIUtils.isCapturing(gameMap, myArmy.cos[0], xyc);
        // If the turf is taken by someone else, it's a KILL objective
        validCapDest &= null == predMap.getResident(xyc) || myArmy == gameMap.getResident(xyc).CO.army;
        validCapDest &= xyc.getDistance(unit) <= distThreshold;
        if( validCapDest )
          goals.add(xyc);
      }
      if( !goals.isEmpty() )
        travelPurpose = TravelPurpose.CONQUER;
      Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), goals);
    }
    if( goals.isEmpty() && uc.actionTypes.contains(UnitActionFactory.ATTACK) )
    {
      goals.addAll(findCombatUnitDestinations(gameMap, allThreats, unit));
      if( !goals.isEmpty() )
        if( unit.model.hasImmobileWeapon() )
          travelPurpose = TravelPurpose.BESIEGE; // Siege units have the highest travel priority since they're most likely to have to waste turns.
        else
          travelPurpose = TravelPurpose.KILL;
    }

    if( goals.isEmpty() ) // If there's really nothing to do, go to MY HQ
      goals.addAll(myArmy.HQLocations);

    return travelPurpose;
  }

  private ArrayList<XYCoord> findCombatUnitDestinations(GameMap gameMap, ArrayList<Unit> allThreats, Unit unit)
  {
    return findCombatUnitDestinations(gameMap, allThreats, new XYCoord(unit), new ModelForCO(unit), null);
  }
  private ArrayList<XYCoord> findCombatUnitDestinations(GameMap gameMap, ArrayList<Unit> allThreats, XYCoord start, ModelForCO um, UnitModel targetType)
  {
    ArrayList<XYCoord> goals = new ArrayList<>();
    Map<XYCoord, Integer> valueMap = new HashMap<>();
    UnitContext uc = new UnitContext(um.co, um.um);
    uc.coord = start;

    // Categorize all enemies by type, and all types by how well we match up vs them
    for( Unit target : allThreats )
    {
      XYCoord targetCoord = new XYCoord(target.x, target.y);
      if( !gameMap.isLocationValid(targetCoord) )
        continue; // We don't care about shooting dead people

      UnitModel model = target.model;
      if( null != targetType && model != targetType )
        continue;
      final ModelForCO modelKey = new ModelForCO(target);
      int range = 1;
      for( ; range < 5; range++ )
      {
        uc.chooseWeapon(model, range);
        if( null != uc.weapon )
          break;
      }
      if( null == uc.weapon )
        continue; // No point in calculating further if we can't hit the target

      double effectiveness = findEffectiveness(um, modelKey);
      GamePath path = new PathCalcParams(uc, predMap).setTheoretical().findShortestPath(targetCoord);
      if (null != path &&
          AGGRO_EFFECT_THRESHOLD < effectiveness)
      {
        int distance = path.getMoveCost(um.co, um.um, gameMap);
        int distanceTurns = distance / uc.calculateMovePower() + 1; // I sure do love division by 0
        int targetFunds = WallyAI.valueUnit(gameMap, target, true);
        valueMap.put( targetCoord, (int)(effectiveness*targetFunds)/distanceTurns );
      }
    }

    if( !valueMap.isEmpty() )
    {
      // Sort all targets by how much we want to shoot them with this unit
      Queue<Entry<XYCoord, Integer>> targetTypesInOrder =
          new PriorityQueue<>(valueMap.size(), new EntryValueComparator<>());
      targetTypesInOrder.addAll(valueMap.entrySet());

      while (!targetTypesInOrder.isEmpty())
      {
        XYCoord coord = targetTypesInOrder.poll().getKey(); // peel off the juiciest
        goals.add(coord);
      }
    }

    if( goals.isEmpty() ) // Send 'em at production facilities if there's nothing to shoot
    {
      for( XYCoord coord : futureCapTargets )
      {
        MapLocation loc = gameMap.getLocation(coord);
        PathCalcParams pcp = new PathCalcParams(uc, gameMap).setTheoretical();
        if( um.co.unitProductionByTerrain.containsKey(loc.getEnvironment().terrainType)
            && myArmy.isEnemy(loc.getOwner())
            && null != pcp.findShortestPath(coord) )
        {
          goals.add(coord);
        }
      }
    }

    return goals;
  }

  private static class EvictionContext
  {
    AIModule whodunit;
    GameMap map;
    @SuppressWarnings("unused")
    UnitPrediction[][] mapPlan;
    int maxEvictionDepth;
    private Stack<Unit> evictionStack;
    private Stack<ActionPlan> postrequisites;
    public EvictionContext(AIModule whodunit, GameMap gameMap, UnitPrediction[][] mapPlan, int evictionDepth, Unit toMove)
    {
      this.whodunit = whodunit;
      this.map      = gameMap;
      this.mapPlan  = mapPlan;
      maxEvictionDepth = evictionDepth;
      this.evictionStack = new Stack<>();
      postrequisites = new Stack<>();
      ActionPlan evictingPlan = mapPlan[toMove.x][toMove.y].toAchieve;
      // If the action is my own action, don't ban me from considering it again
      if( null != evictingPlan && toMove != evictingPlan.actor.unit )
        enqueuePostReqPlans(mapPlan, evictingPlan, postrequisites);
    }
    public void push(Unit unit, ActionPlan plan)
    {
      evictionStack.push(unit);
      postrequisites.push(plan);
    }
    public void pop()
    {
      evictionStack.pop();
      postrequisites.pop();
    }
    public int getEvictionValue()
    {
      int value = 0;
      for( ActionPlan plan : postrequisites )
      {
        value += plan.score;
      }
      return value;
    }
    public int remainingDepth()
    {
      return maxEvictionDepth - evictionStack.size();
    }
    /** Find the tiles a unit on startTile shouldn't go, based on any planned actions that need that tile to be open. */
    public ArrayList<XYCoord> calcPostReqMoveBans()
    {
      var bannedTiles = new ArrayList<XYCoord>();

      for( ActionPlan plan : postrequisites )
      {
        var tile = new XYCoord(plan.actor.unit);
        if( tile != null ) bannedTiles.add(tile);
        tile = plan.action.getMoveLocation();
        if( tile != null ) bannedTiles.add(tile);
        // If this is a WAIT other than the first action, ban any tiles that that WAIT could have gone to - this saves compute and unit-turns.
        if( plan != postrequisites.get(0) && plan.action.getType() == UnitActionFactory.WAIT )
        {
          PathCalcParams pcp = new PathCalcParams(plan.actor.unit, map);
          ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths();
          bannedTiles.addAll(destinations);
        }
        // Will need to consider UNLOAD here at some point?
      }

      return bannedTiles;
    }
  }

  /**
   * Find an action that moves toward a long-term objective (with consideration for life-preservation optional)
   * <p>Defaults to allowing evictions of other travelers.
   */
  ArrayList<ActionPlan> planValueActions(
                        EvictionContext ec, Unit unit)
  {
    return planValueActions(ec, unit, false);
  }
  ArrayList<ActionPlan> planValueActions(
                        EvictionContext ec, Unit unit,
                        boolean shouldYeet)
  {
    if( ec.evictionStack.contains(unit) )
      return null;
    GameMap gameMap = ec.map;
    var bannedTiles = ec.calcPostReqMoveBans();
    XYCoord startTile = new XYCoord(unit);
    final boolean mustMove = bannedTiles.contains(startTile);
    ActionPlan myOldPlan = plansByUnit.get(unit);

    boolean ignoreResident = true;
    // Find the possible destinations.
    PathCalcParams pcp = new PathCalcParams(unit, predMap);
    pcp.includeOccupiedSpaces = ignoreResident;
    ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths();
    destinations.removeAll(AIUtils.findAlliedIndustries(ec.map, myArmy, destinations, true));

    // TODO: Jump in a transport, if available, or join?

    XYCoord goal = null;
    GamePath path = null;
    ArrayList<XYCoord> validTargets = new ArrayList<XYCoord>();
    TravelPurpose travelPurpose = fillTravelDestinations(predMap, validTargets, unit);

    destinations.removeAll(bannedTiles);
    validTargets.removeAll(bannedTiles);

    for( XYCoord target : validTargets )
    {
      path = new PathCalcParams(unit, predMap).setTheoretical().findShortestPath(target);
      if( path != null ) // We can reach it.
      {
        goal = target;
        break;
      }
    }
    // If we have no destinations, consider ourselves wandering
    if( null == goal )
    {
      goal = startTile;
      path = GamePath.stayPut(unit);
      travelPurpose = TravelPurpose.WANDER;
    }

    // Choose the point on the path just out of our range as our 'goal', and try to move there.
    // This will allow us to navigate around large obstacles that require us to move away
    // from our intended long-term goal.
    int unitMove = unit.getMovePower(predMap);
    path.snip(unitMove + 2); // Trim the path approximately down to size.
    XYCoord pathPoint = path.getEndCoord(); // Set the last location as our goal.

    // Sort my currently-reachable move locations by distance from the goal,
    // and build a GameAction to move to the closest one.
    Utils.sortLocationsByDistance(pathPoint, destinations);

//    log(String.format("  %s is traveling toward %s at %s via %s  mustMove?: %s  evictionValue?: %s",
//                          unit.toStringWithLocation(),
//                          ec.map.getLocation(goal).getEnvironment().terrainType, goal,
//                          pathPoint, mustMove, evictionValue));

    Queue<Entry<ActionPlan, Integer>> rankedTravelPlans =
        new PriorityQueue<>(13, new EntryValueComparator<>());

    int minFundsDelta = Math.min(0, -1 * ec.getEvictionValue());
    if( myOldPlan != null )
      minFundsDelta += myOldPlan.score;
    for( Utils.SearchNode xyc : destinations )
    {
//      log(String.format("    is it safe to go to %s?", xyc));
//    log(String.format("    Yes"));

      Unit plannedResident = predMap.getResident(xyc); // Must call predMap so that residents we plan to murder don't show up.
      ActionPlan  resiPlan = mapPlan[xyc.x][xyc.y].toAchieve;
      boolean spaceFree = null == plannedResident;
      // Bail if:
      // There's no other action to evaluate against ours
      // Not allowed to evict

      Unit currentResident = ec.map.getResident(xyc);
      if( ec.evictionStack.contains(currentResident) )
        continue;
      boolean currentResidentHasPlans = plansByUnit.containsKey(currentResident);
      if( !currentResidentHasPlans )
        // If whatever's in our landing pad has no plans yet, poke and see if some can be made
        if( null != currentResident && currentResident.CO.army == myArmy )
        {
          if( ec.evictionStack.contains(currentResident) )
            continue;
          boolean residentIsEvictable = !currentResident.isTurnOver;

          if( !residentIsEvictable || ec.remainingDepth() <= 0 )
            continue;
        } // ~if resident

      GamePath movePath = xyc.getMyPath();
      HashSet<ActionPlan> foundPrereqs = new HashSet<>();
      if( preReqConflictExists(ec.map, unit, movePath, ec.postrequisites, foundPrereqs) )
        continue;

      int bonusPoints = 0;
      if( xyc.equals(goal) )
      {
        bonusPoints += 9;
        if( travelPurpose == TravelPurpose.SUPPLIES )
          bonusPoints = unit.CO.getCost(unit.model);
      }
      UnitContext actor = new UnitContext(gameMap, unit); // Define a UC per coord, so the final plan's UC has the right coordinate on it
      actor.setPath(movePath);
      ArrayList<GameActionSet> actionSets = unit.getPossibleActions(ec.map, movePath, ignoreResident);
      // Since we're moving anyway, might as well try shooting the scenery
      for( GameActionSet actionSet : actionSets )
      {
        final UnitActionFactory actionType = actionSet.getSelected().getType();
        if( actionType == UnitActionFactory.ATTACK )
        {
          for( GameAction attack : actionSet.getGameActions() )
          {
            XYCoord targetXYC = attack.getTargetLocation();
            boolean targetPrereqConflict = false;
            for( var prereqPlan : foundPrereqs )
              if( targetXYC.equals(prereqPlan.action.getMoveLocation()) )
              {
                // We are shooting a tile that was used to clear a tile we moved into/through; that's a prereq conflict.
                targetPrereqConflict = true;
                break;
              }
            if( targetPrereqConflict )
              continue;

            final AttackValue results;
            Unit targetUnit   = ec.map.getResident(targetXYC);
            if( null != targetUnit )
              results = new AttackValue(this, actor, new UnitContext(targetUnit), predMap, shouldYeet);
            else
              results = new AttackValue(this, actor, targetXYC                  , predMap, shouldYeet);
            final int fundsDelta = results.fundsDelta;

            final int thisDelta = (int) fundsDelta + bonusPoints;
            if( thisDelta > minFundsDelta )
            {
//              log(String.format("      Best en passant attack deals %s", fundsDelta));
              ActionPlan plan = new ActionPlan(ec.whodunit, actor, attack, thisDelta);
              plan.path = movePath;
              plan.goal = goal;
              plan.purpose = TravelPurpose.KILL;
              plan.isAttack = true;
              plan.percentDamage = (int) (results.hpDamage * 10);
              rankedTravelPlans.add(new AbstractMap.SimpleEntry<>(plan, thisDelta));
            }
          }
        }

        if( actionType == UnitActionFactory.CAPTURE )
        {
          for( GameAction capture : actionSet.getGameActions() )
          {
            final int thisDelta = valueCapture((CaptureAction) capture, gameMap);

            int wallPoints = 0;
            if( !shouldYeet )
              wallPoints = wallFundsValue(predMap, threatMap, unit, xyc, null);
            final int finalCapValue = thisDelta + wallPoints;
            if( finalCapValue <= minFundsDelta )
              continue;

            ActionPlan plan = new ActionPlan(ec.whodunit, actor, capture, finalCapValue);
            plan.path = movePath;
            plan.goal = goal;
            plan.purpose = TravelPurpose.CONQUER;
            plan.isAttack = false;
            rankedTravelPlans.add(new AbstractMap.SimpleEntry<>(plan, finalCapValue));
          }
        }

        if( actionType == UnitActionFactory.WAIT )
        {
          if( !mustMove && travelPurpose == TravelPurpose.WANDER )
            continue; // Don't clutter the queue with pointless movements
          if( !spaceFree && myOldPlan != resiPlan &&
              ( null == resiPlan || resiPlan.purpose.priority > travelPurpose.priority) )
            continue;
          for( GameAction move : actionSet.getGameActions() )
          {
            int startDist = startTile.getDistance(pathPoint);
            int endDist   = movePath.getEndCoord().getDistance(pathPoint);
            int walkGain = startDist - endDist;
            int wallPoints = 0;
            int healBonus  = 0;
            if( !shouldYeet )
              wallPoints = wallFundsValue(predMap, threatMap, unit, xyc, null);
            var loc = gameMap.getLocation(movePath.getEndCoord());
            if( unit.isHurt() &&
                !myArmy.isEnemy(loc.getOwner()) &&
                unit.model.healableHabs.contains(loc.getEnvironment().terrainType)
              )
              healBonus = unit.CO.getRepairPower() * unit.CO.getCost(unit.model) / 10;
            int waitScore = movePath.getPathLength() + wallPoints + walkGain + bonusPoints + healBonus;
            if( minFundsDelta < waitScore && movePath.getPathLength() > 1 ) // Just wait if we can't do anything cool
            {
              ActionPlan plan = new ActionPlan(ec.whodunit, actor, move, waitScore);
              plan.path = movePath;
              plan.goal = goal;
              plan.purpose = travelPurpose;
              rankedTravelPlans.add(new AbstractMap.SimpleEntry<>(plan, waitScore));
            }
          }
        }
      } // ~for action types
    } // ~for destinations

    ArrayList<ActionPlan> bestPlans = calcEvictionPlans(ec, unit, shouldYeet, rankedTravelPlans);

    return bestPlans;
  }

  private ArrayList<ActionPlan> calcEvictionPlans(EvictionContext ec,
                                    Unit unit, boolean shouldYeet,
                                    Queue<Entry<ActionPlan, Integer>> rankedTravelPlans)
  {
    ArrayList<ActionPlan> bestPlans = null;
    // Now that we have an ordered list of our travel locations, figure out the best one we can accomplish (potentially requiring eviction of both current and planned residents)
    var travelPlans = new ArrayList<>(rankedTravelPlans);
    for( int i = 0; i < travelPlans.size(); ++i )
    {
      ActionPlan evictingPlan = travelPlans.get(i).getKey();
      XYCoord xyc = evictingPlan.action.getMoveLocation();
      ArrayList<ActionPlan> prereqPlans = new ArrayList<>();

      ArrayList<Unit> evictees = new ArrayList<>();
      ArrayList<ActionPlan> evicteePlans = new ArrayList<>();

      // If there's a current resident with no plans, make up a "wait" in place as the action that corresponds to this evictee
      Unit currentResident = ec.map.getResident(xyc);
      boolean currentResidentHasPlans = plansByUnit.containsKey(currentResident);
      if( !currentResidentHasPlans // This case is handled by the next conditional
          && unit != currentResident && null != currentResident // Resident exists and isn't me
          && !unit.CO.isEnemy(currentResident.CO) ) // If the unit is an enemy, we assume we will have planned to "evict" it otherwise before planning this
      {
        if( currentResident.isTurnOver )
          continue;
        evictees.add(currentResident);
        GamePath path = GamePath.stayPut(currentResident);
        GameAction stayPut = new WaitLifecycle.WaitAction(currentResident, path);
        int stayPutScore = wallFundsValue(ec.map, threatMap, currentResident, xyc, null);
        // Penalize evictions so spurious ones happen less
        stayPutScore -= valueUnit(ec.map, currentResident, true);

        UnitContext resiContext = new UnitContext(currentResident);
        resiContext.path = path;
        evicteePlans.add(new ActionPlan(ec.whodunit, resiContext, stayPut, stayPutScore));
      }

      // Handle any unit that has planned to take my spot
      ActionPlan  evictablePlan = mapPlan[xyc.x][xyc.y].toAchieve;
      if( null != evictablePlan )
      {
        Unit futureResident = evictablePlan.actor.unit;
        if( unit == futureResident )
          return null; // Trying to evict yourself is dumb
        evictees.add(futureResident);
        evicteePlans.add(evictablePlan);
      }

      boolean evictionFailure = false;
      for( Unit ev : evictees )
      {
        evictionFailure |= ( ec.evictionStack.contains(ev) );
        evictionFailure |= ( ev.isTurnOver && ev.CO.army == myArmy );
        if( !evictionFailure )
        {
          PathCalcParams pcp = new PathCalcParams(ev, predMap);
          pcp.includeOccupiedSpaces = false;
          evictionFailure |= (pcp.findAllPaths().size() < 2); // Can't move anywhere that isn't already occupied
        }
      }
      if( evictionFailure )
        continue;

      // Prevent reflexive eviction
      ec.push(unit, evictingPlan);
      for( int evicteeIndex = 0; evicteeIndex < evictees.size(); ++evicteeIndex )
      {
        Unit ev = evictees.get(evicteeIndex);
        ActionPlan evicteeOldPlan = evicteePlans.get(evicteeIndex);

        ArrayList<ActionPlan> evictionPlans = null;
        evictionPlans = calcEvictedActions(ec, ev, shouldYeet);

        evictionFailure |= null == evictionPlans;
        if( evictionFailure )
          break;

        ActionPlan evicteeNewPlan = evictionPlans.get(0);
        int opportunityCost = evicteeOldPlan.score - evicteeNewPlan.score;
        evictionFailure |= evictingPlan.score < opportunityCost;
        if( evictionFailure )
          break;

        prereqPlans.addAll(evictionPlans);
      } // ~for evictees
      ec.pop();

      if( evictionFailure )
        continue;
      bestPlans = new ArrayList<>();
      bestPlans.add(evictingPlan); // Since this cancels resiPlan implicitly, resiPlan's replacement needs to be cached after this one is
      bestPlans.addAll(prereqPlans);

      break; // We found a workable one. Ship it
    }

    return bestPlans;
  }

  private ArrayList<ActionPlan> calcEvictedActions(
                                    EvictionContext ec, Unit evictee,
                                    boolean shouldYeet)
  {
    boolean evictionFailure = ec.evictionStack.contains(evictee);
    if( evictionFailure )
      return null;
    boolean residentIsEvictable = !evictee.isTurnOver;

    evictionFailure |= !residentIsEvictable || ec.remainingDepth() <= 0;
    if( evictionFailure )
      return null;
    // If nobody's there, no need to evict.
    // If the resident is evictable, try to evict and bail if we can't.
    // If the resident isn't evictable, we think it will be dead soon, so just keep going.

    ArrayList<ActionPlan> evictionPlans;
    evictionPlans = planValueActions(ec, evictee);

    return evictionPlans;
  }

  /**
   * @return The maximum expected health loss
   */
  private int fundsThreatAt(GameMap gameMap, ArrayList<TileThreat>[][] threatMap, Unit unit, XYCoord xyc, Unit target)
  {
    return fundsThreatAt(gameMap, threatMap, new UnitContext(unit), xyc, target);
  }
  private int fundsThreatAt(GameMap gameMap, ArrayList<TileThreat>[][] threatMap, ModelForCO type, XYCoord xyc, Unit target)
  {
    UnitContext myUnit = new UnitContext(type.co, type.um);
    myUnit.coord = xyc;
    myUnit.unit = new Unit(type.co, type.um); // Make stuff up so combat modifiers don't explode?
    myUnit.unit.x = xyc.x;
    myUnit.unit.y = xyc.y;
    return fundsThreatAt(gameMap, threatMap, myUnit, xyc, target);
  }
  private int fundsThreatAt(GameMap gameMap, ArrayList<TileThreat>[][] threatMap, UnitContext myUnit, XYCoord xyc, Unit target)
  {
    int threat = 0;
    var realThreats = realThreatsAt(gameMap, threatMap, myUnit, xyc, target);
    for( TileThreat tt : realThreats.keySet() )
    {
      AttackValue valuator = new AttackValue(this, realThreats.get(tt), gameMap, true);
      threat = Math.max(threat, valuator.fundsDelta);
    }

    return threat;
  }
  /**
   * @return a map from threats to their combat results
   */
  private HashMap<TileThreat, BattleSummary> realThreatsAt(GameMap gameMap, ArrayList<TileThreat>[][] threatMap, UnitContext myUnit, XYCoord xyc, Unit target)
  {
    HashMap<TileThreat, BattleSummary> output = new HashMap<>();
    ArrayList<TileThreat> tileThreats = threatMap[xyc.x][xyc.y];
    for( TileThreat tt : tileThreats )
    {
      if( target == tt.identity.unit )
        continue; // We aren't scared of that which we're about to shoot
      if( tt.identity.unit.getHP() < 1 )
        continue; // Dead people can't shoot

      boolean viablePaths = canReachHitFromZone(predMap, tt);
      if( !viablePaths )
        continue; // He can't hit us if he's blocked

      // Scratch struct so we can mess with it
      final UnitContext threatContext = new UnitContext(tt.identity);

      // Collect planned damage so far
      int damagePercent = 0;
      final HashMap<ActionPlan, Integer> damageInstances = mapPlan[threatContext.coord.x][threatContext.coord.y].damageInstances;
      for( int hit : damageInstances.values() )
        damagePercent += hit;

      // Apply that damage knowledge
      if( threatContext.getHP() * 10 <= damagePercent )
        continue; // Dead people don't usually shoot
      threatContext.alterHealthNoRound(-1 * damagePercent);

      BattleSummary bestHit = null;
      for( WeaponModel wep : tt.relevantWeapons )
      {
        threatContext.setWeapon(wep);

        BattleSummary results = CombatEngine.simulateBattleResults(threatContext, myUnit, gameMap, CalcType.OPTIMISTIC);

        if( null == bestHit ||
            bestHit.defender.getPreciseHealthDamage() < results.defender.getPreciseHealthDamage() )
          bestHit = results;
      }

      if( null != bestHit )
        output.put(tt, bestHit);
    }

    return output;
  }

  private static boolean canReachHitFromZone(PredictionMap predMap, TileThreat tt)
  {
    PathCalcParams pcp = new PathCalcParams(tt.identity, predMap);
    return canReachHitFromZone(predMap, pcp, tt.hitFrom);
  }
  private static boolean canReachHitFromZone(PredictionMap predMap, PathCalcParams pcp, Collection<SearchNode> roots)
  {
    for( SearchNode hitRoot : roots )
    {
      var node = hitRoot;
      if( null == node.parent )
        return true; // If we're the start of the path, GG
      int costTotal = pcp.mt.getTransitionCost(predMap, node.parent, node, pcp.team, pcp.canTravelThroughEnemies);
      if( costTotal > pcp.initialMovePower )
        continue; // If the hit tile is blocked, GG

      // Iterate through the nominal path, to see if it's blocked.
      while (true)
      {
        if( null == node.parent.parent )
          return true;
        final int toReachParent = pcp.mt.getTransitionCost(predMap, node.parent.parent, node.parent, pcp.team, pcp.canTravelThroughEnemies);
        if( costTotal + toReachParent > pcp.initialMovePower ) // Nominal path is impassible
        {
          final int startMP = pcp.initialMovePower;
          pcp.initialMovePower = startMP - costTotal; // Consider only the remaining movepower
          if( canReachHitFromZone(predMap, pcp, node.allParents) )
            return true;
          pcp.initialMovePower = startMP;
          break;
        }
        costTotal += toReachParent;
        node = node.parent;
      }
    }
    return false;
  }

  /**
   * @return expected funds gain of hanging out here (can be negative)
   * <p>For use after unit building is fully planned
   */
  private int wallFundsValue(GameMap gameMap, ArrayList<TileThreat>[][] threatMap, Unit unit, XYCoord xyc, Unit target)
  {
    int wallLoss = fundsThreatAt(gameMap, threatMap, unit, xyc, target);
    int wallGain = 0;

    ArrayList<XYCoord> adjacentCoords = Utils.findLocationsInRange(predMap, xyc, 1);
    for( XYCoord coord : adjacentCoords )
    {
      MapLocation loc = gameMap.getLocation(coord);
      if( loc != null )
      {
        Unit friend = loc.getResident();
        if( friend != null && !myArmy.isEnemy(friend.CO) )
        {
          var friendHits = realThreatsAt(gameMap, threatMap, new UnitContext(unit), xyc, target);
          for( TileThreat tt : friendHits.keySet())
          {
            // TODO: Determine whether other units can kill me to let this threat through?
            boolean blockByMe = false;
            for( SearchNode hitRoot : tt.hitFrom )
            {
              if( hitRoot.equals(xyc) )
              {
                blockByMe = true;
                break;
              }
            }
            if( !blockByMe )
              continue; // We aren't blocking this damage, so we don't get credit

            AttackValue valuator = new AttackValue(this, friendHits.get(tt), gameMap, true);
            wallGain += valuator.fundsDelta;
          }
        }
      }
    }

    return wallGain - wallLoss;
  }

  private static int valueUnit(GameMap map, Unit unit, boolean includeCurrentHealth)
  {
    return valueUnit(unit, map.getLocation(unit.x, unit.y), includeCurrentHealth);
  }
  private static int valueUnit(Unit unit, MapLocation locale, boolean includeCurrentHealth)
  {
    int value = unit.getCost();

    if( unit.CO.isEnemy(locale.getOwner()) &&
            unit.hasActionType(UnitActionFactory.CAPTURE)
            && locale.isCaptureable() )
      value += valueTerrain(unit.CO, locale.getEnvironment().terrainType); // Strongly value units that threaten capture

    value -= locale.getEnvironment().terrainType.getDefLevel(); // Value things on lower terrain more, so we wall for equal units if we can get on better terrain
    if( includeCurrentHealth )
    {
      value *= unit.getHealth();
      value /= UnitModel.MAXIMUM_HEALTH;
    }

    return value;
  }

  private static int valueTerrain(Commander co, TerrainType terrain)
  {
    int value = 0;
    if( terrain.isProfitable() )
      value += co.gameRules.incomePerCity * TERRAIN_FUNDS_WEIGHT;
    if( co.unitProductionByTerrain.containsKey(terrain) )
      value += TERRAIN_INDUSTRY_WEIGHT;
    if( TerrainType.HEADQUARTERS == terrain
        || TerrainType.LAB == terrain )
      value += TERRAIN_HQ_WEIGHT;
    return value;
  }

  public static class CombatBuildDeets
  {
    public CombatBuildDeets(WallyAI ai, MapLocation loc, ArrayList<XYCoord> targets, ModelForCO mfc, ModelForCO targetType)
    {
      this.loc = loc;
      this.targets = targets;
      this.mfc = mfc;
      enemyToCounter = targetType;
      XYCoord coord = loc.getCoordinates();
      price = mfc.co.getBuyCost(mfc.um, coord);

      UnitContext uc = new UnitContext(mfc.co, mfc.um);
      uc.setCoord(coord);
      double effectiveness = 1;
      double costRatio     = 1;
      if( null != enemyToCounter )
      {
        double enemyPrice = enemyToCounter.co.getCost(enemyToCounter.um);
        effectiveness = ai.findEffectiveness(mfc, enemyToCounter);
        costRatio = enemyPrice / price;
      }
      int minDist          = targets.get(0).getDistance(coord);
      int turnAdjustment   = (mfc.um.isAny(UnitModel.SIEGE))? 2 : 1;
      int minTurns         = minDist / uc.calculateMovePower() + turnAdjustment;
      double turnRatio     = 10.0 / minTurns;
      score = (int) (1000 * effectiveness * costRatio * turnRatio);
    }
    MapLocation loc;
    ArrayList<XYCoord> targets;
    ModelForCO mfc, enemyToCounter;
    int price, score;
    @Override
    public String toString()
    {
      return String.format("(%s at %s)", mfc, loc.getCoordinates());
    }
  }
  private static class CombatBuldDeetsComparator implements Comparator<CombatBuildDeets>
  {
    @Override
    public int compare(CombatBuildDeets build1, CombatBuildDeets build2)
    {
      int diff = build2.score - build1.score;
      return diff;
    }
  }

  /**
   * Returns all acceptable places to build the unit type
   */
  public ArrayList<CombatBuildDeets> getBuildOptionsFor(GameMap gameMap, CommanderProductionInfo CPI, UnitModel model, ModelForCO target)
  {
    ArrayList<CombatBuildDeets> candidates = new ArrayList<>();

    for( MapLocation loc : CPI.getAllFacilitiesFor(model) )
    {
      final ModelForCO mfc = new ModelForCO(loc.getOwner(), model);

      int threat = fundsThreatAt(gameMap, threatMap, mfc, loc.getCoordinates(), null);
      int threatThreshold = (BUILD_SCARE_PERCENT * mfc.co.getBuyCost(mfc.um, loc.getCoordinates())) / 100;
      if( threatThreshold < threat )
        continue;
      // If we can get to a target...
      ArrayList<XYCoord> potentialVictims = findCombatUnitDestinations(predMap, allThreats, loc.getCoordinates(), mfc, (null == target)? null : target.um);
      if( 0 < potentialVictims.size() )
        candidates.add(new CombatBuildDeets(this, loc, potentialVictims, mfc, target));
    }

    return candidates;
  }

  private Map<XYCoord, UnitModel> queueUnitProduction(GameMap gameMap)
  {
    Map<XYCoord, UnitModel> builds = new HashMap<XYCoord, UnitModel>();
    // Figure out what unit types we can purchase with our available properties.
    boolean includeFriendlyOccupied = true;
    CommanderProductionInfo CPI = new CommanderProductionInfo(myArmy, gameMap, includeFriendlyOccupied);

    HashSet<MapLocation> blockedByActions = new HashSet<>();
    for( MapLocation prop : CPI.availableProperties )
    {
      XYCoord coord = prop.getCoordinates();
      Unit resident = predMap.getResident(coord);
      if( null != resident )
      {
        if( !resident.isTurnOver )
          log(String.format("  Can't evict %s to build at %s", resident, coord));
        blockedByActions.add(prop);
      }
    }
    CPI.availableProperties.removeAll(blockedByActions);
    if( CPI.availableProperties.isEmpty() )
    {
      log("No properties available to build.");
      return builds;
    }

    log("Evaluating Production needs");
    int budget = myArmy.money;
    var capperPrices = new HashMap<XYCoord, Integer>();
    var capperTypes = myArmy.cos[0].getAllModels(UnitModel.CAPTURE);

    // Set up inf builds on all my props
    for( var ct : capperTypes )
    {
      var capFacilities = CPI.getAllFacilitiesFor(ct);
      for( var ctLoc : capFacilities )
      {
        XYCoord ctxyc = ctLoc.getCoordinates();
        if( builds.containsKey(ctxyc) )
          continue; // only build the cheapest thing
        Commander buyer = ctLoc.getOwner();
        int cost = buyer.getBuyCost(ct, ctxyc);
        if( cost > budget )
          return builds;
        builds.put(ctxyc, ct);
        budget -= cost;
        capperPrices.put(ctxyc, cost);
      }
    }
    // Purge any "extra" unit types we didn't actually build for captures
    capperTypes.clear();
    capperTypes.addAll(builds.values());

    // Get a count of enemy forces.
    Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myArmy, predMap);
    Map<ModelForCO, Integer> enemyUnitHP = new HashMap<>();
    for( Commander co : unitLists.keySet() )
    {
      if( myArmy.isEnemy(co) )
      {
        for( Unit u : unitLists.get(co) )
        {
          // Count how many of each model of enemy units are in play.
          ModelForCO key = new ModelForCO(u);
          if( enemyUnitHP.containsKey(key) )
          {
            enemyUnitHP.put(key, enemyUnitHP.get(key) + u.getHP());
          }
          else
          {
            enemyUnitHP.put(key, u.getHP());
          }
        }
      }
    }

    // Figure out how well we think we have the existing threats covered
    Map<ModelForCO, Integer> myUnitHP = new HashMap<>();
    for( Unit u : myArmy.getUnits() )
    {
      // Count how many of each model of enemy units are in play.
      ModelForCO key = new ModelForCO(u);
      if( myUnitHP.containsKey(key) )
      {
        myUnitHP.put(key, myUnitHP.get(key) + u.getHP());
      }
      else
      {
        myUnitHP.put(key, u.getHP());
      }
    }

    for( ModelForCO threat : enemyUnitHP.keySet() )
    {
      for( ModelForCO counter : myUnitHP.keySet() ) // Subtract how well we think we counter each enemy from their HP counts
      {
        double counterPower = findEffectiveness(counter, threat);
        if( counterPower < 0.1 )
          continue;
        counterPower *= COUNTER_EFFICIENCY_FACTOR;
        enemyUnitHP.put( threat, (int) (enemyUnitHP.get(threat) - counterPower * myUnitHP.get(counter)) );
      }
    }

    Queue<Entry<ModelForCO, Integer>> enemyModels =
        new PriorityQueue<>(myArmy.cos[0].unitModels.size(), new EntryValueComparator<>());

    // Fill the queue of enemy models, changing HP->funds
    for( Entry<ModelForCO, Integer> ent : enemyUnitHP.entrySet() )
    {
      ModelForCO tmco = ent.getKey();
      var fundsEntry = new AbstractMap.SimpleEntry<ModelForCO, Integer>(tmco, ent.getValue() * tmco.co.getCost(tmco.um) / 10);
      enemyModels.add(fundsEntry);
    }

    // Try to purchase units that will counter the most-represented enemies.
    while (!enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())
    {
      Entry<ModelForCO, Integer> enemyEntry = enemyModels.poll();
      ModelForCO enemyToCounter = enemyEntry.getKey();
      int enemyHP = enemyUnitHP.get(enemyToCounter);
      if( enemyHP < 0 )
        break; // Nothing to counter; stop counterbuilding
      log(String.format("Need a counter for %s HP of %s (have %s)", enemyHP, enemyToCounter, budget));

      // Get our possible options for countermeasures.
      int costBuffer = 0;
      Queue<CombatBuildDeets> availableBuilds = new PriorityQueue<>(new CombatBuldDeetsComparator());
      for( var currentCounter : CPI.modelToTerrainMap.keySet() )
      {
        var options = getBuildOptionsFor(gameMap, CPI, currentCounter, enemyToCounter);
        for( CombatBuildDeets opt : options )
        {
          XYCoord coord = opt.loc.getCoordinates();
          costBuffer = capperPrices.getOrDefault(coord, 0);
          // Filter stuff I can't afford
          if( opt.price > (budget + costBuffer) )
            continue;
          availableBuilds.add(opt);
        }
      }

      double idealEffect = 0;
      CombatBuildDeets idealCounter = null;
      for( CombatBuildDeets counterDeets : availableBuilds )
      {
        if( counterDeets.score < 1 )
          break;
        XYCoord coord = counterDeets.loc.getCoordinates();
        costBuffer = capperPrices.getOrDefault(coord, 0);
        Commander buyer = gameMap.getLocation(coord).getOwner();

        final int buyCost = counterDeets.price;
        double effectiveness = findEffectiveness(counterDeets.mfc, enemyToCounter);
        double scaledEfficiency = BANK_EFFICIENCY_FACTOR * effectiveness / buyCost;

        if( buyCost > (budget + costBuffer) )
          continue;

        log(String.format("    buy %s for %s with effectiveness %s%%? (%s + %s remaining)",
                              counterDeets.mfc, buyCost, (int)(100*effectiveness), budget, costBuffer));

        boolean bankInstead = false;
        for( ModelForCO otherCounter : CPI.availableUnitModels )
        {
          final int otherCost = buyer.getCost(otherCounter.um); // Generic cost calc since we've not decided where to buy
          if( MAX_BANK_FUNDS_FACTOR * buyCost < otherCost )
            continue;
          double otherEffectiveness = findEffectiveness(otherCounter, enemyToCounter);
          double otherEfficiency = otherEffectiveness / otherCost;
          if( otherEfficiency >= scaledEfficiency )
          {
            bankInstead = true;
            break; // If we can reasonably save for a much better unit, do so.
          }
        }
        if( bankInstead )
          continue;

        if(effectiveness >= idealEffect)
        {
          idealEffect = effectiveness;
          idealCounter = counterDeets;
        }
      } // ~for( availableUnitModels )

      if( null == idealCounter )
        continue;
      log(String.format("      buying %s", idealCounter));
      XYCoord idealCoord = idealCounter.loc.getCoordinates();
      builds.put(idealCoord, idealCounter.mfc.um);
      budget -= idealCounter.price - costBuffer;
      CPI.removeBuildLocation(gameMap.getLocation(idealCoord));

      int counterHPCountered    = (int) (enemyHP - idealEffect * 10);
      int counterFundsCountered = counterHPCountered * enemyToCounter.co.getCost(enemyToCounter.um) / 10;
      if( counterFundsCountered > 0 ) // Push it back in the queue if we haven't fully countered it.
      {
        enemyEntry.setValue(counterFundsCountered);
        enemyModels.add(enemyEntry);
      }
    } // ~while( !enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())

    int capHP = 0, allHP = 0;
    for( ModelForCO mfc : myUnitHP.keySet() )
    {
      allHP += myUnitHP.get(mfc);
      if( mfc.um.isAny(UnitModel.CAPTURE) )
        capHP += myUnitHP.get(mfc);
    }
    boolean captureDensityGood = false;
    if( capHP > 0 )
      captureDensityGood = (allHP / capHP) <= MAX_UNITS_PER_CAP_UNIT;

    // We want to specialize in sieges, build units for that if there's cash and nothing to counter
    var wantedTypes = myArmy.cos[0].getAllModels(UnitModel.SIEGE);
    for( boolean doUpgrades : new boolean[] { false, true } )
      for( UnitModel um : wantedTypes )
      {
        var options = new PriorityQueue<CombatBuildDeets>(new CombatBuldDeetsComparator());
        options.addAll(getBuildOptionsFor(gameMap, CPI, um, null));
        for( CombatBuildDeets opt : options )
        {
          final XYCoord coord = opt.loc.getCoordinates();

          int marginalCost = opt.price;
          if( builds.containsKey(coord) )
          {
            UnitModel currentBuild = builds.get(coord);
            boolean replaceable = false;
            replaceable |= captureDensityGood && capperTypes.contains(currentBuild);
            replaceable |= doUpgrades && wantedTypes.contains(currentBuild);
            if( !replaceable )
              break; // If it's not a standard build, don't override
            if( um.costBase < currentBuild.costBase )
              break; // If it's a standard build that's more expensive, it's a counter unit
            marginalCost -= opt.mfc.co.getBuyCost(currentBuild, coord);
          }

          if( marginalCost <= budget )
          {
            builds.put(coord, um);
            budget -= marginalCost;
          }
        }
      }
    return builds;
  }

  /**
   * Sort units by funds amount in descending order.
   */
  private static class EntryValueComparator<T> implements Comparator<Entry<T, Integer>>
  {
    @Override
    public int compare(Entry<T, Integer> entry1, Entry<T, Integer> entry2)
    {
      int diff = entry2.getValue() - entry1.getValue();
      return diff;
    }
  }

  /** Returns effective power in terms of whole kills per unit, based on respective threat areas and how much damage I deal */
  public double findEffectiveness(ModelForCO model, ModelForCO target)
  {
    // TODO: account for average terrain defense?
    UnitContext mc = new UnitContext(model.co, model.um);
    UnitContext tc = new UnitContext(target.co, target.um);
    // These can technically come from different weapons, but we're going for a conservative estimate.
    double enemyRange = 0;
    int enemyDamage = 0;
    for( WeaponModel wm : target.um.weapons )
    {
      tc.setWeapon(wm);
      double range = tc.rangeMax;
      if( wm.canFireAfterMoving() )
        range += getEffectiveMove(target);
      else
        range -= (Math.pow(wm.rangeMin(), MIN_SIEGE_RANGE_WEIGHT) - 1); // penalize range based on inner range
      enemyRange = Math.max(enemyRange, range);
      enemyDamage = Math.max(enemyDamage, CombatEngine.calculateOneStrikeDamage(tc, tc.rangeMax, mc, predMap, CalcType.OPTIMISTIC));
    }
    double bestScore = 0;
    for( WeaponModel wm : model.um.weapons )
    {
      mc.setWeapon(wm);
      double damage = CombatEngine.calculateOneStrikeDamage(mc, mc.rangeMax, tc, predMap, CalcType.OPTIMISTIC);

      double myRange = mc.rangeMax;
      if( wm.canFireAfterMoving() )
        myRange += getEffectiveMove(model);
      else
        myRange -= (Math.pow(wm.rangeMin(), MIN_SIEGE_RANGE_WEIGHT) - 1); // penalize range based on inner range

      double rangeMod = Math.pow(myRange / enemyRange, RANGE_WEIGHT);
      if( !Double.isFinite(rangeMod) )
        rangeMod = 1;
      double damageMod = ((double) damage) / UnitModel.MAXIMUM_HEALTH;
      if( mc.rangeMax == 1 && enemyDamage > 0 ) // Scale our effective damage by our direct combat (dis)advantage, if we're a direct.
        damageMod *= Math.min(10, ((double) damage) / enemyDamage);

      double effectiveness = damageMod * rangeMod;
      bestScore = Math.max(bestScore, effectiveness);
    }
    return bestScore;
  }
  public double getEffectiveMove(ModelForCO model)
  {
    if( unitEffectiveMove.containsKey(model) )
      return unitEffectiveMove.get(model);

    UnitContext uc = new UnitContext(model.co, model.um);
    MoveType p = uc.calculateMoveType();
    GameMap map = myArmy.myView;
    double totalCosts = 0;
    int validTiles = 0;
    double totalTiles = map.mapWidth * map.mapHeight; // to avoid integer division
    // Iterate through the map, counting up the move costs of all valid terrain
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        Environment terrain = map.getLocation(w, h).getEnvironment();
        if( p.canStandOn(terrain) )
        {
          validTiles++;
          int cost = p.getMoveCost(terrain);
          totalCosts += Math.pow(cost, TERRAIN_PENALTY_WEIGHT);
        }
      }
    }
    //             term for how fast you are   term for map coverage
    double ratio = (validTiles / totalCosts) * (validTiles / totalTiles); // 1.0 is the max expected value

//    double effMove = model.calculateMovePower() * ratio;
    double effMove = uc.calculateMovePower() * ratio;
    unitEffectiveMove.put(model, effMove);
    return effMove;
  }

  private static class AttackValue
  {
    final int hpLoss, hpDamage, loss, damage;
    final int fundsDelta;

    // Terrain-attack constructor
    public AttackValue(WallyAI ai, UnitContext actor, XYCoord target, GameMap map, boolean ignoreWallValue)
    {
      MapLocation targetLoc = map.getLocation(target);
      StrikeParams params = CombatEngine.calculateTerrainDamage(actor.unit, actor.path, targetLoc, map);
      hpLoss     = 0;
      hpDamage   = (int) params.calculateDamage();
      loss       = 0;
      damage     = 0;
      fundsDelta = 1;
    }
    public AttackValue(WallyAI ai, UnitContext actor, UnitContext target, GameMap gameMap, boolean ignoreWallValue)
    {
      this(ai, CombatEngine.simulateBattleResults(actor, target, gameMap, CALC), gameMap, ignoreWallValue);
    }
    public AttackValue(WallyAI ai, BattleSummary results, GameMap gameMap, boolean ignoreWallValue)
    {
      UnitContext actor  = results.attacker.before;
      UnitContext target = results.defender.before;
      hpLoss   = actor .getHP() - Math.max(0, results.attacker.after.getHP());
      hpDamage = target.getHP() - Math.max(0, results.defender.after.getHP());

      final int actorCost = WallyAI.valueUnit(gameMap, actor.unit, false);
      int targetValue = WallyAI.valueUnit(gameMap, target.unit, false);
      int captureValue = 0;
      if( target.unit.getCaptureProgress() > 0 ) // Assume we deny a turn of income
      {
        captureValue = actor.CO.gameRules.incomePerCity;
        if( target.CO.unitProductionByTerrain.containsKey(gameMap.getEnvironment(target.coord).terrainType) )
          captureValue += TERRAIN_INDUSTRY_WEIGHT;
      }

      int hpDiffValue = 0;
      if( results.attacker.after.getHealth() <= 0 )
        hpDiffValue -= YEET_FUNDS_BIAS;
      if( results.defender.after.getHealth() <= 0 )
        hpDiffValue += KILL_FUNDS_BIAS;
      if( hpDamage > 0 && target.getHealth() == UnitModel.MAXIMUM_HEALTH )
        hpDiffValue += CHIP_FUNDS_BIAS;

      loss     = (hpLoss   * actorCost  ) / 10;
      damage   = (hpDamage * targetValue) / 10 + captureValue + hpDiffValue;

      int wallValue = 0;
      if( !ignoreWallValue )
        wallValue = ai.wallFundsValue(gameMap, ai.threatMap, actor.unit, actor.coord, target.unit);
      //                                 funds "gained"              funds lost
      fundsDelta = (int) (damage*AGGRO_FUNDS_WEIGHT + wallValue)    -   loss;
      // This double-values counterdamage on units that aren't safe, but that seems pretty harmless?
    }
  }
  public int valueCapture(CaptureAction ga, GameMap gameMap)
  {
    Unit unit = ga.getActor();
    XYCoord capCoord = ga.getMoveLocation();
    int capProgress = 0;
    if( 0 == capCoord.getDistance(unit) )
      capProgress += unit.getCaptureProgress();
    int capValue = new UnitContext(unit).calculateCapturePower();
    int capThreshold = gameMap.getEnvironment(capCoord).terrainType.getCaptureThreshold();
    int capTurns = (capThreshold - capProgress + capValue-1) / capValue;

    int yeetFactor = valueTerrain(unit.CO, gameMap.getEnvironment(capCoord).terrainType);
    if( capTurns == 1 )
      return (int) (yeetFactor * COMPLETE_CAPTURE_WEIGHT);

    yeetFactor *= 2; // To restore the previous value scale for full-HP caps
    // Since we can't be certain of a capture, ballpark the terrain value scaled by the capture time.
    return yeetFactor / capTurns;
  }

  private static void enqueuePostReqPlans(UnitPrediction[][] mapPlan, ActionPlan initialPostreq, Collection<ActionPlan> postrequisites)
  {
    if( null == initialPostreq )
      return;
    // Note: this means the initial actor's plan may end up in its own postrequisite list
    postrequisites.add(initialPostreq);

    XYCoord sourceCoord = initialPostreq.actor.coord;
    // Other plan moves into my actor's current tile - that's a postrequisite
    if( !initialPostreq.action.getMoveLocation().equals(sourceCoord) )
    {
      enqueuePostReqPlans(mapPlan, mapPlan[sourceCoord.x][sourceCoord.y].toAchieve, postrequisites);
    }
    // Other plan moves into a tile I clear - that's also a postrequisite
    if( initialPostreq.isAttack )
    {
      XYCoord target = initialPostreq.action.getTargetLocation();
      ActionPlan actionIntoClearedTile = mapPlan[target.x][target.y].toAchieve;
      if( !postrequisites.contains(actionIntoClearedTile) )
        enqueuePostReqPlans(mapPlan, actionIntoClearedTile, postrequisites);
      else
        System.out.println(String.format("Warning: postrequisite cycle found between:\n%s\n%s", initialPostreq, actionIntoClearedTile));
    }
  }

  /**
   * Finds all prerequisites of the input movetile/action, and returns true if any are in the provided postrequisite list
   * <p>Note: postrequisites may contain the root plan (see above in enqueuePostReqPlans), so don't check if the postreqs contain the root.
   */
  private boolean preReqConflictExists(GameMap map, Unit unit, GamePath path, Collection<ActionPlan> postrequisites, HashSet<ActionPlan> foundPrereqs)
  {
    if( foundPrereqs.size() > 42 )
      return true;
    var evictee = map.getResident(path.getEndCoord());
    // I need this unit to move so I can take his space - that's a prerequisite
    if( null != evictee && evictee != unit && !evictee.CO.isEnemy(myArmy) )
    {
      var prereq = plansByUnit.get(evictee);
      if( null != prereq ) // If there's no plan, it can't already be a planned postrequisite. Probably.
      {
        if( postrequisites.contains(prereq) )
          return true;
        if( foundPrereqs.contains(prereq) )
          return true;
        foundPrereqs.add(prereq);
        if( preReqConflictExists(map, prereq.actor.unit, prereq.path, postrequisites, foundPrereqs) )
          return true;
      }
    }

    for( PathNode node : path.getWaypoints() )
    {
      var dest = node.GetCoordinates();
      // I need this unit to help clear the tile I'm moving into - that's a prerequisite
      // Note: I can't move into a tile that I'd help clear by shooting it, so don't worry about that possibility
      for( ActionPlan prereq : mapPlan[dest.x][dest.y].damageInstances.keySet() )
      {
        if( postrequisites.contains(prereq) )
          return true;
        if( foundPrereqs.contains(prereq) )
          return true;
        foundPrereqs.add(prereq);
        if( preReqConflictExists(map, prereq.actor.unit, prereq.path, postrequisites, foundPrereqs) )
          return true;
      }
    }
    // Will need to handle extra tiles for UNLOAD.
    return false;
  }

  /**
   * This is probably some kind of sin against design
   * <p>But it provides a way to get Utils to assume the map state is what I think it will be, so it seems legit
   * <p>Overrides/implements the base methods for the unit-in-tile fetching behavior
   */
  public static class PredictionMap extends MapPerspective
  {
    private static final long serialVersionUID = 1L;
    private UnitPrediction[][] mapPlan;
    public PredictionMap(Army pViewer, UnitPrediction[][] mapPlan)
    {
      super(pViewer.myView, pViewer);
      this.mapPlan = mapPlan;
    }
    @Override
    public MapLocation getLocation(int x, int y)
    {
      XYCoord coord = new XYCoord(x, y);
      MapLocation masterLoc = master.getLocation(coord);
      MapLocation returnLoc = new MapLocation(masterLoc.getEnvironment(), coord);
      returnLoc.setOwner(masterLoc.getOwner());

      Unit resident = calcResident(x, y);
      returnLoc.setResident(resident);
      return returnLoc;
    }
    @Override
    public boolean isLocationEmpty(Unit unit, int x, int y)
    {
      Unit resident = calcResident(x, y);
      return null == resident || resident == unit;
    }
    public boolean helpsClearTile(ActionPlan plan)
    {
      if( plan.action.getType() != UnitActionFactory.ATTACK )
        return false;
      XYCoord tt = plan.action.getTargetLocation();
      Unit resident = getResident(tt);
      // If it's empty or we've already planned to fill it with our own dude, we planned to kill with this action.
      return null == resident || !viewer.isEnemy(resident.CO);
    }
    public Unit calcResident(int x, int y)
    {
      Unit        resSource  = master.getResident(x, y);
      UnitContext resPlanned = mapPlan[x][y].identity;

      if( null == resSource && null == resPlanned )
        return null;

      // Default to returning the planned unit. Return the "real" unit if we have to.
      Unit resFake = null;
      if( null != resPlanned )
      {
        resFake = resPlanned.unit;
        if( null == resFake )
        {
          // If we've planned a unit that doesn't exist, make stuff up
          resFake = new Unit(resSource.CO, resSource.model);
          resFake.x = x;
          resFake.y = y;
        }
        else if( resFake.getHP() < 1 ) // Keep zombies at bay
          resFake = null;
      }

      // Use the planned unit if there's nobody in the way, or the other body in the problem is mine.
      if( null == resSource ||
          (!resSource.isTurnOver && viewer == resSource.CO.army) )
        return resFake;

      // If the actual unit is not in its "assigned seat" (i.e. off the map, because it's dead), treat it as dead.
      // Note that this check is only valid for enemy units, since planned units usually *won't* actually be on the tile we're looking at.
      if( viewer.isEnemy(resSource.CO) &&
          0 < new XYCoord(resSource).getDistance(x, y) )
        return resFake;

      // Collect planned damage so far
      int damagePercent = 0;
      for( int hit : mapPlan[x][y].damageInstances.values() )
        damagePercent += hit;
      int realHealth = resSource.getHealth();
      if( damagePercent >= realHealth )
      {
        // If we think it will be dead, don't report its presence
        if( resSource == resFake )
          return null;
        return resFake;
      }

      return resFake;
    }
    /** Debugging function - see what tiles we think we've planned to kill the residents of */
    public HashMap<XYCoord, HashMap<ActionPlan, Integer>> calcPredictedClearTiles()
    {
      var result = new HashMap<XYCoord, HashMap<ActionPlan, Integer>>();
      final int minX = 0;
      final int minY = 0;
      final int maxX = mapWidth  - 1;
      final int maxY = mapHeight - 1;

      for( int y = minY; y <= maxY; y++ ) // Top to bottom, left to right
      {
        for( int x = minX; x <= maxX; x++ )
        {
          Unit resident  = calcResident(x, y);
          Unit resSource = master.getResident(x, y);

          if( resSource != resident && null != resSource && viewer.isEnemy(resSource.CO) )
            result.put(new XYCoord(x, y), mapPlan[x][y].damageInstances);
        }
      }

      return result;
    }
  }
}
