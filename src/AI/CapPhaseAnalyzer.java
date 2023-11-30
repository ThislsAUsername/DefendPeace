package AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GameAction;
import Engine.GamePath;
import Engine.Utils;
import Engine.XYCoord;
import Engine.UnitActionLifecycles.CaptureLifecycle;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

public class CapPhaseAnalyzer implements Serializable
{
  private static final long serialVersionUID = 7714433092190703028L;
  static final int LOOKAHEAD_TURNS = 3;

  public static class CapStop
  {
    public int extraTurns = 0; // Defines how many turns we have already looked ahead to try to find another cap stop
    public final XYCoord coord;
    public CapStop(XYCoord coord)
    {
      this.coord = coord;
    }
    @Override
    public String toString()
    {
      return coord + "+" + extraTurns;
    }
  }
  protected Map<XYCoord, ArrayList<ArrayList<CapStop>>> capChains = new HashMap<>();
  protected Map<Unit, ArrayList<CapStop>> capChainsAllocated = new HashMap<>();
  private ArrayList<XYCoord> contestedProps; // as was probably considered by the map designer; doesn't necessarily take movement/production differences into account

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Contested properties:");
    for( XYCoord contested : contestedProps )
      sb.append(String.format(" %s ", contested));
    sb.append("\n");
    for( XYCoord factoryXYC : capChains.keySet() )
    {
      sb.append(String.format("Cap chains for %s:\n", factoryXYC));
      for( ArrayList<CapStop> chain : capChains.get(factoryXYC) )
      {
        sb.append(String.format("  chain\n"));
        for( CapStop stop : chain )
          sb.append(String.format("    %s\n", stop));
      }
    }
    return sb.toString();
  }

  /**
   * If the input unit has a cap chain allocated, return it
   * Else, if it's on the start of a cap chain in the un-allocated list, remove the chain from the list and allocate it
   * @return The chosen chain, if any
   */
  public ArrayList<CapStop> getCapChain(Unit unit)
  {
    ArrayList<CapStop> chain = null;
    XYCoord position = new XYCoord(unit);
    // Add the unit to a cap chain, if possible
    if( capChainsAllocated.containsKey(unit) )
      chain = capChainsAllocated.get(unit);
    else
    {
      ArrayList<ArrayList<CapStop>> chainList = capChains.get(position);
      if( null != chainList && !chainList.isEmpty() )
      {
        chain = chainList.remove(0);
        capChainsAllocated.put(unit, chain);
        if( chainList.isEmpty() )
          capChains.remove(position);
      }
    }

    return chain;
  }

  public GameAction getCapAction(GameMap map, Unit unit)
  {
    XYCoord position = new XYCoord(unit);
    ArrayList<CapStop> chain = getCapChain(unit);
    if( null != chain )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        return new CaptureLifecycle.CaptureAction(map, unit, Utils.findShortestPath(unit, position, map));
      }

      while (!chain.isEmpty())
      {
        final XYCoord coord = chain.get(0).coord;
        // If we're already there or own it, throw the point out
        if( position.equals(coord) || !unit.CO.isEnemy(map.getLocation(coord).getOwner()) )
          chain.remove(0);
        else
          return findChainAction(map, unit, chain);
      }
      // Chain's done, if we ever get here
      capChainsAllocated.remove(unit);
    }

    return null;
  }

  public GameAction findChainAction(GameMap gameMap, Unit unit, ArrayList<CapStop> chain)
  {
    XYCoord goal = chain.get(0).coord;

    boolean includeOccupiedSpaces = false; // We assume that the area won't be cluttered
    ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeOccupiedSpaces);

    // If we can get to our destination, go for it
    if( destinations.contains(goal) )
      return new CaptureLifecycle.CaptureAction(gameMap, unit, Utils.findShortestPath(unit, goal, gameMap));

    return AIUtils.moveTowardLocation(unit, goal, gameMap);
  }

  public CapPhaseAnalyzer(GameMap map, Army viewer)
  {
    contestedProps = new ArrayList<>();

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
        else if( env.terrainType.isProfitable() )
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
        if( viewer == currentOwner.army )
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
        inf.x = ownedFac.x;
        inf.y = ownedFac.y;

        final GamePath infPath = findFeasiblePath(inf, neutralFac, map);
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
      if( null != newOwner && viewer == newOwner.army )
        rightfulFactories.add(neutralFac);
    }

    // Finally, figure out what non-factories are contested or rightfully mine
    for( XYCoord propXYC : props )
    {
      // Each Army's turns to cap this prop, measured in % of a turn's movement
      HashMap<Army, Integer> possibleOwners = new HashMap<>();
      for( XYCoord ownedFac : factoryOwnership.keySet() )
      {
        final Commander owner = factoryOwnership.get(ownedFac);
        if( owner == null )
          continue; // Don't barf in weird maps

        final Unit inf = new Unit(owner, owner.getUnitModel(UnitModel.TROOP));
        inf.x = ownedFac.x;
        inf.y = ownedFac.y;

        final GamePath infPath = findFeasiblePath(inf, propXYC, map);
        if( null == infPath || infPath.getPathLength() < 1 )
          continue; // Can't reach this city

        int oldDistance = Integer.MAX_VALUE;
        if( possibleOwners.containsKey(owner.army) )
          oldDistance = possibleOwners.get(owner.army);

        int distance = infPath.getFuelCost(inf, map) * 100 / inf.getMovePower(map);
        if( distance < oldDistance )
          possibleOwners.put(owner.army, distance);
      }


      // Calculate who's the closest, and if that army has real competition for this prop
      Army closestArmy = null;
      int closestDistance = Integer.MAX_VALUE;
      for( Army army : possibleOwners.keySet() )
      {
        int distance = possibleOwners.get(army);
        if( distance < closestDistance )
        {
          closestArmy = army;
          closestDistance = distance;
        }
      }

      boolean contested = false;
      for( Army army : possibleOwners.keySet() )
      {
        if(!army.isEnemy(closestArmy))
          continue;
        int distance = possibleOwners.get(army);
        int distanceDelta = Math.abs(closestDistance - distance);
        contested |= ( distanceDelta <= 100 );
      }

      // If it isn't contested and we want to try to cap it, add it to the list
      if( contested )
        contestedProps.add(propXYC);
      else if( viewer == closestArmy
            && viewer.isEnemy(map.getLocation(propXYC).getOwner()) ) // Don't try to cap it if we already own it
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
      inf.x = start.x;
      inf.y = start.y;

      final GamePath infPath = findFeasiblePath(inf, dest, map);
      if( null == infPath || infPath.getPathLength() < 1 )
        continue; // Can't reach

      int distance = infPath.getFuelCost(inf, map);

      ArrayList<CapStop> chain = new ArrayList<>();
      CapStop build = new CapStop(start);
      // A bunch of "free funding turns" should convince the chain-sorter to put factory-captures first.
      build.extraTurns = distance/inf.getMovePower(map) - 13;
      chain.add(build);
      CapStop cap = new CapStop(dest);
      chain.add(cap);
      factoryCapChains.add(chain);

      // Now that we're just in cap-chain land, don't worry about whether we start with this factory or not.
      startingFactories.add(dest);
    }

    buildBaseCapChains(map, viewer, rightfulProps, startingFactories);

    // Add our factory chains in at the start of each list
    for(ArrayList<CapStop> chain : factoryCapChains)
    {
      XYCoord start = chain.get(0).coord;
      capChains.get(start).add(0, chain);
    }
  }

  public void buildBaseCapChains(GameMap map, Army viewer, ArrayList<XYCoord> rightfulProps, ArrayList<XYCoord> startingFactories)
  {
    ArrayList<XYCoord> remainingFactories = new ArrayList<>(startingFactories);
    // Build initial bits of capChains
    for( XYCoord start : remainingFactories )
    {
      ArrayList<ArrayList<CapStop>> chainList = new ArrayList<>();
      capChains.put(start, chainList);
    }

    boolean madeProgress = true;
    final Unit inf = new Unit(viewer.cos[0], viewer.cos[0].getUnitModel(UnitModel.TROOP));
    if( startingFactories.size() > 0 )
    {
      inf.x = startingFactories.get(0).x;
      inf.y = startingFactories.get(0).y;
    }
    final int infMove = inf.getMovePower(map);

    // Find the next stop or iterate extraTurns on all cap chains
    while (madeProgress && !rightfulProps.isEmpty())
    {
      // Create new cap chains
      for( XYCoord start : remainingFactories )
      {
        ArrayList<CapStop> chain = new ArrayList<>();
        CapStop build = new CapStop(start);
        chain.add(build);
        capChains.get(start).add(0, chain);
      }

      madeProgress = false;
      for( ArrayList<ArrayList<CapStop>> chainList : capChains.values() )
        for( ArrayList<CapStop> chain : chainList )
        {
          if( rightfulProps.isEmpty() )
            break;

          CapStop last = chain.get(chain.size() - 1);
          if( last.extraTurns >= LOOKAHEAD_TURNS )
          {
            if (chain.size() == 1)
              remainingFactories.remove(last.coord);
            break;
          }

          XYCoord start = last.coord;

          inf.x = start.x;
          inf.y = start.y;
          Utils.sortLocationsByTravelTime(inf, rightfulProps, map);
          XYCoord dest = rightfulProps.get(0);

          final GamePath infPath = findFeasiblePath(inf, dest, map);
          if( null == infPath || infPath.getPathLength() < 1 )
          {
            last.extraTurns = LOOKAHEAD_TURNS+1;
            continue; // Can't reach
          }
          madeProgress = true; // We have somewhere we can still get to

          final int distance = infPath.getFuelCost(inf, map);
          final int currentTotalMove = (last.extraTurns + 1) * infMove;

          if( distance <= currentTotalMove )
          {
            rightfulProps.remove(dest);
            CapStop cap = new CapStop(dest);
            chain.add(cap);
          }
          else
            last.extraTurns++;
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

  public static GamePath findFeasiblePath(final Unit unit, final XYCoord destination, final GameMap map)
  {
    final boolean theoretical = true;
    return Utils.findShortestPath(new XYCoord(unit), unit.CO.army, unit.getMoveFunctor(),
                                  unit.getMovePower(map) * (LOOKAHEAD_TURNS),
                                  destination.x, destination.y,
                                  map, theoretical);
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
        int turnShift = capList.get(i-1).extraTurns;
        currentTurn += turnShift + 1; // +1 for the extra cap turn
        // We get income from the prop for every turn after we captured it
        currentIncome += Math.max(0, turnLimit - currentTurn);
      }

      return currentIncome;
    }
  }

}
