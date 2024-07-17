package AI;

import java.util.*;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.DeployableCommander;
import Engine.*;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.*;
import Units.*;
import lombok.var;


public class JakeMan extends ModularAI
{
  private static class instantiator implements AIMaker
  {
    private boolean buildCounters   = true;
    private boolean buildMdCounters = true;
    private String name = "JakeMan";
    @Override
    public AIController create(Army army)
    {
      return new JakeMan(army, this);
    }

    @Override
    public String getName()
    {
      return name;
    }

    @Override
    public String getDescription()
    {
      return
          "Builds infantry, tanks, B-Copters, and Md Tanks.\n" +
          "Attacks when he has local force superiority (i.e. takes free dudes)\n" +
          "High-level design by @Lost&Found#6348, ID 824112693123612692.";
    }
  }
  public static final AIMaker info = new instantiator();
  public static final AIMaker oldSchoolCool;
  static
  {
    var oldSchool = new instantiator();
    oldSchool.buildCounters   = true;
    oldSchool.buildMdCounters = false;
    oldSchool.name = "OldSchoolCool";
    oldSchoolCool = oldSchool;
  }
  public final instantiator myInfo;

  @Override
  public AIMaker getAIInfo()
  {
    return myInfo;
  }

  // What % base damage I'll ignore when checking safety
  private static final int    INDIRECT_THREAT_THRESHOLD = 7;
  private static final int    DIRECT_THREAT_THRESHOLD   = 30;
  private static final int    MASSIVE_THREAT_THRESHOLD  = 70;
  // Value to scale the funds damage I deal to something that threatens me
  private static final double FIRSTSTRIKE_ON_THREAT_WEIGHT = 2.0;
  private static final int    STAY_ALIVE_BIAS = 2000;
  private static final int    BIG_THREAT_THRESHOLD = 80; // Enemy health at which I double the expected value of dealing damage
  // Fraction of the unit to remove from the counter-threat power of my unit type if I'm not attacking
  private static final double PEACEFUL_SELF_THREAT_RATIO = 1;
  private static final int    UNIT_HEAL_THRESHOLD = 60; // Health at which units heal
  private static final double UNIT_REFUEL_THRESHOLD = 1.3; // Factor of cost to get to fuel to start worrying about fuel
  private static final double UNIT_REARM_THRESHOLD = 0.25; // Fraction of ammo in any weapon below which to consider resupply

  private Map<UnitModel, Map<XYCoord, Double>> unitMapEnemy;
  private Map<UnitModel, Map<XYCoord, Double>> unitMapFriendly;
  private Map<Commander, ArrayList<Unit>> unitLists;

  ArrayList<UnitModel> allTanks;
  UnitModel infantry, tank, mdTank, antiAir, copter;
  // Order of enemy types to consider building counter units
  ArrayList<UnitModel> counterOrder;
  public static class CounterRatio
  {
    UnitModel counter;
    int power;
    boolean roundTargetPercentUp = false;
    public static final int ROUND_UP_HEALTH = 20;
    public static final int SAVE_BENCHMARK  = 6000; // "Buy one less tank"
    public CounterRatio(UnitModel counter, int power)
    {
      this.counter = counter;
      this.power = power;
    }
    @Override
    public String toString()
    {
      return counter.name + " countering for " + power;
    }
  }
  // For each enemy unit type, my unit types X/Y/Z counter it at this effectiveness percent
  private Map<UnitModel, ArrayList<CounterRatio>> unitTypeToCounterMap;
  // For each enemy unit type, my own counter unit type is negated by X/Y/Z at this ratio
  private Map<UnitModel, Map<UnitModel, ArrayList<CounterRatio>>> counterTypeNegationMap;
  // All unit types that I should track for calculating counterbuilds
  HashSet<UnitModel> allCounterContestants;

  public JakeMan(Army army, instantiator info)
  {
    super(army);
    myInfo = info;

    // look where all vehicles are and what their threat ranges are (yes, mechs are vehicles)
    // take free dudes that you have more defenders for than them
    // move your leftover vehicles so they cover the tiles attacking most relevant stuff (contested cities, own units)
    // move infs as far towards nearest contested stuff as you can without underdefending, cap if possible

    // Comment from L&F/Rize, on the idea of engaging e.g. a Lin COU on a city:
    // IF the dude is free & there's multiple attackers & you can do multiple attacks (since having 2 tanks for 1 tile won't help),
    //   check the total calc after shooting with every firepower thing that can shoot and not the normal one
    // [On the subject of attacking a unit that would be unappealing to hit with one unit (city tank with +stats)...
    //   If I think I have local force superiority, and I can hit with multiple units, evaluate the entire sequence of shots rather than just a single attack.
    //   (since the more hits you can do, the more favorable the overall outcome is liable to be)]

    aiPhases = new ArrayList<AIModule>(
        Arrays.asList(
            new DeployCOUOnTank(army, this),
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
    if( null == capPhase )
      init(gameMap);
    log(String.format("[======== JakeMan initializing turn %s for %s =========]", turnNum, myArmy));
  }

  @Override
  public void endTurn()
  {
    super.endTurn();
    log(String.format("[======== JakeMan ending turn %s for %s =========]", turnNum, myArmy));
  }

  public static class DeployCOUOnTank implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myArmy;
    public final JakeMan ai;

    public DeployCOUOnTank(Army co, JakeMan ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    public boolean checked = false;
    @Override
    public void initTurn(GameMap gameMap) { checked = false; }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap map)
    {
      if( checked )
        return null;

      for( Commander COUer : ai.coParser.deployableCOs )
      {
        ArrayList<DeployableCommander.DeployCOUAction> potentialCOUs = new ArrayList<>();
        for( Unit minion : COUer.units )
        {
          if( minion.isTurnOver )
            continue; // Don't cheat, even if the game lets us
          if( !ai.allTanks.contains(minion.model) )
            continue; // Don't boost non-tanks

          // If we can COU up, add the minion to the list
          final GamePath standStill = new GamePath();
          standStill.addWaypoint(minion.x, minion.y);
          final ArrayList<GameActionSet> unitActions = minion.getPossibleActions(map, standStill);
          for( GameActionSet actionSet : unitActions )
            if( actionSet.getSelected() instanceof DeployableCommander.DeployCOUAction )
            {
              potentialCOUs.add((DeployableCommander.DeployCOUAction) actionSet.getSelected());
              break;
            }
        }

        // There's probably a better way to score this than price...
        int topPrice = 0;
        DeployableCommander.DeployCOUAction bestCOUAction = null;
        for( DeployableCommander.DeployCOUAction action : potentialCOUs )
        {
          final int myPrice = COUer.getCost(action.getActor().model);
          if( topPrice < myPrice )
          {
            topPrice = myPrice;
            bestCOUAction = action;
          }
        }

        return bestCOUAction;
      }

      // Only flag ourselves as "checked" if we've run out of COUs to deploy
      checked = true;
      return null;
    }
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
      ai.unitLists = AIUtils.getEnemyUnitsByCommander(null, gameMap);
      for( Commander co : ai.unitLists.keySet() )
      {
        Map<UnitModel, Map<XYCoord, Double>> mapToFill;
        if( myArmy.isEnemy(co) )
          mapToFill = ai.unitMapEnemy;
        else
          mapToFill = ai.unitMapFriendly;

        for( Unit threat : ai.unitLists.get(co) )
        {
          // add each new threat to the existing threats
          final UnitModel um = threat.model;
          if( !mapToFill.containsKey(um) )
            mapToFill.put(um, new HashMap<>());
          Map<XYCoord, Double> threatArea = mapToFill.get(um);
          double newValue = (double)(threat.getHealth()) / UnitModel.MAXIMUM_HEALTH;
          // Square unit fraction so low-HP units aren't valued so much
          newValue *= newValue;
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
      PathCalcParams pcp = new PathCalcParams(unit, gameMap);
      pcp.includeOccupiedSpaces = includeOccupiedSpaces;
      ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths();
      if( mustMove )
        destinations.remove(new XYCoord(unit.x, unit.y));
      destinations.removeAll(AIUtils.findAlliedIndustries(gameMap, co.army, destinations, !avoidProduction));
      // sort by furthest away, good for capturing
      Utils.sortLocationsByDistance(position, destinations);
      Collections.reverse(destinations);
      ArrayList<GameAction> freeDudeShots = new ArrayList<>();

      for( Utils.SearchNode moveCoord : destinations )
      {
        // Figure out how to get here.
        GamePath movePath = moveCoord.getMyPath();

        // Figure out what I can do here.
        ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath, includeOccupiedSpaces);
        for( GameActionSet actionSet : actionSets )
        {
          Unit resident = gameMap.getLocation(moveCoord).getResident();
          boolean spaceFree = null == resident;
          if( !spaceFree )//&& ((unit.CO != resident.CO || resident.isTurnOver)) )
            continue; // Bail if we can't clear the space
          final GameAction ga = actionSet.getSelected();
          if( ga.getType() == UnitActionFactory.WAIT )
            continue;

          final boolean amAttacking = true;
          if( ai.isDudeFree(gameMap, unit, moveCoord, amAttacking) )
          {
            if( ga.getType() == UnitActionFactory.CAPTURE )
            {
              return ga;
            }
            if( ga.getType() == UnitActionFactory.ATTACK )
            {
              freeDudeShots.add(ga);
            }
          }
        }
      }

      if( !freeDudeShots.isEmpty() )
      {
        GameAction bestShot = findBestAttack(gameMap, unit, freeDudeShots);
        return bestShot;
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
      return ai.findTravelAction(gameMap, unit, false, avoidProduction);
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
          boolean avoidProduction = true;
          GameAction eviction = null;
          if( resident.CO.army == myArmy && !resident.isTurnOver )
            eviction = ai.evictUnit(gameMap, null, resident, avoidProduction);
          if( null != eviction )
            return eviction;
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
    uc.calculateActionTypes();
    ArrayList<XYCoord> goals = new ArrayList<XYCoord>();

    ArrayList<XYCoord> stations = AIUtils.findRepairDepots(unit);
    Utils.sortLocationsByTravelTime(unit, stations, gameMap);

    boolean shouldResupply = false;
    GamePath toClosestStation = null;
    boolean canResupply = stations.size() > 0;
    if( canResupply )
    {
      toClosestStation = new PathCalcParams(unit, gameMap).setTheoretical().findShortestPath(stations.get(0));
      canResupply &= null != toClosestStation;
    }
    if( canResupply )
    {
      shouldResupply = unit.getHealth() <= UNIT_HEAL_THRESHOLD;
      shouldResupply |= unit.fuel <= UNIT_REFUEL_THRESHOLD
          * toClosestStation.getMoveCost(unit, gameMap);
      shouldResupply |= unit.ammo >= 0 && unit.ammo <= unit.model.maxAmmo * UNIT_REARM_THRESHOLD;
    }

    if( shouldResupply )
    {
      log(String.format("  %s needs supplies.", unit.toStringWithLocation()));
      goals.addAll(stations);
      if( avoidProduction )
        goals.removeAll(AIUtils.findAlliedIndustries(gameMap, myArmy, goals, !avoidProduction));
    }
    else if( uc.actionTypes.contains(UnitActionFactory.CAPTURE) )
    {
      for( XYCoord xyc : futureCapTargets )
        if( !AIUtils.isCapturing(gameMap, myArmy.cos[0], xyc) )
          goals.add(xyc);
    }
    else if( uc.actionTypes.contains(UnitActionFactory.ATTACK) )
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
        GamePath path = new PathCalcParams(unit, gameMap).setTheoretical().findShortestPath(targetCoord);
        if (path != null &&
            isWeakTo(target.model, unit.model))
        {
          valueMap.put(model, (double)target.getCost());
          if (!targetMap.containsKey(model)) targetMap.put(model, new ArrayList<XYCoord>());
          targetMap.get(model).add(targetCoord);
        }
      }

      // Sort all individual target lists by distance
      for (ArrayList<XYCoord> targetList : targetMap.values())
      {
        Utils.sortLocationsByTravelTime(unit, targetList, gameMap);
        goals.add(targetList.get(0));
      }
    }

    if( goals.isEmpty() ) // Send 'em at production facilities if they haven't got anything better to do
    {
      for( XYCoord coord : futureCapTargets )
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
    {
      goals.addAll(myArmy.HQLocations);
      System.out.println("Warning: JakeMan has no goals for " + unit.toStringWithLocation());
    }

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
      result = findTravelAction(gameMap, unit, mustMove, avoidProduction);
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
                        boolean mustMove,
                        boolean avoidProduction )
  {
    // Find the possible destinations.
    boolean ignoreResident = true;
    PathCalcParams pcp = new PathCalcParams(unit, gameMap);
    pcp.includeOccupiedSpaces = ignoreResident;
    ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths();
    destinations.removeAll(AIUtils.findAlliedIndustries(gameMap, myArmy, destinations, !avoidProduction));

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
      path = new PathCalcParams(unit, gameMap).setTheoretical().findShortestPath(target);
      if( null != path ) // We can reach it.
      {
        goal = target;
        break;
      }
    }
    // If we have to move and have no destinations, make the start tile the goal
    if( mustMove && null == goal )
    {
      goal = new XYCoord(unit);
      path = GamePath.stayPut(unit);
    }

    if( null == goal ) return null;

    // Choose the point on the path just out of our range as our 'goal', and try to move there.
    // This will allow us to navigate around large obstacles that require us to move away
    // from our intended long-term goal.
    path.snip(unit.getMovePower(gameMap) + 2); // Trim the path approximately down to size.
    XYCoord pathPoint = path.getEndCoord(); // Set the last location as our goal.

    // Sort my currently-reachable move locations by distance from the goal,
    // and build a GameAction to move to the closest one.
    Utils.sortLocationsByDistance(pathPoint, destinations);
    log(String.format("  %s is traveling toward %s at %s via %s  mustMove?: %s",
                          unit.toStringWithLocation(),
                          gameMap.getLocation(goal).getEnvironment().terrainType, goal,
                          pathPoint, mustMove));
    for( Utils.SearchNode xyc : destinations )
    {
      log(String.format("    is it safe to go to %s?", xyc));
      if( !isDudeFree(gameMap, unit, xyc, false) )
        continue;

      GameAction action = null;
      Unit resident = gameMap.getLocation(xyc).getResident();
      if( null != resident && unit != resident )
      {
        if( unit.CO == resident.CO && !resident.isTurnOver )
          action = evictUnit(gameMap, unit, resident, avoidProduction);
        if( null != action ) return action;
        continue;
      }
      log(String.format("    Yes"));

      GamePath movePath = xyc.getMyPath();
      ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath, ignoreResident);
      if( actionSets.size() > 0 )
      {
        // Since we're moving anyway, might as well try shooting the scenery
        for( GameActionSet actionSet : actionSets )
        {
          if( actionSet.getSelected().getType() == UnitActionFactory.ATTACK )
          {
            action = findBestAttack(gameMap, unit, actionSet.getGameActions());
          }
        }

        if( null == action && movePath.getPathLength() > 1) // Just wait if we can't do anything cool
          action = new WaitLifecycle.WaitAction(unit, movePath);
        return action;
      }
    }
    return null;
  }

  public static GameAction findBestAttack(GameMap gameMap, Unit unit, ArrayList<GameAction> actionSet)
  {
    double bestDamage = 0;
    GameAction bestAttack = null;
    for( GameAction attack : actionSet )
    {
      double damageValue = AICombatUtils.scoreAttackAction(unit, attack, gameMap,
          (results) -> {
            final Unit defender = results.defender.unit;
            int loss   = Math.min(unit    .getHealth(), results.attacker.getPreciseHealthDamage());
            int damage = Math.min(defender.getHealth(), results.defender.getPreciseHealthDamage());

            // Convert to abstract value
            int extraLoss = 0;
            if( loss >= unit.getHealth() )
              extraLoss += STAY_ALIVE_BIAS;
            loss *= unit.getCost();
            loss /= UnitModel.MAXIMUM_HEALTH;
            loss += extraLoss;

            damage *= defender.getCost();
            damage /= UnitModel.MAXIMUM_HEALTH;
            if( isThreatenedBy(unit.model, defender.model) )
              damage *= FIRSTSTRIKE_ON_THREAT_WEIGHT;
            // Value damage to hurt units less
            if( defender.getHealth() < BIG_THREAT_THRESHOLD )
              damage /= 1.5;

            return damage - loss;
          }, (terrain, params) -> 0); // Don't attack terrain

      if( damageValue > bestDamage )
      {
        bestDamage = damageValue;
        bestAttack = attack;
      }
    }
    return bestAttack;
  }

  private static boolean isThreatenedBy(UnitModel um, UnitModel threat)
  {
    int threshhold = um.hasDirectFireWeapon() ? DIRECT_THREAT_THRESHOLD : INDIRECT_THREAT_THRESHOLD;
    boolean isThreat = false;
    for( WeaponModel wm : threat.weapons )
      isThreat |= threshhold <= wm.getDamage(um);
    return isThreat;
  }

  private static boolean isWeakTo(UnitModel um, UnitModel threat)
  {
    boolean isWeak = isThreatenedBy(um, threat);
    isWeak &= !isThreatenedBy(threat, um);
    return isWeak;
  }

  private boolean isDudeFree(GameMap gameMap, Unit unit, XYCoord xyc, boolean amAttacking)
  {
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
        if( !isThreatenedBy(threat, counter) )
          continue;
        double counterPowerTotal = 0;
        final boolean counterIsMeAndIAmPeaceful = !amAttacking && (counter == unit.model);
        for( XYCoord coord : counterCoords )
        {
          double counterPower = 0;
          if( unitMapFriendly.get(counter).containsKey(coord) )
            counterPower = unitMapFriendly.get(counter).get(coord);
          if( counterIsMeAndIAmPeaceful )
            counterPower -= (PEACEFUL_SELF_THREAT_RATIO * unit.getHP()) / 10;
          counterPowerTotal += Math.max(0, counterPower);
        }
        final double counterPowerAverage = counterPowerTotal / counterCoords.size();
        final double threatPower = threatCounts.get(threat);
        if( counterPowerAverage >= threatPower )
        {
          threatCounts.remove(threat);
          break;
        }
        else
          threatCounts.put(threat, threatPower - counterPowerAverage);
      }
    // If there are no threats we can't handle, dude is free.
    if( threatCounts.size() < 1 )
      return true;

    // Count dude as free if:
    //   we get 3+ terrain stars
    //   the threat surplus is small
    //   that threat is of the same unit type
    final int defLevel = gameMap.getEnvironment(xyc).terrainType.getDefLevel();
    if( defLevel < 3 || unit.model.isAirUnit() )
      return false;
    if( threatCounts.size() > 1 )
      return false;
    if( !threatCounts.containsKey(unit.model) )
      return false; // Our one threat is not same-type
    if( threatCounts.get(unit.model) < 1.3 )
      return true;
    return false;
  }

  // Build an Md if the enemy has 3+ more ground vehicles within 2 tank moves of your base
  // ...but don't if there are 2+ Mds in the area
  private static class FactoryThreatState
  {
    public final JakeMan ai;
    public final XYCoord coord;
    public final ArrayList<Utils.SearchNode> checkTiles;
    public int niceMdCount = 0, meanVehCount = 0, niceVehCount = 0;
    public FactoryThreatState(JakeMan ai, XYCoord coord, GameMap map, UnitContext theVeh)
    {
      this.coord = coord;
      this.ai    = ai;
      theVeh.setCoord(coord);
      PathCalcParams pcp = new PathCalcParams(theVeh, map);
      pcp.initialMovePower *= 2;
      pcp.canTravelThroughEnemies = true;
      checkTiles = pcp.findAllPaths();
    }
    public void trackUnit(Unit threat)
    {
      if( !checkTiles.contains(new XYCoord(threat)) )
        return;
      if( ai.myArmy.isEnemy(threat.CO) )
      {
        if( threat.model.isAny(UnitModel.TANK) )
          ++meanVehCount;
      }
      else
      {
        if( threat.model.isAny(UnitModel.TANK) )
          ++niceVehCount;
        if( ai.mdTank == threat.model )
          ++niceMdCount;
      }
    }
    public boolean shouldBuildMd()
    {
      if( !ai.myInfo.buildMdCounters )
        return false;
      if( niceMdCount > 1 )
        return false;
      int vehDiff = meanVehCount - niceVehCount;
      return vehDiff > 2;
    }
    @Override
    public String toString()
    {
      return "FTS for " + coord;
    }
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
    int budget = myArmy.money;

    // Fill out production with inf first, to trim the budget
    ArrayList<MapLocation> infBases = CPI.getAllFacilitiesFor(infantry);
    for( MapLocation loc : infBases )
    {
      Commander buyer = loc.getOwner();
      final XYCoord coord = loc.getCoordinates();
      final int cost = buyer.getBuyCost(infantry, coord);

      if( cost <= budget )
      {
        builds.put(coord, infantry);
        budget -= cost;
      }
      else
        budget = -1;
    }
    if( budget < 3000 ) // arbitrary
      return builds;
    log("Evaluating Production needs");

    ArrayList<FactoryThreatState> potentialMdBuilds = new ArrayList<>();
    ArrayList<MapLocation> mdBases = CPI.getAllFacilitiesFor(mdTank);
    UnitContext theVeh = new UnitContext(myArmy.cos[0], tank); // for getting the zone of concern
    for( MapLocation loc : mdBases )
      potentialMdBuilds.add(new FactoryThreatState(this, loc.getCoordinates(), gameMap, theVeh));

    // "Net health" of the units I might want to build counters for, or use as counters.
    HashMap<UnitModel, Integer> niceHealth = new HashMap<>();
    HashMap<UnitModel, Integer> meanHealth = new HashMap<>();
    for( Commander co : unitLists.keySet() )
    {
      HashMap<UnitModel, Integer> mapToFill;
      if( myArmy.isEnemy(co) )
        mapToFill = meanHealth;
      else
        mapToFill = niceHealth;

      for( Unit threat : unitLists.get(co) )
      {
        for( var fts : potentialMdBuilds )
          fts.trackUnit(threat);
        final UnitModel um = threat.model;
        if( !allCounterContestants.contains(um) )
          continue;
        int oldVal = mapToFill.getOrDefault(um, 0);
        int newVal = oldVal + threat.getHealth();
        mapToFill.put(um, newVal);
      }
    }

    boolean shouldSave = true;
    for( var fts : potentialMdBuilds )
    {
      if( !fts.shouldBuildMd() )
        continue;
      MapLocation loc = gameMap.getLocation(fts.coord);
      Commander buyer = loc.getOwner();
      final XYCoord coord = loc.getCoordinates();
      final int cost = buyer.getBuyCost(mdTank, coord);

      int marginalCost = cost;
      if( builds.containsKey(coord) )
        marginalCost -= buyer.getBuyCost(builds.get(coord), coord);

      if( marginalCost <= budget )
      {
        builds.put(coord, mdTank);
        budget -= marginalCost;
        shouldSave = false; // If we've already built some counter units, don't worry about saving for more
        log(String.format("Building MD to protect %s", fts.coord));
      }
    }

    boolean counterNeeded = false;
    // If I need to do a counterbuild, either:
    //   add it to the list before the normal builds
    //   reduce my budget this turn to save up
    for( var threatType : meanHealth.keySet() )
    {
      if( !unitTypeToCounterMap.containsKey(threatType) )
        continue; // This isn't on our list of things to counter (e.g. AA)
      int remainingHealth = meanHealth.get(threatType);
      ArrayList<CounterRatio> counters = unitTypeToCounterMap.get(threatType);

      // Chip down remainingHealth based on unit totals.
      for( var ratio : counters )
      {
        if( !niceHealth.containsKey(ratio.counter) )
          continue;
        int counterHealth = niceHealth.get(ratio.counter);
        if( counterTypeNegationMap.containsKey(threatType) )
        {
          var negationRatios = counterTypeNegationMap.get(threatType).getOrDefault(ratio.counter, new ArrayList<>());
          for( var ccRatio : negationRatios )
          {
            if( !meanHealth.containsKey(ccRatio.counter) )
              continue;
            int ccHealth = meanHealth.get(ccRatio.counter);
            int ccPower  = ccHealth * ccRatio.power / UnitModel.MAXIMUM_HEALTH;
            counterHealth -= ccPower;
          }
        }
        int counterPower  = counterHealth * ratio.power / UnitModel.MAXIMUM_HEALTH;
        remainingHealth  -= counterPower;
        if( ratio.roundTargetPercentUp )
        {
          int roundable = remainingHealth % UnitModel.MAXIMUM_HEALTH;
          if( roundable >= CounterRatio.ROUND_UP_HEALTH )
            remainingHealth += UnitModel.MAXIMUM_HEALTH - roundable;
        }
      }
      if( remainingHealth < UnitModel.MAXIMUM_HEALTH )
        continue; // We consider it fully-countered
      counterNeeded = true;

      // Chip down remainingHealth with new builds.
      for( var ratio : counters )
      {
        ArrayList<MapLocation> facilities = CPI.getAllFacilitiesFor(ratio.counter);
        for( MapLocation loc : facilities )
        {
          Commander buyer = loc.getOwner();
          final XYCoord coord = loc.getCoordinates();
          final int cost = buyer.getBuyCost(ratio.counter, coord);

          int marginalCost = cost;
          if( builds.containsKey(coord) )
            marginalCost -= buyer.getBuyCost(builds.get(coord), coord);

          if( marginalCost <= budget )
          {
            boolean safeToBuild = true; // Don't build counter units in range of units they're squishy to.
            for( UnitModel threat : unitMapEnemy.keySet() )
            {
              if( unitMapEnemy.get(threat).containsKey(coord) )
              {
                double power = unitMapEnemy.get(threat).get(coord);
                UnitContext tc = new UnitContext(myArmy.cos[0], threat);
                tc.chooseWeapon(ratio.counter);
                if( null == tc.weapon )
                  continue;
                int damage = (int) (tc.weapon.getDamage(ratio.counter) * power);
                if( damage > MASSIVE_THREAT_THRESHOLD )
                {
                  safeToBuild = false;
                  break;
                }
              }
            }
            if( !safeToBuild )
              continue;

            builds.put(coord, ratio.counter);
            budget -= marginalCost;
            shouldSave = false; // If we've already built some counter units, don't worry about saving for more
            log(String.format("Building %s to counter %s health of %s",
                ratio.counter, remainingHealth, threatType));
            remainingHealth -= ratio.power;
          }
          if( remainingHealth < UnitModel.MAXIMUM_HEALTH )
            break; // We consider it fully-countered
        }
        if( remainingHealth < UnitModel.MAXIMUM_HEALTH )
          break; // We consider it fully-countered
      }
    }

    // If we can't counter all of the threats and don't have enough to buy counters, save money
    if( counterNeeded && shouldSave )
      budget -= CounterRatio.SAVE_BENCHMARK;

    if( budget < 3000 ) // arbitrary
      return builds;

    ArrayList<UnitModel> wantedTypes = new ArrayList<>();
    wantedTypes.add(infantry);
    wantedTypes.add(tank);
    wantedTypes.add(copter);
    wantedTypes.add(mdTank);

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
        {
          UnitModel currentBuild = builds.get(coord);
          if( !wantedTypes.contains(currentBuild) )
            continue; // If it's not a standard build, don't override
          if( um.costBase < currentBuild.costBase )
            continue; // If it's a standard build that's more expensive, it's a counter unit
          marginalCost -= buyer.getBuyCost(currentBuild, coord);
        }

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
            if( tank == builds.get(tankCoord) )
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

  private void init(GameMap map)
  {
    infantry = myArmy.cos[0].getUnitModel(UnitModel.TROOP);
    allTanks = myArmy.cos[0].getAllModels(UnitModel.ASSAULT);
    tank     = allTanks.get(0);
    mdTank   = allTanks.get(1);
    antiAir  = myArmy.cos[0].getUnitModel(UnitModel.SURFACE_TO_AIR);
    copter   = myArmy.cos[0].getUnitModel(UnitModel.ASSAULT | UnitModel.AIR_LOW, false);

    counterBuildSetup();

    allCounterContestants = new HashSet<>();
    for( UnitModel counterable : unitTypeToCounterMap.keySet() )
    {
      allCounterContestants.add(counterable);
      for( CounterRatio ratio : unitTypeToCounterMap.get(counterable) )
        allCounterContestants.add(ratio.counter);
    }

    if( null == copter ) // I clearly don't understand this unit set, so just grab something to hedge
      copter = myArmy.cos[0].getUnitModel(UnitModel.AIR_TO_AIR, false);

    capPhase = new CapPhaseAnalyzer(map, myArmy);
  }

  private void counterBuildSetup()
  {
    counterOrder = new ArrayList<>();
    unitTypeToCounterMap = new HashMap<>();
    counterTypeNegationMap = new HashMap<>(); // Note: we assume all UnitModels referenced in here are in the above
    if( !myInfo.buildCounters )
      return;

    // tanks: no need to calc them beyond the Md clause
    // copters: 1.5 copters or 1AA per
    if( null != copter )
    {
      counterOrder.add(copter);
      unitTypeToCounterMap.put(copter, new ArrayList<>());
      var copterCounters = unitTypeToCounterMap.get(copter);
      CounterRatio copterCounterCopter = new CounterRatio(copter, 200/3);
      copterCounterCopter.roundTargetPercentUp = true; // I don't consider a single existing copter a full counter to an enemy copter
      copterCounters.add(copterCounterCopter);
      copterCounters.add(new CounterRatio(antiAir, 100));
    }

    // Md: 1 neo per 1.5 Mds? Ignore if you already have a bomber
    counterOrder.add(mdTank);
    unitTypeToCounterMap.put(mdTank, new ArrayList<>());
    var mdCounters = unitTypeToCounterMap.get(mdTank);
    var bomber = myArmy.cos[0].getUnitModel(UnitModel.AIR_TO_SURFACE | UnitModel.JET, false);
    if( null != bomber )
      mdCounters.add(new CounterRatio(bomber, 250));
    var neoTank = allTanks.get(2);
    if( null != neoTank )
      mdCounters.add(new CounterRatio(neoTank, 150));

    // 1 fighter per 1.7 (yes) bombers
    //   (trunctate after the multiplicarion, so you get one in response to one bomber and a second in response to the third),
    var fighter = myArmy.cos[0].getUnitModel(UnitModel.AIR_TO_AIR | UnitModel.JET, false);
    if( null != bomber )
    {
      counterOrder.add(bomber);
      unitTypeToCounterMap.put(bomber, new ArrayList<>());
      var bomberCounters = unitTypeToCounterMap.get(bomber);
      if( null != fighter )
        bomberCounters.add(new CounterRatio(fighter, 170));
      bomberCounters.add(new CounterRatio(antiAir, 50));

      // or 2 non-copter-calc-involved AAs per bomber
      if( null != copter )
      {
        counterTypeNegationMap.put(bomber, new HashMap<>());
        var bomberCounterNegators = counterTypeNegationMap.get(bomber);
        bomberCounterNegators.put(antiAir, new ArrayList<>());
        var aaCounterCounters = bomberCounterNegators.get(antiAir);
        aaCounterCounters.add(new CounterRatio(copter, 100));
      }
    }

    // stealth: have 1 healthy fighter on the board if a stealth is present
    var stealths = myArmy.cos[0].getAllModels(UnitModel.AIR_TO_SURFACE | UnitModel.AIR_TO_AIR | UnitModel.JET, false);
    boolean stealthIsStealth = false;
    for( var s : stealths )
      if( s.hidden )
      {
        for( var actionType : s.baseActions )
          if( actionType instanceof TransformLifecycle.TransformFactory )
          {
            stealthIsStealth = true;
            break;
          }
        if( stealthIsStealth )
          break;
      }
    if( stealthIsStealth && null != fighter )
      for( var s : stealths )
      {
        counterOrder.add(s);
        unitTypeToCounterMap.put(s, new ArrayList<>());
        var stealthCounters = unitTypeToCounterMap.get(s);
        CounterRatio fighterCounterStealth = new CounterRatio(fighter, 100);
        fighterCounterStealth.roundTargetPercentUp = true; // Fighter needs to be healthy to deal
        stealthCounters.add(fighterCounterStealth);
      }

    // fighter: 2 AA per, AAs built for other air units included this time
    if( null != fighter )
    {
      counterOrder.add(fighter);
      unitTypeToCounterMap.put(fighter, new ArrayList<>());
      var fighterCounters = unitTypeToCounterMap.get(fighter);
      CounterRatio aaVSfighter = new CounterRatio(antiAir, 50);
      aaVSfighter.roundTargetPercentUp = true; // 2 whole AA per fighter
      fighterCounters.add(aaVSfighter);
    }
  }

}
