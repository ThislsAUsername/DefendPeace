package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionType;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

/**
 *  This AI's intent is to just spend all of its resources, action economy included
 */
public class SpenderAI implements AIController
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Commander co)
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

  private Commander myCo = null;

  private ArrayList<XYCoord> unownedProperties;
  private ArrayList<XYCoord> capturingProperties;

  private StringBuffer logger = new StringBuffer();
  private int turnNum = 0;

  public SpenderAI(Commander co)
  {
    myCo = co;
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    turnNum++;
    log(String.format("[======== SpAI initializing turn %s for %s =========]", turnNum, myCo));

    // Make sure we don't have any hang-ons from last time.
    actions.clear();

    // Create a list of every property we don't own, but want to.
    unownedProperties = new ArrayList<XYCoord>();
    for( int x = 0; x < gameMap.mapWidth; ++x )
    {
      for( int y = 0; y < gameMap.mapHeight; ++y )
      {
        XYCoord loc = new XYCoord(x, y);
        if( gameMap.getLocation(loc).isCaptureable() && myCo.isEnemy(gameMap.getLocation(loc).getOwner()) )
        {
          unownedProperties.add(loc);
        }
      }
    }
    capturingProperties = new ArrayList<XYCoord>();
    for( Unit unit : myCo.units )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        capturingProperties.add(unit.getCaptureTargetCoords());
      }
    }

    // Check for a turn-kickoff power
    AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_TURN_START);
  }

  @Override
  public void endTurn()
  {
    log(String.format("[======== SpAI ending turn %s for %s =========]", turnNum, myCo));
    logger = new StringBuffer();
  }

  private void log(String message)
  {
    System.out.println(message);
    logger.append(message).append('\n');
  }

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
        for( Unit unit : myCo.units )
        {
          if( unit.isTurnOver )
            continue; // No actions for stale units.
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
        boolean includeTransports = true;
        ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeTransports);

        for( XYCoord coord : destinations )
        {
          // Figure out how to get here.
          Path movePath = Utils.findShortestPath(unit, coord, gameMap);

          // Figure out what I can do here.
          ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath);
          for( GameActionSet actionSet : actionSets )
          {
            // See if we have the option to attack.
            if( actionSet.getSelected().getType() == UnitActionType.ATTACK )
            {
              actions.offer(actionSet.getSelected());
              foundAction = true;
              break;
            }

            // Otherwise, see if we have the option to capture.
            if( actionSet.getSelected().getType() == UnitActionType.CAPTURE )
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

          // Find the possible destinations.
          boolean includeTransports = false;
          ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap, includeTransports);

          if( !unownedProperties.isEmpty() ) // Sanity check - it shouldn't be, unless this function is called after we win.
          {
            log(String.format("  Seeking a property to send %s after", unit.toStringWithLocation()));
            int index = 0;
            XYCoord goal = null;
            Path path = null;
            boolean validTarget = false;
            ArrayList<XYCoord> validTargets = new ArrayList<>();

            if( unit.model.possibleActions.contains(UnitActionType.CAPTURE) )
            {
              validTargets.addAll(unownedProperties);
            }
            else
            {
              for( XYCoord coord : unownedProperties )
              {
                Location loc = gameMap.getLocation(coord);
                if( loc.getEnvironment().terrainType == TerrainType.HEADQUARTERS && loc.getOwner() != null ) // should we have an attribute for this?
                {
                  validTargets.add(coord);
                }
              }
            }
            // Loop until we find a valid property to go capture or run out of options.
            Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), validTargets);
            do
            {
              goal = validTargets.get(index++);
              path = Utils.findShortestPath(unit, goal, gameMap, true);
              validTarget = (myCo.isEnemy(gameMap.getLocation(goal).getOwner()) // Property is not allied.
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
              path.snip(unit.model.movePower + 1); // Trim the path approximately down to size.
              goal = new XYCoord(path.getEnd().x, path.getEnd().y); // Set the last location as our goal.

              log(String.format("    Intermediate waypoint: %s", goal));

              // Sort my currently-reachable move locations by distance from the goal,
              // and build a GameAction to move to the closest one.
              Utils.sortLocationsByDistance(goal, destinations);
              XYCoord destination = destinations.get(0);
              Path movePath = Utils.findShortestPath(unit, destination, gameMap);
              if( movePath.getPathLength() > 1 ) // We only want to try to travel if we can actually go somewhere
              {
                GameAction move = new GameAction.WaitAction(unit, movePath);
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
        AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_BUY);
      }

      // We will add all build commands at once, since they can't conflict.
      if( actions.isEmpty() && !stateChange )
      {
        Map<Location, ArrayList<UnitModel>> shoppingLists = new HashMap<>();
        for( XYCoord xyc : myCo.ownedProperties )
        {
          Location loc = gameMap.getLocation(xyc);
          // I like combat units that are useful, so we skip ports for now
          if( loc.getEnvironment().terrainType != TerrainType.SEAPORT && loc.getResident() == null )
          {
            ArrayList<UnitModel> units = myCo.getShoppingList(loc);
            // Only add to the list if we could actually buy something here.
            if( !units.isEmpty() && units.get(0).getCost() <= myCo.money )
            {
              shoppingLists.put(loc, units);
            }
          }
        }
        int budget = myCo.money;
        Map<Location, UnitModel> purchases = new HashMap<>();
        // Now that we know where and what we can buy, let's make some initial selections.
        for( Entry<Location, ArrayList<UnitModel>> locShopList : shoppingLists.entrySet() )
        {
          ArrayList<UnitModel> units = locShopList.getValue();
          for( UnitModel unit : units )
          {
            // I only want combat units, since I don't understand transports
            if( !unit.weaponModels.isEmpty() && unit.getCost() <= budget )
            {
              budget -= unit.getCost();
              purchases.put(locShopList.getKey(), unit);
              break;
            }
          }
        }
        Queue<Entry<Location, ArrayList<UnitModel>>> upgradables = new ArrayDeque<>();
        upgradables.addAll(shoppingLists.entrySet());
        // I want the most expensive single unit I can get, but I also want to spend as much money as possible
        while (!upgradables.isEmpty())
        {
          Entry<Location, ArrayList<UnitModel>> locShopList = upgradables.poll();
          ArrayList<UnitModel> units = locShopList.getValue();
          UnitModel currentPurchase = purchases.get(locShopList.getKey());
          if( null != currentPurchase )
          {
            budget += currentPurchase.getCost();
            for( UnitModel unit : units )
            {
              // I want expensive units, but they have to have guns
              if( budget > unit.getCost() && unit.getCost() > currentPurchase.getCost() && !unit.weaponModels.isEmpty() )
                currentPurchase = unit;
            }
            // once we've found the most expensive thing we can buy here, record that
            budget -= currentPurchase.getCost();
            purchases.put(locShopList.getKey(), currentPurchase);
          }
        }
        // once we're satisfied with all our selections, put in the orders
        for( Entry<Location, UnitModel> lineItem : purchases.entrySet() )
        {
          GameAction action = new GameAction.UnitProductionAction(myCo, lineItem.getValue(),
              lineItem.getKey().getCoordinates());
          actions.offer(action);
        }
      }

      // Check for a turn-ending power
      if( actions.isEmpty() && !stateChange )
      {
        AIUtils.queueCromulentAbility(actions, myCo, CommanderAbility.PHASE_TURN_END);
      }

      // Return the next action, or null if actions is empty.
      nextAction = actions.poll();
    } while (nextAction == null && stateChange); // we don't want to end early, so if the state changed and we don't have an action yet, try again
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }
}
