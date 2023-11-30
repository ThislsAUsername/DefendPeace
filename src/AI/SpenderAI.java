package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.Army;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

/**
 *  This AI's intent is to just spend all of its resources, action economy included
 */
public class SpenderAI implements AIController
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Army co)
    {
      return new SpenderAI(co);
    }

    @Override
    public String getName()
    {
      return "Spender";
    }

    @Override
    public String getDescription()
    {
      return
          "All Spender knows is that money in the bank doesn't help on the field, so he tries to spend all funds as quickly as possible.\n" +
          "This can sometimes result in building units that are not useful.";
    }
  }
  public static final AIMaker info = new instantiator();
  
  @Override
  public AIMaker getAIInfo()
  {
    return info;
  }
  
  Queue<GameAction> actions = new ArrayDeque<GameAction>();
  Queue<Unit> unitQueue = new ArrayDeque<Unit>();
  boolean stateChange;

  private Army myArmy = null;

  private ArrayList<XYCoord> unownedProperties;
  private ArrayList<XYCoord> capturingProperties;

  private StringBuffer logger = new StringBuffer();
  private boolean shouldLog = true;
  private int turnNum = 0;

  public SpenderAI(Army army)
  {
    myArmy = army;
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    turnNum++;
    log(String.format("[======== SpAI initializing turn %s for %s =========]", turnNum, myArmy));

    // Make sure we don't have any hang-ons from last time.
    actions.clear();

    // Create a list of every property we don't own, but want to.
    unownedProperties = new ArrayList<XYCoord>();
    for( int x = 0; x < gameMap.mapWidth; ++x )
    {
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        XYCoord loc = new XYCoord(x, y);
        if( gameMap.getLocation(loc).isCaptureable() && myArmy.isEnemy(gameMap.getLocation(loc).getOwner()) )
        {
          unownedProperties.add(loc);
        }
      }
    }
    capturingProperties = new ArrayList<XYCoord>();
    for( Unit unit : myArmy.getUnits() )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        capturingProperties.add(unit.getCaptureTargetCoords());
      }
    }

    // Check for a turn-kickoff power
    AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_TURN_START);
  }

  @Override
  public void endTurn()
  {
    log(String.format("[======== SpAI ending turn %s for %s =========]", turnNum, myArmy));
    logger = new StringBuffer();
  }

  private void log(String message)
  {
    if( shouldLog )
    {
      System.out.println(message);
      logger.append(message).append('\n');
    }
  }
  @Override
  public void setLogging(boolean value) { shouldLog = value; }

  @Override
  public GameAction getNextAction(GameMap gameMap)
  {
    GameAction nextAction = null;
    do
    {
      // If we have more actions ready, don't bother calculating stuff.
      if( !actions.isEmpty() )
      {
        GameAction action = actions.poll();
        log("  Action: " + action);
        return action;
      }
      else if( unitQueue.isEmpty() )
      {
        stateChange = false; // There's been no gamestate change since we last iterated through all the units, since we're about to do just that
        for( Unit unit : myArmy.getUnits() )
        {
          if( unit.isTurnOver || !gameMap.isLocationValid(unit.x, unit.y))
            continue; // No actions for units that are stale or out of bounds
          unitQueue.offer(unit);
        }
      }

      Queue<Unit> travelQueue = new ArrayDeque<Unit>();
      // Handle actions for each unit the CO owns.
      while (!unitQueue.isEmpty())
      {
        Unit unit = unitQueue.poll();
        boolean foundAction = false;

        // Find the possible destinations.
        Utils.PathCalcParams pcp = new Utils.PathCalcParams(unit, gameMap);
        pcp.includeOccupiedSpaces = false;
        ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths();

        for( Utils.SearchNode coord : destinations )
        {
          // Figure out how to get here.
          GamePath movePath = coord.getMyPath();

          // Figure out what I can do here.
          ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath);
          for( GameActionSet actionSet : actionSets )
          {
            // See if we have the option to attack.
            if( actionSet.getSelected().getType() == UnitActionFactory.ATTACK )
            {
              actions.offer(actionSet.getSelected());
              foundAction = true;
              break;
            }

            // Otherwise, see if we have the option to capture.
            if( actionSet.getSelected().getType() == UnitActionFactory.CAPTURE )
            {
              actions.offer(actionSet.getSelected());
              capturingProperties.add(coord);
              foundAction = true;
              break;
            }
          }
          if( foundAction )
            break; // Only allow one action per unit.
        }
        if( foundAction )
        {
          stateChange = true;
          break; // Only one action per getNextAction() call, to avoid overlap.
        }
        else
        {
          travelQueue.offer(unit); // if we can't do anything useful right now, consider just moving towards a useful destination
        }
      }

      // If no attack/capture actions are available now, just move towards a non-allied building.
      if( actions.isEmpty() && !stateChange )
      {
        while (!travelQueue.isEmpty())
        {
          Unit unit = travelQueue.poll();
          UnitContext uc = new UnitContext(gameMap, unit);

          // Find the possible destinations.
          Utils.PathCalcParams pcp = new Utils.PathCalcParams(unit, gameMap);
          pcp.includeOccupiedSpaces = false;
          ArrayList<Utils.SearchNode> destinations = pcp.findAllPaths();

          if( !unownedProperties.isEmpty() ) // Sanity check - it shouldn't be, unless this function is called after we win.
          {
            log(String.format("  Seeking a property to send %s after", unit.toStringWithLocation()));
            int index = 0;
            XYCoord goal = null;
            GamePath path = null;
            boolean validTarget = false;
            ArrayList<XYCoord> validTargets = new ArrayList<>();

            if( uc.calculateActionTypes().contains(UnitActionFactory.CAPTURE) )
            {
              validTargets.addAll(unownedProperties);
            }
            else
            {
              for( Army co : gameMap.game.armies )
              {
                if( myArmy.isEnemy(co) )
                  validTargets.addAll(co.HQLocations);
              }
            }
            // Loop until we find a valid property to go capture or run out of options.
            Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), validTargets);
            do
            {
              goal = validTargets.get(index++);
              path = Utils.findShortestPath(unit, goal, gameMap, true);
              validTarget = (myArmy.isEnemy(gameMap.getLocation(goal).getOwner()) // Property is not allied.
                  && !capturingProperties.contains(goal) // We aren't already capturing it.
                  && (path.getPathLength() > 0)); // We can reach it.
              log(String.format("    %s at %s? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal,
                  (validTarget ? "Yes" : "No")));
            } while (!validTarget && (index < validTargets.size())); // Loop until we run out of properties to check.

            if( validTarget )
            {
              log(String.format("    Selected %s at %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal));

              // Choose the point on the path just out of our range as our 'goal', and try to move there.
              // This will allow us to navigate around large obstacles that require us to move away
              // from our intended long-term goal.
              path.snip(unit.getMovePower(gameMap) + 1); // Trim the path approximately down to size.
              goal = path.getEndCoord(); // Set the last location as our goal.

              log(String.format("    Intermediate waypoint: %s", goal));

              // Sort my currently-reachable move locations by distance from the goal,
              // and build a GameAction to move to the closest one.
              Utils.sortLocationsByDistance(goal, destinations);
              Utils.SearchNode destination = destinations.get(0);
              GamePath movePath = destination.getMyPath();
              if( movePath.getPathLength() > 1 ) // We only want to try to travel if we can actually go somewhere
              {
                GameAction move = new WaitLifecycle.WaitAction(unit, movePath);
                actions.offer(move);
                stateChange = true;
                break;
              }
            }
          }
        }
      }

      // Check for an available buying enhancement power
      if( actions.isEmpty() && !stateChange )
      {
        AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_BUY);
      }

      // We will add all build commands at once, since they can't conflict.
      if( actions.isEmpty() && !stateChange )
      {
        Map<MapLocation, ArrayList<UnitModel>> shoppingLists = new HashMap<>();
        for( XYCoord xyc : myArmy.getOwnedProperties() )
        {
          MapLocation loc = gameMap.getLocation(xyc);
          Commander buyer = loc.getOwner();
          // I like combat units that are useful, so we skip ports for now
          if( loc.getEnvironment().terrainType != TerrainType.SEAPORT && loc.getResident() == null )
          {
            ArrayList<UnitModel> units = buyer.getShoppingList(loc);
            // Only add to the list if we could actually buy something here.
            if( !units.isEmpty() && buyer.getBuyCost(units.get(0), xyc) <= myArmy.money )
            {
              shoppingLists.put(loc, units);
            }
          }
        }
        int budget = myArmy.money;
        Map<MapLocation, UnitModel> purchases = new HashMap<>();
        // Now that we know where and what we can buy, let's make some initial selections.
        for( Entry<MapLocation, ArrayList<UnitModel>> locShopList : shoppingLists.entrySet() )
        {
          ArrayList<UnitModel> units = locShopList.getValue();
          for( UnitModel unit : units )
          {
            // I only want combat units, since I don't understand transports
            Commander buyer = locShopList.getKey().getOwner();
            final int unitCost = buyer.getBuyCost(unit, locShopList.getKey().getCoordinates());
            if( !unit.weapons.isEmpty() && unitCost <= budget )
            {
              budget -= unitCost;
              purchases.put(locShopList.getKey(), unit);
              break;
            }
          }
        }
        Queue<Entry<MapLocation, ArrayList<UnitModel>>> upgradables = new ArrayDeque<>();
        upgradables.addAll(shoppingLists.entrySet());
        // I want the most expensive single unit I can get, but I also want to spend as much money as possible
        while (!upgradables.isEmpty())
        {
          Entry<MapLocation, ArrayList<UnitModel>> locShopList = upgradables.poll();
          Commander buyer = locShopList.getKey().getOwner();
          ArrayList<UnitModel> units = locShopList.getValue();
          UnitModel currentPurchase = purchases.get(locShopList.getKey());
          if( null != currentPurchase )
          {
            int currentCost = buyer.getBuyCost(currentPurchase, locShopList.getKey().getCoordinates());
            budget += currentCost;
            for( UnitModel newPurchase : units )
            {
              final int newCost = buyer.getBuyCost(newPurchase, locShopList.getKey().getCoordinates());
              // I want expensive units, but they have to have guns
              if( budget > newCost && newCost > currentCost && !newPurchase.weapons.isEmpty() )
              {
                currentPurchase = newPurchase;
                currentCost = newCost;
              }
            }
            // once we've found the most expensive thing we can buy here, record that
            budget -= currentCost;
            purchases.put(locShopList.getKey(), currentPurchase);
          }
        }
        // once we're satisfied with all our selections, put in the orders
        for( Entry<MapLocation, UnitModel> lineItem : purchases.entrySet() )
        {
          GameAction action = new GameAction.UnitProductionAction(lineItem.getKey().getOwner(), lineItem.getValue(),
              lineItem.getKey().getCoordinates());
          actions.offer(action);
        }
      }

      // Check for a turn-ending power
      if( actions.isEmpty() && !stateChange )
      {
        AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_TURN_END);
      }

      // Return the next action, or null if actions is empty.
      nextAction = actions.poll();
    } while (nextAction == null && stateChange); // we don't want to end early, so if the state changed and we don't have an action yet, try again
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }
}
