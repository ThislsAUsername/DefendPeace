package AI;

import java.util.*;
import java.util.Map.Entry;

import AI.CommanderProductionInfo;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.*;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.*;
import Units.*;
import Units.MoveTypes.MoveType;

/**
 *  Wally values units based on firepower and the area they can threaten.
 *  He tries to keep units safe by keeping them out of range, but will also meatshield to protect more valuable units.
 */
public class WallyAI extends ModularAI
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Commander co)
    {
      return new WallyAI(co);
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

  // What % damage I'll ignore when checking safety
  private static final int INDIRECT_THREAT_THRESHHOLD = 7;
  private static final int DIRECT_THREAT_THRESHHOLD = 13;
  private static final int    UNIT_HEAL_THRESHHOLD = 6; // HP at which units heal
  private static final double UNIT_REFUEL_THRESHHOLD = 1.3; // Factor of cost to get to fuel to start worrying about fuel
  private static final double UNIT_REARM_THRESHHOLD = 0.25; // Fraction of ammo in any weapon below which to consider resupply
  private static final double AGGRO_EFFECT_THRESHHOLD = 0.42; // How effective do I need to be against a unit to target it?
  private static final double AGGRO_FUNDS_WEIGHT = 0.9; // Multiplier on damage I need to get before a sacrifice is worth it
  private static final double RANGE_WEIGHT = 1; // Exponent for how powerful range is considered to be
  private static final double TERRAIN_PENALTY_WEIGHT = 3; // Exponent for how crippling we think high move costs are
  private static final double MIN_SIEGE_RANGE_WEIGHT = 0.8; // Exponent for how much to penalize siege weapon ranges for their min ranges

  private static final double TERRAIN_FUNDS_WEIGHT = 2.5; // Multiplier for per-city income for adding value to units threatening to cap
  private static final double TERRAIN_INDUSTRY_WEIGHT = 20000; // Funds amount added to units threatening to cap an industry
  private static final double TERRAIN_HQ_WEIGHT = 42000; //                  "                                      HQ
  
  private Map<UnitModel, Map<XYCoord, Double>> threatMap;
  private ArrayList<Unit> allThreats;
  private HashMap<UnitModel, Double> unitEffectiveMove = null; // How well the unit can move, on average, on this map
  public double getEffectiveMove(UnitModel model)
  {
    if( unitEffectiveMove.containsKey(model) )
      return unitEffectiveMove.get(model);

    MoveType p = model.propulsion;
    GameMap map = myCo.myView;
    double totalCosts = 0;
    int validTiles = 0;
    double totalTiles = map.mapWidth * map.mapHeight; // to avoid integer division
    // Iterate through the map, counting up the move costs of all valid terrain
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        Environment terrain = map.getLocation(w, h).getEnvironment();
        if( p.canTraverse(terrain) )
        {
          validTiles++;
          int cost = p.getMoveCost(terrain);
          totalCosts += Math.pow(cost, TERRAIN_PENALTY_WEIGHT);
        }
      }
    }
    //             term for how fast you are   term for map coverage
    double ratio = (validTiles / totalCosts) * (validTiles / totalTiles); // 1.0 is the max expected value
    
    double effMove = model.movePower * ratio;
    unitEffectiveMove.put(model, effMove);
    return effMove;
  }


  public WallyAI(Commander co)
  {
    super(co);
    aiPhases = new ArrayList<AIModule>(
        Arrays.asList(
            new PowerActivator(co, CommanderAbility.PHASE_TURN_START),
            new GenerateThreatMap(co, this), // FreeRealEstate and Travel need this, and NHitKO/building do too because of eviction
            new CaptureFinisher(co, this),

            new NHitKO(co, this),
            new SiegeAttacks(co, this),
            new PowerActivator(co, CommanderAbility.PHASE_BUY),
            new FreeRealEstate(co, this, false, false), // prioritize non-eviction
            new FreeRealEstate(co, this, true,  false), // evict if necessary
            new BuildStuff(co, this),
            new FreeRealEstate(co, this, true,  true), // step on industries we're not using
            new Travel(co, this),

            new PowerActivator(co, CommanderAbility.PHASE_TURN_END)
            ));
  }

  private void init(GameMap map)
  {
    unitEffectiveMove = new HashMap<UnitModel, Double>();
    // init all move multipliers before powers come into play
    for( Commander co : map.commanders )
    {
      for( UnitModel model : co.unitModels )
      {
        getEffectiveMove(model);
      }
    }
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    super.initTurn(gameMap);
    if( null == unitEffectiveMove )
      init(gameMap);
    log(String.format("[======== Wally initializing turn %s for %s =========]", turnNum, myCo));
  }

  @Override
  public void endTurn()
  {
    super.endTurn();
    log(String.format("[======== Wally ending turn %s for %s =========]", turnNum, myCo));
  }

  public static class SiegeAttacks extends UnitActionFinder
  {
    private static final long serialVersionUID = 1L;
    public SiegeAttacks(Commander co, ModularAI ai)
    {
      super(co, ai);
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap gameMap)
    {
      GameAction bestAttack = null;
      // Find the possible destination.
      XYCoord coord = new XYCoord(unit.x, unit.y);

      if( AIUtils.isFriendlyProduction(gameMap, myCo, coord) || !unit.model.hasImmobileWeapon() )
        return bestAttack;

      // Figure out how to get here.
      GamePath movePath = Utils.findShortestPath(unit, coord, gameMap);

      // Figure out what I can do here.
      ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath);
      double bestDamage = 0;
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
            double damage = valueUnit(target, loc, false) * Math.min(target.getHP(), CombatEngine.simulateBattleResults(unit, target, gameMap, unit.x, unit.y).defenderHPLoss);
            if( damage > bestDamage )
            {
              bestDamage = damage;
              bestAttack = action;
            }
          }
        }
      }
      if( null != bestAttack )
      {
        ai.log(String.format("%s is shooting %s",
            unit.toStringWithLocation(), gameMap.getLocation(bestAttack.getTargetLocation()).getResident()));
      }
      return bestAttack;
    }
  }

  // Try to get confirmed kills with mobile strikes.
  public static class NHitKO implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Commander myCo;
    public final WallyAI ai;

    public NHitKO(Commander co, WallyAI ai)
    {
      myCo = co;
      this.ai = ai;
    }

    @Override
    public void initTurn(GameMap gameMap) {targets = null;}
    HashSet<XYCoord> targets = null;

    XYCoord targetLoc;
    Map<XYCoord, Unit> neededAttacks;
    double damageSum = 0;

    public void reset()
    {
      if( null != targets )
        targets.remove(targetLoc);
      targetLoc = null;
      neededAttacks = null;
      damageSum = 0;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      GameAction nextAction = nextAttack(gameMap);
      if( null != nextAction )
        return nextAction;

      HashSet<XYCoord> industries = new HashSet<XYCoord>();
      for( XYCoord coord : myCo.ownedProperties )
        if( myCo.unitProductionByTerrain.containsKey(gameMap.getEnvironment(coord).terrainType)
            || TerrainType.HEADQUARTERS == gameMap.getEnvironment(coord).terrainType
            || TerrainType.LAB == gameMap.getEnvironment(coord).terrainType )
          industries.add(coord);

      // Initialize to targeting all spaces on or next to industries+HQ, since those are important spots
      if( null == targets )
      {
        targets = new HashSet<XYCoord>();

        HashSet<XYCoord> industryBlockers = new HashSet<XYCoord>();
        for( XYCoord coord : industries )
          industryBlockers.addAll(Utils.findLocationsInRange(gameMap, coord, 0, 1));

        for( XYCoord coord : industryBlockers )
        {
          Unit resident = gameMap.getResident(coord);
          if( null != resident && myCo.isEnemy(resident.CO) )
            targets.add(coord);
        }
      }

      for( XYCoord coord : new ArrayList<XYCoord>(targets) )
      {
        Unit resident = gameMap.getResident(coord);
        if( null != resident && myCo.isEnemy(resident.CO) )
        {
          targetLoc = coord;
          neededAttacks = AICombatUtils.findMultiHitKill(gameMap, resident, unitQueue, industries);
          if( null != neededAttacks )
            break;
        }
        else
          targets.remove(coord);
      }

      return nextAttack(gameMap);
    }

    private GameAction nextAttack(GameMap gameMap)
    {
      if( null == targetLoc || null == neededAttacks )
        return null;

      Unit target = gameMap.getLocation(targetLoc).getResident();
      if( null == target )
      {
        ai.log(String.format("    NHitKO target is ded. Ayy."));
        reset();
        return null;
      }

      for( XYCoord xyc : neededAttacks.keySet() )
      {
        Unit unit = neededAttacks.get(xyc);
        if( unit.isTurnOver || !gameMap.isLocationEmpty(unit, xyc) )
          continue;

        damageSum += CombatEngine.simulateBattleResults(unit, target, gameMap, xyc.xCoord, xyc.yCoord).defenderHPLoss;
        ai.log(String.format("    %s brings the damage total to %s", unit.toStringWithLocation(), damageSum));
        return new BattleLifecycle.BattleAction(gameMap, unit, Utils.findShortestPath(unit, xyc, gameMap), target.x, target.y);
      }
      // If we're here, we're either done or we need to clear out friendly blockers
      for( XYCoord xyc : neededAttacks.keySet() )
      {
        Unit resident = gameMap.getResident(xyc);
        if( null == resident || resident.isTurnOver || resident.CO != myCo )
          continue;

        boolean ignoreSafety = true, avoidProduction = true;
        return ai.evictUnit(gameMap, ai.allThreats, ai.threatMap, neededAttacks.get(xyc), resident, ignoreSafety, avoidProduction);
      }
      ai.log(String.format("    NHitKO ran out of attacks to do"));
      reset();
      return null;
    }
  }

  public static class GenerateThreatMap implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Commander myCo;
    public final WallyAI ai;

    public GenerateThreatMap(Commander co, WallyAI ai)
    {
      myCo = co;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      ai.allThreats = new ArrayList<Unit>();
      ai.threatMap = new HashMap<UnitModel, Map<XYCoord, Double>>();
      Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myCo, gameMap);
      for( UnitModel um : myCo.unitModels )
      {
        ai.threatMap.put(um, new HashMap<XYCoord, Double>());
        for( Commander co : unitLists.keySet() )
        {
          if( myCo.isEnemy(co) )
          {
            for( Unit threat : unitLists.get(co) )
            {
              // add each new threat to the existing threats
              ai.allThreats.add(threat);
              Map<XYCoord, Double> threatArea = ai.threatMap.get(um);
              for( Entry<XYCoord, Double> newThreat : AICombatUtils.findThreatPower(gameMap, threat, um).entrySet() )
              {
                if( null == threatArea.get(newThreat.getKey()) )
                  threatArea.put(newThreat.getKey(), newThreat.getValue());
                else
                  threatArea.put(newThreat.getKey(), newThreat.getValue() + threatArea.get(newThreat.getKey()));
              }
            }
          }
        }
      }

      return null;
    }
  }

  // Try to get unit value by capture or attack
  public static class FreeRealEstate extends UnitActionFinder
  {
    private static final long serialVersionUID = 1L;
    private final WallyAI ai;
    private final boolean canEvict, canStepOnProduction;
    public FreeRealEstate(Commander co, WallyAI ai, boolean canEvict, boolean canStepOnProduction)
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
      return findValueAction(myCo, ai, unit, gameMap, mustMove, !canStepOnProduction, canEvict);
    }

    public static GameAction findValueAction( Commander co, WallyAI ai,
                                              Unit unit, GameMap gameMap,
                                              boolean mustMove, boolean avoidProduction,
                                              boolean canEvict )
    {
      XYCoord position = new XYCoord(unit.x, unit.y);
      MapLocation unitLoc = gameMap.getLocation(position);

      boolean includeOccupiedSpaces = true; // Since we know how to shift friendly units out of the way
      ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeOccupiedSpaces);
      if( mustMove )
        destinations.remove(new XYCoord(unit.x, unit.y));
      destinations.removeAll(AIUtils.findAlliedIndustries(gameMap, co, destinations, !avoidProduction));
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
          boolean spaceFree = gameMap.isLocationEmpty(unit, moveCoord);
          Unit resident = gameMap.getLocation(moveCoord).getResident();
          if( !spaceFree && (!canEvict || (unit.CO != resident.CO || resident.isTurnOver)) )
            continue; // Bail if we can't clear the space

          // See if we can bag enough damage to be worth sacrificing the unit
          if( actionSet.getSelected().getType() == UnitActionFactory.ATTACK )
          {
            for( GameAction ga : actionSet.getGameActions() )
            {
              MapLocation targetLoc = gameMap.getLocation(ga.getTargetLocation());
              Unit target = targetLoc.getResident();
              if( null == target )
                continue;
              BattleSummary results =
                  CombatEngine.simulateBattleResults(unit, target, gameMap, moveCoord.xCoord, moveCoord.yCoord);
              double loss   = Math.min(unit  .getHP(), (int)results.attackerHPLoss);
              double damage = Math.min(target.getHP(), (int)results.defenderHPLoss);
              
              boolean goForIt = false;
              if( valueUnit(target, targetLoc, false) * Math.floor(damage) * AGGRO_FUNDS_WEIGHT > valueUnit(unit, unitLoc, true) )
              {
                ai.log(String.format("  %s is going aggro on %s", unit.toStringWithLocation(), target.toStringWithLocation()));
                ai.log(String.format("    He plans to deal %s HP damage for a net gain of %s funds", damage, (target.model.getCost() * damage - unit.model.getCost() * unit.getHP())/10));
                goForIt = true;
              }
              else if( damage > loss
                     && ai.canWallHere(gameMap, ai.threatMap, unit, ga.getMoveLocation()) )
              {
                ai.log(String.format("  %s thinks it's safe to attack %s", unit.toStringWithLocation(), target.toStringWithLocation()));
                goForIt = true;
              }

              if( goForIt )
              {
                if( !spaceFree )
                {
                  boolean ignoreSafety =
                      valueUnit(unit, gameMap.getLocation(moveCoord), true) >= valueUnit(resident, gameMap.getLocation(moveCoord), true);
                  return ai.evictUnit(gameMap, ai.allThreats, ai.threatMap, unit, resident, ignoreSafety, avoidProduction);
                }
                return ga;
              }
            }
          }

          // Only consider capturing if we can sit still or go somewhere safe.
          if( actionSet.getSelected().getType() == UnitActionFactory.CAPTURE
              && (moveCoord.getDistance(unit.x, unit.y) == 0 || ai.canWallHere(gameMap, ai.threatMap, unit, moveCoord)) )
          {
            if( !spaceFree )
            {
              boolean ignoreSafety =
                  valueUnit(unit, gameMap.getLocation(moveCoord), true) >= valueUnit(resident, gameMap.getLocation(moveCoord), true);
              return ai.evictUnit(gameMap, ai.allThreats, ai.threatMap, unit, resident, ignoreSafety, avoidProduction);
            }
            return actionSet.getSelected();
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
    private final WallyAI ai;
    public Travel(Commander co, WallyAI ai)
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
      return ai.findTravelAction(gameMap, ai.allThreats, ai.threatMap, unit, false, ignoreSafety, avoidProduction);
    }
  }

  public static class BuildStuff implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public final Commander myCo;
    public final WallyAI ai;

    public BuildStuff(Commander co, WallyAI ai)
    {
      myCo = co;
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
          if( resident.CO == myCo && !resident.isTurnOver )
            return ai.evictUnit(gameMap, ai.allThreats, ai.threatMap, null, resident, ignoreSafety, avoidProduction);
          else
          {
            ai.log(String.format("  Can't evict unit %s to build %s", resident.toStringWithLocation(), builds.get(coord)));
            builds.remove(coord);
            continue;
          }
        }
        ArrayList<UnitModel> list = myCo.getShoppingList(gameMap.getLocation(coord)); // COs expect to see their shopping lists fetched before a purchase
        UnitModel toBuy = builds.get(coord);
        if( toBuy.getCost() <= myCo.money && list.contains(toBuy) )
        {
          builds.remove(coord);
          return new GameAction.UnitProductionAction(myCo, toBuy, coord);
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
                                  ArrayList<Unit> allThreats, Map<UnitModel, Map<XYCoord, Double>> threatMap,
                                  Unit unit,
                                  boolean avoidProduction )
  {
    ArrayList<XYCoord> goals = new ArrayList<XYCoord>();

    ArrayList<XYCoord> stations = AIUtils.findRepairDepots(unit);
    Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), stations);
    boolean shouldResupply = false;
    if( stations.size() > 0 )
    {
      shouldResupply = unit.getHP() <= UNIT_HEAL_THRESHHOLD;
      shouldResupply |= unit.fuel <= UNIT_REFUEL_THRESHHOLD
          * Utils.findShortestPath(unit, stations.get(0), gameMap).getFuelCost(unit.model, gameMap);
      shouldResupply |= unit.ammo >= 0 && unit.ammo <= unit.model.maxAmmo * UNIT_REARM_THRESHHOLD;
    }

    if( shouldResupply )
    {
      log(String.format("  %s needs supplies.", unit.toStringWithLocation()));
      goals.addAll(stations);
      if( avoidProduction )
        goals.removeAll(AIUtils.findAlliedIndustries(gameMap, myCo, goals, !avoidProduction));
    }
    else if( unit.model.possibleActions.contains(UnitActionFactory.CAPTURE) )
    {
      for( XYCoord xyc : unownedProperties )
        if( !AIUtils.isCapturing(gameMap, myCo, xyc) )
          goals.add(xyc);
    }
    else if( unit.model.possibleActions.contains(UnitActionFactory.ATTACK) )
    {
      Map<UnitModel, Double> valueMap = new HashMap<UnitModel, Double>();
      Map<UnitModel, ArrayList<XYCoord>> targetMap = new HashMap<UnitModel, ArrayList<XYCoord>>();

      // Categorize all enemies by type, and all types by how well we match up vs them
      for( Unit target : allThreats )
      {
        UnitModel model = target.model;
        XYCoord targetCoord = new XYCoord(target.x, target.y);
        double effectiveness = findEffectiveness(unit.model, target.model);
        if (Utils.findShortestPath(unit, targetCoord, gameMap, true) != null &&
            AGGRO_EFFECT_THRESHHOLD < effectiveness)
        {
          valueMap.put(model, effectiveness*model.getCost());
          if (!targetMap.containsKey(model)) targetMap.put(model, new ArrayList<XYCoord>());
          targetMap.get(model).add(targetCoord);
        }
      }

      // Sort all individual target lists by distance
      for (ArrayList<XYCoord> targetList : targetMap.values())
        Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), targetList);

      // Sort all target types by how much we want to shoot them with this unit
      Queue<Entry<UnitModel, Double>> targetTypesInOrder = 
          new PriorityQueue<Entry<UnitModel, Double>>(myCo.unitModels.size(), new UnitModelFundsComparator());
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
        if( myCo.unitProductionByTerrain.containsKey(loc.getEnvironment().terrainType)
            && myCo.isEnemy(loc.getOwner()) )
        {
          goals.add(coord);
        }
      }
    }

    if( goals.isEmpty() ) // If there's really nothing to do, go to MY HQ
      goals.add(myCo.HQLocation);

    Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), goals);
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
                        ArrayList<Unit> allThreats, Map<UnitModel, Map<XYCoord, Double>> threatMap,
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
    GameAction result = FreeRealEstate.findValueAction(myCo, this, unit, gameMap, mustMove, avoidProduction, canEvict);
    if( null == result )
    {
      result = findTravelAction(gameMap, allThreats, threatMap, unit, ignoreSafety, mustMove, avoidProduction);
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
                        ArrayList<Unit> allThreats, Map<UnitModel, Map<XYCoord, Double>> threatMap,
                        Unit unit,
                        boolean ignoreSafety, boolean mustMove,
                        boolean avoidProduction )
  {
    // Find the possible destinations.
    boolean ignoreResident = true;
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, ignoreResident);
    destinations.removeAll(AIUtils.findAlliedIndustries(gameMap, myCo, destinations, !avoidProduction));

    // TODO: Jump in a transport, if available, or join?

    XYCoord goal = null;
    GamePath path = null;
    ArrayList<XYCoord> validTargets = findTravelDestinations(gameMap, allThreats, threatMap, unit, avoidProduction);
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
    path.snip(unit.model.movePower + 1); // Trim the path approximately down to size.
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
      if( !ignoreSafety && !canWallHere(gameMap, threatMap, unit, xyc) )
        continue;

      GameAction action = null;
      Unit resident = gameMap.getLocation(xyc).getResident();
      if( null != resident && unit != resident )
      {
        boolean evictIgnoreSafety =
            valueUnit(unit, gameMap.getLocation(xyc), true) >= valueUnit(resident, gameMap.getLocation(xyc), true);
        if( unit.CO == resident.CO && !resident.isTurnOver )
          action = evictUnit(gameMap, allThreats, threatMap, unit, resident, evictIgnoreSafety, avoidProduction);
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
                    double loss   = Math.min(unit            .getHP(), (int)results.attackerHPLoss);
                    double damage = Math.min(results.defender.getHP(), (int)results.defenderHPLoss);

                    if( damage > loss ) // only shoot that which you hurt more than it hurts you
                      return damage * results.defender.model.getCost();

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

  private boolean isSafe(GameMap gameMap, Map<UnitModel, Map<XYCoord, Double>> threatMap, Unit unit, XYCoord xyc)
  {
    Double threat = threatMap.get(unit.model).get(xyc);
    int threshhold = unit.model.hasDirectFireWeapon() ? DIRECT_THREAT_THRESHHOLD : INDIRECT_THREAT_THRESHHOLD;
    return (null == threat || threshhold > threat);
  }

  /**
   * @return whether it's safe or a good place to wall
   * For use after unit building is complete
   */
  private boolean canWallHere(GameMap gameMap, Map<UnitModel, Map<XYCoord, Double>> threatMap, Unit unit, XYCoord xyc)
  {
    MapLocation destination = gameMap.getLocation(xyc);
    // if we're safe, we're safe
    if( isSafe(gameMap, threatMap, unit, xyc) )
      return true;

    // TODO: Determine whether the ally actually needs a wall there. Mechs walling for Tanks vs inf is... silly.
    // if we'd be a nice wall for a worthy ally, we can pretend we're safe there also
    ArrayList<XYCoord> adjacentCoords = Utils.findLocationsInRange(gameMap, xyc, 1);
    for( XYCoord coord : adjacentCoords )
    {
      MapLocation loc = gameMap.getLocation(coord);
      if( loc != null )
      {
        Unit resident = loc.getResident();
        if( resident != null && !myCo.isEnemy(resident.CO)
            && valueUnit(resident, loc, true) > valueUnit(unit, destination, true) )
        {
          return true;
        }
      }
    }
    return false;
  }

  private static int valueUnit(Unit unit, MapLocation locale, boolean includeCurrentHealth)
  {
    int value = unit.model.getCost();

    if( unit.CO.isEnemy(locale.getOwner()) &&
            unit.model.hasActionType(UnitActionFactory.CAPTURE)
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

  /**
   * Returns the center mass of a given unit type, weighted by HP
   * NOTE: Will violate fog knowledge
   */
  private static XYCoord findAverageDeployLocation(GameMap gameMap, Commander co, UnitModel model)
  {
    // init with the center of the map
    int totalX = gameMap.mapWidth / 2;
    int totalY = gameMap.mapHeight / 2;
    int totalPoints = 1;
    for( Unit unit : co.units )
    {
      if( unit.model == model )
      {
        totalX += unit.x * unit.getHP();
        totalY += unit.y * unit.getHP();
        totalPoints += unit.getHP();
      }
    }

    return new XYCoord(totalX / totalPoints, totalY / totalPoints);
  }

  /**
   * Returns the ideal place to build a unit type or null if it's impossible
   * Kinda-sorta copied from AIUtils
   */
  public XYCoord getLocationToBuild(CommanderProductionInfo CPI, UnitModel model)
  {
    Set<TerrainType> desiredTerrains = CPI.modelToTerrainMap.get(model);
    ArrayList<XYCoord> candidates = new ArrayList<XYCoord>();
    for( MapLocation loc : CPI.availableProperties )
    {
      if( desiredTerrains.contains(loc.getEnvironment().terrainType) )
      {
        candidates.add(loc.getCoordinates());
      }
    }
    if( candidates.isEmpty() )
      return null;

    // Sort locations by how close they are to "center mass" of that unit type, then reverse since we want to distribute our forces
    Utils.sortLocationsByDistance(findAverageDeployLocation(myCo.myView, myCo, model), candidates);
    Collections.reverse(candidates);
    return candidates.get(0);
  }

  private Map<XYCoord, UnitModel> queueUnitProductionActions(GameMap gameMap)
  {
    Map<XYCoord, UnitModel> builds = new HashMap<XYCoord, UnitModel>();
    // Figure out what unit types we can purchase with our available properties.
    boolean includeFriendlyOccupied = true;
    CommanderProductionInfo CPI = new CommanderProductionInfo(myCo, gameMap, includeFriendlyOccupied);

    if( CPI.availableProperties.isEmpty() )
    {
      log("No properties available to build.");
      return builds;
    }

    log("Evaluating Production needs");
    int budget = myCo.money;
    UnitModel infModel = myCo.getUnitModel(UnitModel.TROOP);

    // Get a count of enemy forces.
    Map<Commander, ArrayList<Unit>> unitLists = AIUtils.getEnemyUnitsByCommander(myCo, gameMap);
    Map<UnitModel, Double> enemyUnitCounts = new HashMap<UnitModel, Double>();
    for( Commander co : unitLists.keySet() )
    {
      if( myCo.isEnemy(co) )
      {
        for( Unit u : unitLists.get(co) )
        {
          // Count how many of each model of enemy units are in play.
          if( enemyUnitCounts.containsKey(u.model) )
          {
            enemyUnitCounts.put(u.model, enemyUnitCounts.get(u.model) + (u.getHP() / 10));
          }
          else
          {
            enemyUnitCounts.put(u.model, u.getHP() / 10.0);
          }
        }
      }
    }

    // Figure out how well we think we have the existing threats covered
    Map<UnitModel, Double> myUnitCounts = new HashMap<UnitModel, Double>();
    for( Unit u : myCo.units )
    {
      // Count how many of each model of enemy units are in play.
      if( myUnitCounts.containsKey(u.model) )
      {
        myUnitCounts.put(u.model, myUnitCounts.get(u.model) + (u.getHP() / 10));
      }
      else
      {
        myUnitCounts.put(u.model, u.getHP() / 10.0);
      }
    }

    for( UnitModel threat : enemyUnitCounts.keySet() )
    {
      for( UnitModel counter : myUnitCounts.keySet() ) // Subtract how well we think we counter each enemy from their HP counts
      {
        double counterPower = findEffectiveness(counter, threat);
        enemyUnitCounts.put(threat, enemyUnitCounts.get(threat) - counterPower * myUnitCounts.get(counter));
      }
    }

    // change unit quantity->funds
    for( Entry<UnitModel, Double> ent : enemyUnitCounts.entrySet() )
    {
      ent.setValue(ent.getValue() * ent.getKey().getCost());
    }

    Queue<Entry<UnitModel, Double>> enemyModels = 
        new PriorityQueue<Entry<UnitModel, Double>>(myCo.unitModels.size(), new UnitModelFundsComparator());
    enemyModels.addAll(enemyUnitCounts.entrySet());

    // Try to purchase units that will counter the most-represented enemies.
    while (!enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())
    {
      // Find the first (most funds-invested) enemy UnitModel, and remove it. Even if we can't find an adequate counter,
      // there is not reason to consider it again on the next iteration.
      UnitModel enemyToCounter = enemyModels.poll().getKey();
      double enemyNumber = enemyUnitCounts.get(enemyToCounter);
      log(String.format("Need a counter for %sx%s", enemyToCounter, enemyNumber / enemyToCounter.getCost() / enemyToCounter.maxHP));
      log(String.format("Remaining budget: %s", budget));

      // Get our possible options for countermeasures.
      ArrayList<UnitModel> availableUnitModels = new ArrayList<UnitModel>(CPI.availableUnitModels);
      while (!availableUnitModels.isEmpty())
      {
        // Sort my available models by their power against this enemy type.
        Collections.sort(availableUnitModels, new UnitPowerComparator(enemyToCounter, this));

        // Grab the best counter.
        UnitModel idealCounter = availableUnitModels.get(0);
        availableUnitModels.remove(idealCounter); // Make sure we don't try to build two rounds of the same thing in one turn.
        // I only want combat units, since I don't understand transports
        if( !idealCounter.weapons.isEmpty() )
        {
          log(String.format("  buy %s?", idealCounter));
          int totalCost = idealCounter.getCost();

          // Calculate a cost buffer to ensure we have enough money left so that no factories sit idle.
          int costBuffer = (CPI.getNumFacilitiesFor(infModel) - 1) * infModel.getCost(); // The -1 assumes we will build this unit from a factory. Possibly untrue.
          if( 0 > costBuffer )
            costBuffer = 0; // No granting ourselves extra moolah.
          if(totalCost <= (budget - costBuffer))
          {
            // Go place orders.
            log(String.format("    I can build %s for a cost of %s (%s remaining, witholding %s)",
                                    idealCounter, totalCost, budget, costBuffer));
            XYCoord coord = getLocationToBuild(CPI, idealCounter);
            builds.put(coord, idealCounter);
            budget -= idealCounter.getCost();
            CPI.removeBuildLocation(gameMap.getLocation(coord));
            // We found a counter for this enemy UnitModel; break and go to the next type.
            // This break means we will build at most one type of unit per turn to counter each enemy type.
            break;
          }
          else
          {
            log(String.format("    %s cost %s, I have %s (witholding %s).", idealCounter, idealCounter.getCost(), budget,
                costBuffer));
          }
        }
      } // ~while( !availableUnitModels.isEmpty() )
    } // ~while( !enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())

    // Build infantry from any remaining facilities.
    log("Building infantry to fill out my production");
    while ((budget >= infModel.getCost()) && (CPI.availableUnitModels.contains(infModel)))
    {
      XYCoord coord = getLocationToBuild(CPI, infModel);
      builds.put(coord, infModel);
      budget -= infModel.getCost();
      CPI.removeBuildLocation(gameMap.getLocation(coord));
      log(String.format("  At %s (%s remaining)", coord, budget));
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

  /**
   * Arrange UnitModels according to their effective damage/range against a configured UnitModel.
   */
  private static class UnitPowerComparator implements Comparator<UnitModel>
  {
    UnitModel targetModel;
    private WallyAI wally;

    public UnitPowerComparator(UnitModel targetType, WallyAI pWally)
    {
      targetModel = targetType;
      wally = pWally;
    }

    @Override
    public int compare(UnitModel model1, UnitModel model2)
    {
      double eff1 = wally.findEffectiveness(model1, targetModel);
      double eff2 = wally.findEffectiveness(model2, targetModel);

      return (eff1 < eff2) ? 1 : ((eff1 > eff2) ? -1 : 0);
    }
  }

  /** Returns effective power in terms of whole kills per unit, based on respective threat areas and how much damage I deal */
  public double findEffectiveness(UnitModel model, UnitModel target)
  {
    double theirRange = 0;
    for( WeaponModel wm : target.weapons )
    {
      double range = wm.maxRange;
      if( wm.canFireAfterMoving )
        range += getEffectiveMove(target);
      theirRange = Math.max(theirRange, range);
    }
    double counterPower = 0;
    for( WeaponModel wm : model.weapons )
    {
      double damage = wm.getDamage(target);
      double myRange = wm.maxRange;
      if( wm.canFireAfterMoving )
        myRange += getEffectiveMove(model);
      else
        myRange -= (Math.pow(wm.minRange, MIN_SIEGE_RANGE_WEIGHT) - 1); // penalize range based on inner range
      double rangeMod = Math.pow(myRange / theirRange, RANGE_WEIGHT);
      // TODO: account for average terrain defense?
      double effectiveness = damage * rangeMod / 100;
      counterPower = Math.max(counterPower, effectiveness);
    }
    return counterPower;
  }
}
