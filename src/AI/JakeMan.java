package AI;

import java.util.*;
import java.util.Map.Entry;

import AI.CommanderProductionInfo;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.*;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.UnitActionLifecycles.CaptureLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.*;
import Units.*;


public class JakeMan extends ModularAI
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Army army)
    {
      return new JakeMan(army);
    }

    @Override
    public String getName()
    {
      return "J.A.K.E.M.A.N.";
    }

    @Override
    public String getDescription()
    {
      return
          "Wants your free dudes.\n" +
          "Competitive-style AI design credit @Lost&Found#6348, ID 824112693123612692.";
    }
  }
  public static final AIMaker info = new instantiator();

  @Override
  public AIMaker getAIInfo()
  {
    return info;
  }

  protected Army myArmy = null;

  // What % damage I'll ignore when checking safety
  private static final int INDIRECT_THREAT_THRESHHOLD = 7;
  private static final int DIRECT_THREAT_THRESHHOLD = 13;
  private static final int    UNIT_HEAL_THRESHHOLD = 6; // HP at which units heal
  private static final double UNIT_REFUEL_THRESHHOLD = 1.3; // Factor of cost to get to fuel to start worrying about fuel
  private static final double UNIT_REARM_THRESHHOLD = 0.25; // Fraction of ammo in any weapon below which to consider resupply

  private static final double TERRAIN_FUNDS_WEIGHT = 2.5; // Multiplier for per-city income for adding value to units threatening to cap
  private static final double TERRAIN_INDUSTRY_WEIGHT = 20000; // Funds amount added to units threatening to cap an industry
  private static final double TERRAIN_HQ_WEIGHT = 42000; //      

  private Map<UnitModel, Map<XYCoord, Double>> unitMapEnemy;
  private Map<UnitModel, Map<XYCoord, Double>> unitMapFriendly;
  protected Map<XYCoord, ArrayList<ArrayList<CapStop>>> capChains = new HashMap<>();
  protected Map<Unit, ArrayList<CapStop>> capChainsAllocated = new HashMap<>();
  private ArrayList<XYCoord> contestedProps; // as was probably considered by the map designer; doesn't necessarily take movement/production differences into account

  UnitModel infantry;
  ArrayList<UnitModel> allTanks;
  UnitModel antiAir;
  UnitModel copter;

  public JakeMan(Army army)
  {
    super(army);
    myArmy = army;

    // look where all vehicles are and what their threat ranges are (yes, mechs are vehicles)
    // take free dudes that you have more defenders for than them
    // move your leftover vehicles so they cover the tiles attacking most relevant stuff (contested cities, own units)
    // move infs as far towards nearest contested stuff as you can without underdefending, cap if possible

    aiPhases = new ArrayList<AIModule>(
        Arrays.asList(
            new PowerActivator(army, CommanderAbility.PHASE_TURN_START),
            new CapChainActuator(army, this),
            new CaptureFinisher(army, this),
            new GenerateThreatMap(army, this), // FreeRealEstate and Travel need this, and NHitKO/building do too because of eviction

            new PowerActivator(army, CommanderAbility.PHASE_BUY),
            new GetFreeDudes(army, this, false, false), // prioritize non-eviction
            new GetFreeDudes(army, this, true,  false), // evict if necessary
            new BuildStuff(army, this),
            new GetFreeDudes(army, this, true,  true), // step on industries we're not using
            new Travel(army, this),

            new PowerActivator(army, CommanderAbility.PHASE_TURN_END)
            ));
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    super.initTurn(gameMap);
    if( null == contestedProps )
      init(gameMap);
    log(String.format("[======== JakeMan initializing turn %s for %s =========]", turnNum, myArmy));
  }

  @Override
  public void endTurn()
  {
    super.endTurn();
    log(String.format("[======== JakeMan ending turn %s for %s =========]", turnNum, myArmy));
  }

  public static class GenerateThreatMap implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myArmy;
    public final JakeMan ai;

    public GenerateThreatMap(Army co, JakeMan ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      ai.unitMapEnemy = new HashMap<UnitModel, Map<XYCoord, Double>>();
      ai.unitMapFriendly = new HashMap<UnitModel, Map<XYCoord, Double>>();
      Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(null, gameMap);
        for( Commander co : unitLists.keySet() )
        {
        Map<UnitModel, Map<XYCoord, Double>> mapToFill;
          if( myArmy.isEnemy(co) )
          mapToFill = ai.unitMapEnemy;
        else
          mapToFill = ai.unitMapFriendly;

            for( Unit threat : unitLists.get(co) )
            {
              // add each new threat to the existing threats
          final UnitModel um = threat.model;
          if( !mapToFill.containsKey(um) )
            mapToFill.put(um, new HashMap<>());
          Map<XYCoord, Double> threatArea = mapToFill.get(um);
          double newValue = threat.getHP() / 10.0;
          for( XYCoord coord : AICombatUtils.findThreatPower(gameMap, threat, null).keySet() )
              {
            if( !threatArea.containsKey(coord) )
              threatArea.put(coord, newValue);
                else
              threatArea.put(coord, newValue + threatArea.get(coord));
          }
        }
      }

      return null;
    }
  }

  public static class CapChainActuator extends UnitActionFinder
  {
    private static final long serialVersionUID = 1L;
    JakeMan aiCast;
    public CapChainActuator(Army co, JakeMan ai)
    {
      super(co, ai);
      aiCast = ai;
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap map)
    {
      // Don't really care to handle COs that modify what can cap
      if( !unit.model.baseActions.contains(UnitActionFactory.CAPTURE) )
        return null;

      XYCoord position = new XYCoord(unit.x, unit.y);
      // Add the unit to a cap chain, if possible
      if( !aiCast.capChainsAllocated.containsKey(unit) )
      {
        ArrayList<ArrayList<CapStop>> chainList = aiCast.capChains.get(position);
        if( null != chainList && !chainList.isEmpty() )
        {
          aiCast.capChainsAllocated.put(unit, chainList.remove(0));
          if( chainList.isEmpty() )
            aiCast.capChains.remove(position);
        }
      }
      // Follow the cap chain, if possible
      if( aiCast.capChainsAllocated.containsKey(unit) )
      {
        if( unit.getCaptureProgress() > 0 )
        {
          ai.unownedProperties.remove(position);
          return new CaptureLifecycle.CaptureAction(map, unit, Utils.findShortestPath(unit, position, map));
        }

        ArrayList<CapStop> chain = aiCast.capChainsAllocated.get(unit);
        while (!chain.isEmpty())
        {
          final XYCoord coord = chain.get(0).coord;
          // If we're already there or own it, throw the point out
          if( position.equals(coord) || !myArmy.isEnemy(map.getLocation(coord).getOwner()) )
            chain.remove(0);
          else
            return findChainAction(map, unit, chain, aiCast);
        }
        // Chain's done, if we ever get here
        aiCast.capChainsAllocated.remove(unit);
      }

      return null;
    }

    public static GameAction findChainAction(GameMap gameMap, Unit unit, ArrayList<CapStop> chain, JakeMan ai)
    {
      XYCoord goal = chain.get(0).coord;

      boolean includeOccupiedSpaces = true; // Since we know how to shift friendly units out of the way
      ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeOccupiedSpaces);

      // If we can get to our destination, go for it
      if( destinations.contains(goal) )
        return new CaptureLifecycle.CaptureAction(gameMap, unit, Utils.findShortestPath(unit, goal, gameMap));

      Utils.sortLocationsByDistance(goal, destinations);

      for( XYCoord moveCoord : destinations )
      {
        Unit resident = gameMap.getLocation(moveCoord).getResident();
        boolean spaceFree = null == resident;
        if( !spaceFree )//&& ((unit.CO != resident.CO || resident.isTurnOver)) )
          continue; // Bail if we can't clear the space

        // Figure out how to get here.
        GamePath movePath = Utils.findShortestPath(unit, moveCoord, gameMap);

        return new WaitLifecycle.WaitAction(unit, movePath);
      }
      return null;
    }
  }
  
  // Try to get unit value by capture or attack
  public static class GetFreeDudes extends UnitActionFinder
  {
    private static final long serialVersionUID = 1L;
    private final JakeMan ai;
    private final boolean canEvict, canStepOnProduction;
    public GetFreeDudes(Army co, JakeMan ai, boolean canEvict, boolean canStepOnProduction)
    {
      super(co, ai);
      this.ai = ai;
      this.canEvict = canEvict;
      this.canStepOnProduction = canStepOnProduction;
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap gameMap)
    {
      boolean mustMove = false;
      return findFreeDude(unit.CO, ai, unit, gameMap, mustMove, !canStepOnProduction, canEvict);
    }

    public static GameAction findFreeDude( Commander co, JakeMan ai,
                                              Unit unit, GameMap gameMap,
                                              boolean mustMove, boolean avoidProduction,
                                              boolean canEvict )
    {
      XYCoord position = new XYCoord(unit.x, unit.y);

      boolean includeOccupiedSpaces = true; // Since we know how to shift friendly units out of the way
      ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeOccupiedSpaces);
      if( mustMove )
        destinations.remove(new XYCoord(unit.x, unit.y));
      destinations.removeAll(AIUtils.findAlliedIndustries(gameMap, co.army, destinations, !avoidProduction));
      // sort by furthest away, good for capturing
      Utils.sortLocationsByDistance(position, destinations);
      Collections.reverse(destinations);

      for( XYCoord moveCoord : destinations )
      {
        // Figure out how to get here.
        GamePath movePath = Utils.findShortestPath(unit, moveCoord, gameMap);

        // Figure out what I can do here.
        ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath, includeOccupiedSpaces);
        for( GameActionSet actionSet : actionSets )
        {
          Unit resident = gameMap.getLocation(moveCoord).getResident();
          boolean spaceFree = null == resident;
          if( !spaceFree )//&& ((unit.CO != resident.CO || resident.isTurnOver)) )
            continue; // Bail if we can't clear the space

          if( ai.isDudeFree(gameMap, unit, moveCoord) )
          {
            final GameAction ga = actionSet.getSelected();
            if( ga.getType() == UnitActionFactory.CAPTURE )
          {
              return ga;
            }
            if( ga.getType() == UnitActionFactory.ATTACK )
            {
              MapLocation targetLoc = gameMap.getLocation(ga.getTargetLocation());
              Unit target = targetLoc.getResident();

              BattleSummary results =
                  CombatEngine.simulateBattleResults(unit, target, gameMap, movePath);
              double loss   = Math.min(unit  .getHP(), (int)results.attacker.getPreciseHPDamage());
              double damage = Math.min(target.getHP(), (int)results.defender.getPreciseHPDamage());
              

              if( damage > loss )

                return ga;
            }

          // Only consider capturing if we can sit still or go somewhere safe.
          }
        }
      }
      return null;
    }
  }

  // If no attack/capture actions are available now, just move around
  public static class Travel extends UnitActionFinder
  {
    private static final long serialVersionUID = 1L;
    private final JakeMan ai;
    public Travel(Army co, JakeMan ai)
    {
      super(co, ai);
      this.ai = ai;
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap gameMap)
    {
      ai.log(String.format("Evaluating travel for %s.", unit.toStringWithLocation()));
      boolean avoidProduction = false;
      boolean ignoreSafety = false;
      return ai.findTravelAction(gameMap, unit, false, ignoreSafety, avoidProduction);
    }
  }

  public static class BuildStuff implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public final Army myArmy;
    public final JakeMan ai;

    public BuildStuff(Army co, JakeMan ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    Map<XYCoord, UnitModel> builds;

    @Override
    public void endTurn()
    {
      if( null != builds )
      {
        ai.log(String.format("Warning - builds not null on turn end; contains %s", builds));
        builds = null;
      }
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      if( null == builds )
        builds = ai.queueUnitProductionActions(gameMap);

      for( XYCoord coord : new ArrayList<XYCoord>(builds.keySet()) )
      {
        ai.log(String.format("Attempting to build %s at %s", builds.get(coord), coord));
        Unit resident = gameMap.getResident(coord);
        if( null != resident )
        {
          boolean ignoreSafety = true, avoidProduction = true;
          if( resident.CO.army == myArmy && !resident.isTurnOver )
            return ai.evictUnit(gameMap, null, resident, ignoreSafety, avoidProduction);
          else
          {
            ai.log(String.format("  Can't evict unit %s to build %s", resident.toStringWithLocation(), builds.get(coord)));
            builds.remove(coord);
            continue;
          }
        }
        MapLocation loc = gameMap.getLocation(coord);
        Commander buyer = loc.getOwner();
        ArrayList<UnitModel> list = buyer.getShoppingList(loc);
        UnitModel toBuy = builds.get(coord);
        if( buyer.getBuyCost(toBuy, coord) <= myArmy.money && list.contains(toBuy) )
        {
          builds.remove(coord);
          return new GameAction.UnitProductionAction(buyer, toBuy, coord);
        }
        else
        {
          ai.log(String.format("  Trying to build %s, but it's unavailable at %s", toBuy, coord));
          continue;
        }
      }

      builds = null;
      return null;
    }
  }

  /** Produces a list of destinations for the unit, ordered by their relative precedence */
  private ArrayList<XYCoord> findTravelDestinations(
                                  GameMap gameMap,
                                  Unit unit,
                                  boolean avoidProduction )
  {
    UnitContext uc = new UnitContext(gameMap, unit);
    uc.calculatePossibleActions();
    ArrayList<XYCoord> goals = new ArrayList<XYCoord>();

    ArrayList<XYCoord> stations = AIUtils.findRepairDepots(unit);
    Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), stations);
    boolean shouldResupply = false;
    if( stations.size() > 0 )
    {
      shouldResupply = unit.getHP() <= UNIT_HEAL_THRESHHOLD;
      shouldResupply |= unit.fuel <= UNIT_REFUEL_THRESHHOLD
          * Utils.findShortestPath(unit, stations.get(0), gameMap).getFuelCost(unit, gameMap);
      shouldResupply |= unit.ammo >= 0 && unit.ammo <= unit.model.maxAmmo * UNIT_REARM_THRESHHOLD;
    }

    if( shouldResupply )
    {
      log(String.format("  %s needs supplies.", unit.toStringWithLocation()));
      goals.addAll(stations);
      if( avoidProduction )
        goals.removeAll(AIUtils.findAlliedIndustries(gameMap, myArmy, goals, !avoidProduction));
    }
    else if( uc.possibleActions.contains(UnitActionFactory.CAPTURE) )
    {
      for( XYCoord xyc : unownedProperties )
        if( !AIUtils.isCapturing(gameMap, myArmy.cos[0], xyc) )
          goals.add(xyc);
    }
    else if( uc.possibleActions.contains(UnitActionFactory.ATTACK) )
    {
      Map<UnitModel, Double> valueMap = new HashMap<UnitModel, Double>();
      Map<UnitModel, ArrayList<XYCoord>> targetMap = new HashMap<UnitModel, ArrayList<XYCoord>>();

      // Categorize all enemies by type, and all types by how well we match up vs them
      final ArrayList<XYCoord> allEnemies = AIUtils.findEnemyUnits(myArmy, gameMap);
      Utils.sortLocationsByTravelTime(unit, allEnemies, gameMap);
      for( XYCoord xyc : allEnemies )
      {
        Unit target = gameMap.getResident(xyc);
        UnitModel model = target.model;
        XYCoord targetCoord = new XYCoord(target.x, target.y);
        if (Utils.findShortestPath(unit, targetCoord, gameMap, true) != null &&
            isWeakTo(target.model, unit.model))
        {
          valueMap.put(model, (double)target.getCost());
          if (!targetMap.containsKey(model)) targetMap.put(model, new ArrayList<XYCoord>());
          targetMap.get(model).add(targetCoord);
        }
      }

      // Sort all individual target lists by distance
      for (ArrayList<XYCoord> targetList : targetMap.values())
        Utils.sortLocationsByTravelTime(unit, targetList, gameMap);

      // Sort all target types by how much we want to shoot them with this unit
      Queue<Entry<UnitModel, Double>> targetTypesInOrder = 
          new PriorityQueue<Entry<UnitModel, Double>>(myArmy.cos[0].unitModels.size(), new UnitModelFundsComparator());
      targetTypesInOrder.addAll(valueMap.entrySet());

      while (!targetTypesInOrder.isEmpty())
      {
        UnitModel model = targetTypesInOrder.poll().getKey(); // peel off the juiciest
        goals.addAll(targetMap.get(model)); // produce a list ordered by juiciness first, then distance TODO: consider a holistic "juiciness" metric that takes into account both matchup and distance?
      }
    }

    if( goals.isEmpty() ) // Send 'em at production facilities if they haven't got anything better to do
    {
      for( XYCoord coord : unownedProperties )
      {
        MapLocation loc = gameMap.getLocation(coord);
        if( unit.CO.unitProductionByTerrain.containsKey(loc.getEnvironment().terrainType)
            && myArmy.isEnemy(loc.getOwner()) )
        {
          goals.add(coord);
        }
      }
    }

    if( goals.isEmpty() ) // If there's really nothing to do, go to MY HQ
      goals.addAll(myArmy.HQLocations);

    Utils.sortLocationsByTravelTime(unit, goals, gameMap);
    return goals;
  }

  /** Functions as working memory to prevent eviction cycles */
  private transient Set<Unit> evictionStack;
  private static final int    EVICTION_STACK_MAX_DEPTH = 7;
  /**
   * Queue the first action required to move a unit out of the way
   * For use after unit building is complete
   * Can recurse based on the other functions it calls.
   */
  private GameAction evictUnit(
                        GameMap gameMap,
                        Unit evicter, Unit unit,
                        boolean ignoreSafety,
                        boolean avoidProduction )
  {
    boolean isBase = false;
    if( null == evictionStack )
    {
      evictionStack = new HashSet<Unit>();
      isBase = true;
    }

    String spacing = "";
    for( int i = 0; i < evictionStack.size(); ++i ) spacing += "  ";
    log(String.format("%sAttempting to evict %s", spacing, unit.toStringWithLocation()));
    if( evicter != null )
      evictionStack.add(evicter);

    if( evictionStack.contains(unit) )
    {
      log(String.format("%s  Eviction cycle! Bailing.", spacing));
      return null;
    }
    if( evictionStack.size() > EVICTION_STACK_MAX_DEPTH )
    {
      log(String.format("%s  Too many units blocking! Bailing.", spacing));
      return null;
    }
    evictionStack.add(unit);

    boolean mustMove = true, canEvict = true;
    GameAction result = GetFreeDudes.findFreeDude(unit.CO, this, unit, gameMap, mustMove, avoidProduction, canEvict);
    if( null == result )
    {
      result = findTravelAction(gameMap, unit, ignoreSafety, mustMove, avoidProduction);
    }

    if( isBase )
      evictionStack = null;
    log(String.format("%s  Eviction of %s success? %s", spacing, unit.toStringWithLocation(), null != result));
    return result;
  }

  /**
   * Find a good long-term objective for the given unit, and pursue it (with consideration for life-preservation optional)
   */
  private GameAction findTravelAction(
                        GameMap gameMap,
                        Unit unit,
                        boolean ignoreSafety, boolean mustMove,
                        boolean avoidProduction )
  {
    // Find the possible destinations.
    boolean ignoreResident = true;
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, ignoreResident);
    destinations.removeAll(AIUtils.findAlliedIndustries(gameMap, myArmy, destinations, !avoidProduction));

    // TODO: Jump in a transport, if available, or join?

    XYCoord goal = null;
    GamePath path = null;
    ArrayList<XYCoord> validTargets = findTravelDestinations(gameMap, unit, avoidProduction);
    if( mustMove ) // If we *must* travel, make sure we do actually move.
    {
      destinations.remove(new XYCoord(unit.x, unit.y));
      validTargets.remove(new XYCoord(unit.x, unit.y));
    }

    for( XYCoord target : validTargets )
    {
      path = Utils.findShortestPath(unit, target, gameMap, true);
      if( path.getPathLength() > 0 ) // We can reach it.
      {
        goal = target;
        break;
      }
    }

    if( null == goal ) return null;

    // Choose the point on the path just out of our range as our 'goal', and try to move there.
    // This will allow us to navigate around large obstacles that require us to move away
    // from our intended long-term goal.
    path.snip(unit.getMovePower(gameMap) + 1); // Trim the path approximately down to size.
    XYCoord pathPoint = path.getEndCoord(); // Set the last location as our goal.

    // Sort my currently-reachable move locations by distance from the goal,
    // and build a GameAction to move to the closest one.
    Utils.sortLocationsByDistance(pathPoint, destinations);
    log(String.format("  %s is traveling toward %s at %s via %s  mustMove?: %s  ignoreSafety?: %s",
                          unit.toStringWithLocation(),
                          gameMap.getLocation(goal).getEnvironment().terrainType, goal,
                          pathPoint, mustMove, ignoreSafety));
    for( XYCoord xyc : destinations )
    {
      log(String.format("    is it safe to go to %s?", xyc));
      if( !ignoreSafety && !isDudeFree(gameMap, unit, xyc) )
        continue;

      GameAction action = null;
      Unit resident = gameMap.getLocation(xyc).getResident();
      if( null != resident && unit != resident )
      {
        boolean evictIgnoreSafety =
            valueUnit(unit, gameMap.getLocation(xyc), true) >= valueUnit(resident, gameMap.getLocation(xyc), true);
        if( unit.CO == resident.CO && !resident.isTurnOver )
          action = evictUnit(gameMap, unit, resident, evictIgnoreSafety, avoidProduction);
        if( null != action ) return action;
        continue;
      }
      log(String.format("    Yes"));

      GamePath movePath = Utils.findShortestPath(unit, xyc, gameMap);
      ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath, ignoreResident);
      if( actionSets.size() > 0 )
      {
        // Since we're moving anyway, might as well try shooting the scenery
        for( GameActionSet actionSet : actionSets )
        {
          if( actionSet.getSelected().getType() == UnitActionFactory.ATTACK )
          {
            double bestDamage = 0;
            for( GameAction attack : actionSet.getGameActions() )
            {
              double damageValue = AICombatUtils.scoreAttackAction(unit, attack, gameMap,
                  (results) -> {
                    double loss   = Math.min(unit                 .getHP(), (int)results.attacker.getPreciseHPDamage());
                    double damage = Math.min(results.defender.unit.getHP(), (int)results.defender.getPreciseHPDamage());

                    if( damage > loss ) // only shoot that which you hurt more than it hurts you
                      return damage * results.defender.unit.getCost();

                    return 0.;
                  }, (terrain, params) -> 0.01); // Attack terrain, but don't prioritize it over units

              if( damageValue > bestDamage )
              {
                log(String.format("      Best en passant attack deals %s", damageValue));
                bestDamage = damageValue;
                action = attack;
              }
            }
          }
        }

        if( null == action && movePath.getPathLength() > 1) // Just wait if we can't do anything cool
          action = new WaitLifecycle.WaitAction(unit, movePath);
        return action;
      }
    }
    return null;
  }

  private boolean isThreatenedBy(UnitModel um, UnitModel threat)
  {
    int threshhold = um.hasDirectFireWeapon() ? DIRECT_THREAT_THRESHHOLD : INDIRECT_THREAT_THRESHHOLD;
    boolean isThreat = false;
    for( WeaponModel wm : threat.weapons )
      isThreat |= threshhold <= wm.getDamage(um);
    return isThreat;
  }

  private boolean isWeakTo(UnitModel um, UnitModel threat)
  {
    boolean isWeak = isThreatenedBy(um, threat);
    isWeak &= !isThreatenedBy(threat, um);
    return isWeak;
  }

  private boolean isDudeFree(GameMap gameMap, Unit unit, XYCoord xyc)
  {
    int threshhold = unit.model.hasDirectFireWeapon() ? DIRECT_THREAT_THRESHHOLD : INDIRECT_THREAT_THRESHHOLD;
    HashMap<UnitModel, Double> threatCounts = new HashMap<>();
    for( UnitModel threat : unitMapEnemy.keySet() )
    {
      if( !isThreatenedBy(unit.model, threat) )
        continue;
      if( unitMapEnemy.get(threat).containsKey(xyc) )
        threatCounts.put(threat, unitMapEnemy.get(threat).get(xyc));
  }
    if( threatCounts.size() < 1 )
      return true;

    ArrayList<XYCoord> counterCoords = Utils.findLocationsInRange(gameMap, xyc, 1);
    counterCoords.remove(xyc);
    for( UnitModel threat : threatCounts.keySet().toArray(new UnitModel[0]) )
      for( UnitModel counter : unitMapFriendly.keySet() )
  {
        boolean isCounter = false;
        for( WeaponModel wm : counter.weapons )
          isCounter &= threshhold <= wm.getDamage(threat);
        if( !isCounter )
          continue;
        double counterPowerTotal = 0;
        for( XYCoord coord : counterCoords )
          if( unitMapFriendly.get(threat).containsKey(coord) )
            counterPowerTotal += unitMapFriendly.get(threat).get(coord);
        final double counterPowerAverage = counterPowerTotal / counterCoords.size();
        final double threatPower = threatCounts.get(threat);
        if( counterPowerAverage > threatPower )
          threatCounts.remove(threat);
        else
          threatCounts.put(threat, threatPower - counterPowerAverage);
      }
    return threatCounts.size() < 1;
  }

  private static int valueUnit(Unit unit, MapLocation locale, boolean includeCurrentHealth)
  {
    int value = unit.getCost();

    if( unit.CO.isEnemy(locale.getOwner()) &&
            unit.hasActionType(UnitActionFactory.CAPTURE)
            && locale.isCaptureable() )

      value += valueTerrain(unit.CO, locale.getEnvironment().terrainType); // Strongly value units that threaten capture
    if( includeCurrentHealth )
      value *= unit.getHP();
    value -= locale.getEnvironment().terrainType.getDefLevel(); // Value things on lower terrain more, so we wall for equal units if we can get on better terrain

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

  private Map<XYCoord, UnitModel> queueUnitProductionActions(GameMap gameMap)
  {
    // build tank unless proven otherwise
    Map<XYCoord, UnitModel> builds = new HashMap<XYCoord, UnitModel>();
    // Figure out what unit types we can purchase with our available properties.
    boolean includeFriendlyOccupied = true;
    CommanderProductionInfo CPI = new CommanderProductionInfo(myArmy, gameMap, includeFriendlyOccupied);

    if( CPI.availableProperties.isEmpty() )
    {
      log("No properties available to build.");
      return builds;
    }

    log("Evaluating Production needs");
    ArrayList<UnitModel> wantedTypes = new ArrayList<>();

    wantedTypes.add(infantry);
    wantedTypes.add(allTanks.get(0));
    wantedTypes.add(copter);
    wantedTypes.add(allTanks.get(1));

    int budget = myArmy.money;
    // Try to purchase as many of the biggest units I can
    for(int i = 0; i < wantedTypes.size(); ++i)
    {
      UnitModel um = wantedTypes.get(i);
      log(String.format("Buying %s?", um));
      int buildCount = 0;

      ArrayList<MapLocation> facilities = CPI.getAllFacilitiesFor(um);
      for( MapLocation loc : facilities )
      {
        Commander buyer = loc.getOwner();
        final XYCoord coord = loc.getCoordinates();
        final int cost = buyer.getBuyCost(um, coord);

        int marginalCost = cost;
        if( builds.containsKey(coord) )
          marginalCost -= buyer.getBuyCost(builds.get(coord), coord);

        if( marginalCost <= budget )
        {
          builds.put(coord, um);
          ++buildCount;
          budget -= marginalCost;
        }
        else if( um == copter ) // Consider downgrading a tank to an inf to get a copter
        {
          for( XYCoord tankCoord : builds.keySet().toArray(new XYCoord[0]) )
          {
            if( allTanks.get(0) == builds.get(tankCoord) )
            {
              int downgradeSavings = 0;
              downgradeSavings += buyer.getBuyCost(builds.get(tankCoord), coord);
              downgradeSavings -= buyer.getBuyCost(infantry, coord);

              if( marginalCost - downgradeSavings <= budget )
              {
                builds.put(tankCoord, infantry);
                builds.put(coord, um);
                ++buildCount;
                budget -= marginalCost - downgradeSavings;
              }
            }
          }
        } //~copter upgrade
      }
      log(String.format("  Built %s; Budget: %s / %s", buildCount, budget, myArmy.money));
    }

    return builds;
  }

  /**
   * Sort units by funds amount in descending order.
   */
  private static class UnitModelFundsComparator implements Comparator<Entry<UnitModel, Double>>
  {
    @Override
    public int compare(Entry<UnitModel, Double> entry1, Entry<UnitModel, Double> entry2)
    {
      double diff = entry2.getValue() - entry1.getValue();
      return (int) (diff * 10); // Multiply by 10 since we return an int, but don't want to lose the decimal-level discrimination.
    }
  }


  private static class CapStop
  {
    public int extraTiles = 0; // Defines how many turns we have already looked ahead to try to find another cap stop
    public final XYCoord coord;
    public CapStop(XYCoord coord)
    {
      this.coord = coord;
    }
    @Override
    public String toString()
    {
      return coord + "+" + extraTiles;
    }
  }
  /**
   * Sort CapStop lists by potential total profit
   */
  private static class CapStopFundsComparator implements Comparator<ArrayList<CapStop>>
  {
    public final int infMove;
    public CapStopFundsComparator(int infMove)
    {
      this.infMove = infMove;
    }
    public int turnLimit = 13;
    @Override
    public int compare(ArrayList<CapStop> entry1, ArrayList<CapStop> entry2)
    {
      final int entry2Val = IncomeTillTurn(entry2, turnLimit, infMove);
      final int entry1Val = IncomeTillTurn(entry1, turnLimit, infMove);
      int diff = entry2Val - entry1Val;
      return diff;
    }
    // Calculates the number of building/incomes we'll get by TURN_LIMIT from the input capture chain
    public static int IncomeTillTurn(ArrayList<CapStop> capList, int turnLimit, int infMove)
    {
      // Start at 1, since we know the first item is just a build
      int currentTurn = 1;
      int currentIncome = 0;

      for(int i = 1; i < capList.size() || currentTurn >= turnLimit; ++i)
      {
        int turnShift = (int) Math.ceil(capList.get(i-1).extraTiles / (double)infMove);
        currentTurn += turnShift + 1; // +1 for the extra cap turn
        // We get income from the prop for every turn after we captured it
        currentIncome += Math.max(0, turnLimit - currentTurn);
      }

      return currentIncome;
    }
  }

  private void init(GameMap map)
  {
    contestedProps = new ArrayList<>();
    infantry = myArmy.cos[0].getUnitModel(UnitModel.TROOP);
    allTanks = myArmy.cos[0].getAllModels(UnitModel.ASSAULT);
    antiAir  = myArmy.cos[0].getUnitModel(UnitModel.SURFACE_TO_AIR);
    copter   = myArmy.cos[0].getUnitModel(UnitModel.ASSAULT | UnitModel.AIR_LOW, false);

    ArrayList<XYCoord> props = new ArrayList<>();
    HashMap<XYCoord, Commander> factoryOwnership = new HashMap<>();
    for( int i = 0; i < map.mapWidth; i++ )
    {
      for( int j = 0; j < map.mapHeight; j++ )
      {
        Environment env = map.getEnvironment(i, j);
        if(env.terrainType == TerrainType.FACTORY)
        {
          factoryOwnership.put(new XYCoord(i, j), map.getLocation(i, j).getOwner());
        }
        if(env.terrainType == TerrainType.CITY
            || env.terrainType == TerrainType.AIRPORT
            // TODO: Comm towers
            )
        {
          props.add(new XYCoord(i, j));
        }
      }
    }

    ArrayList<XYCoord> rightfulProps = new ArrayList<>();
    ArrayList<XYCoord> rightfulFactories = new ArrayList<>();
    ArrayList<XYCoord> startingFactories = new ArrayList<>();
    // Fully calculate factory ownership based on who can cap each first
    // Assumption: No contested factories
    for( XYCoord neutralFac : factoryOwnership.keySet() )
    {
      final Commander currentOwner = factoryOwnership.get(neutralFac);
      if( currentOwner != null )
      {
        if( myArmy == currentOwner.army )
          startingFactories.add(neutralFac);
        continue; // Not actually neutral
      }

      int newOwnerDistance = Integer.MAX_VALUE;
      Commander newOwner = null;
      for( XYCoord ownedFac : factoryOwnership.keySet() )
      {
        final Commander owner = factoryOwnership.get(ownedFac);
        if( owner == null )
          continue; // Not yet owned

        final Unit inf = new Unit(owner, owner.getUnitModel(UnitModel.TROOP));
        inf.x = ownedFac.xCoord;
        inf.y = ownedFac.yCoord;

        final GamePath infPath = Utils.findShortestPath(inf, neutralFac, map, true);
        if( null == infPath || infPath.getPathLength() < 1 )
          continue; // Can't reach

        int distance = infPath.getFuelCost(inf, map);
        if( distance < newOwnerDistance )
        {
          newOwnerDistance = distance;
          newOwner = owner;
        }
      }
      factoryOwnership.put(neutralFac, newOwner);
      if( null != newOwner && myArmy == newOwner.army )
        rightfulFactories.add(neutralFac);
    }

    // Finally, figure out what non-factories are contested or rightfully mine
    for( XYCoord propXYC : props )
    {
      int newOwnerDistance = Integer.MAX_VALUE;
      Commander newOwner = null;
      for( XYCoord ownedFac : factoryOwnership.keySet() )
      {
        final Commander owner = factoryOwnership.get(ownedFac);
        if( owner == null )
          continue; // Don't barf in weird maps

        final Unit tank = new Unit(owner, owner.getUnitModel(UnitModel.ASSAULT));
        tank.x = ownedFac.xCoord;
        tank.y = ownedFac.yCoord;

        final GamePath tankPath = Utils.findShortestPath(tank, propXYC, map, true);
        if( null == tankPath || tankPath.getPathLength() < 1 )
          continue; // Can't reach this city

        // Measured in % of a turn
        int distance = tankPath.getFuelCost(tank, map) * 100 / tank.getMovePower(map);
        int distanceDelta = Math.abs(newOwnerDistance - distance);
        if( distanceDelta <= 100 && owner.isEnemy(newOwner) )
        {
          newOwner = null;
          contestedProps.add(propXYC);
          break;
        }
        if( distance < newOwnerDistance )
        {
          newOwnerDistance = distance;
          newOwner = owner;
        }
      }
      if( null != newOwner && myArmy == newOwner.army
          && myArmy.isEnemy(map.getLocation(propXYC).getOwner()) // Don't try to cap it if we already own it
          )
        rightfulProps.add(propXYC);
    }
    
    // Build cap chains to factories; don't continue them, since cap chains from that factory will be considered separately
    ArrayList<ArrayList<CapStop>> factoryCapChains = new ArrayList<>();
    while (!rightfulFactories.isEmpty())
    {
      final XYCoord dest = rightfulFactories.remove(0);
      Utils.sortLocationsByDistance(dest, startingFactories);
      final XYCoord start = startingFactories.get(0);
      final Commander owner = factoryOwnership.get(start);

      final Unit inf = new Unit(owner, owner.getUnitModel(UnitModel.TROOP));
      inf.x = start.xCoord;
      inf.y = start.yCoord;

      final GamePath infPath = Utils.findShortestPath(inf, dest, map, true);
      if( null == infPath || infPath.getPathLength() < 1 )
        continue; // Can't reach

      int distance = infPath.getFuelCost(inf, map);

      ArrayList<CapStop> chain = new ArrayList<>();
      CapStop build = new CapStop(start);
      build.extraTiles = distance - 13;
      chain.add(build);
      CapStop cap = new CapStop(dest);
      chain.add(cap);
      factoryCapChains.add(chain);
      
      // Now that we're just in cap-chain land, don't worry about whether we start with this factory or not.
      startingFactories.add(dest);
    }
    
    buildBaseCapChains(map, rightfulProps, startingFactories);

    for( XYCoord factoryXYC : capChains.keySet() )
    {
      log(String.format("Cap chains for %s:", factoryXYC));
      for( ArrayList<CapStop> chain : capChains.get(factoryXYC) )
      {
        log(String.format("  chain"));
        for( CapStop stop : chain )
          log(String.format("    %s", stop));
      }
    }

    // Add our factory chains in at the start of each list
    for(ArrayList<CapStop> chain : factoryCapChains)
    {
      XYCoord start = chain.get(0).coord;
      capChains.get(start).add(0, chain);
    }
  }

  public void buildBaseCapChains(GameMap map, ArrayList<XYCoord> rightfulProps, ArrayList<XYCoord> startingFactories)
  {
    // Build initial bits of capChains
    for( XYCoord start : startingFactories )
    {
      ArrayList<ArrayList<CapStop>> chain = new ArrayList<>();
      capChains.put(start, chain);
    }

    boolean madeProgress = true;
    final Unit inf = new Unit(myArmy.cos[0], myArmy.cos[0].getUnitModel(UnitModel.TROOP));
    int infMove = inf.getMovePower(map);
    // Find the next stop or iterate extraTiles on all cap chains
    while (madeProgress && !rightfulProps.isEmpty())
    {
      // Create new cap chains
      for( XYCoord start : startingFactories )
      {
        ArrayList<CapStop> chain = new ArrayList<>();
        CapStop build = new CapStop(start);
        chain.add(build);
        capChains.get(start).add(chain);
      }
      madeProgress = false;
      for( ArrayList<ArrayList<CapStop>> chainList : capChains.values() )
        for( ArrayList<CapStop> chain : chainList )
        {
          if( rightfulProps.isEmpty() )
            break;

          CapStop last = chain.get(chain.size() - 1);
          XYCoord start = last.coord;

          inf.x = start.xCoord;
          inf.y = start.yCoord;
          Utils.sortLocationsByTravelTime(inf, rightfulProps, map);
          XYCoord dest = rightfulProps.get(0);

          final GamePath infPath = Utils.findShortestPath(inf, dest, map, true);
          if( null == infPath || infPath.getPathLength() < 1 )
            continue; // Can't reach
          madeProgress = true; // We have somewhere we can still get to

          int distance = infPath.getFuelCost(inf, map);
          final int currentTotalMove = last.extraTiles + infMove;

          if( distance <= currentTotalMove )
          {
            rightfulProps.remove(dest);
            CapStop cap = new CapStop(dest);
            chain.add(cap);
          }
          else
            last.extraTiles = Math.min(infMove * 2, currentTotalMove);
        }
    }

    // Cull cap chains with no actual caps
    for( ArrayList<ArrayList<CapStop>> chainList : capChains.values() )
      for( int i = 0; i < chainList.size(); )
      {
        ArrayList<CapStop> chain = chainList.get(i);
        if( chain.size() < 2 )
          chainList.remove(i);
        else
          ++i;
      }

    // Sort cap chains by profit
    for(ArrayList<ArrayList<CapStop>> chainList : capChains.values())
      Collections.sort(chainList, new CapStopFundsComparator(infMove));
  }
}
