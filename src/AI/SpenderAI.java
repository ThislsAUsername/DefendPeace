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

    // If the CO has enough AP, preload the CommanderAbilityAction.
    ArrayList<CommanderAbility> abilities = myCo.getReadyAbilities();
    if( abilities.size() > 0 )
    {
      actions.offer(new GameAction.AbilityAction(abilities.get(0)));
    }
  }

  @Override
  public void endTurn()
  {
    log(String.format("[======== SpAI ending turn %s for %s =========]", turnNum, myCo));
    System.out.println(logger.toString());
    logger = new StringBuffer();
  }

  private void log(String message)
  {
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
        return actions.poll();
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
        ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap);

        for( XYCoord coord : destinations )
        {
          // Figure out how to get here.
          Path movePath = Utils.findShortestPath(unit, coord, gameMap);

          // Figure out what I can do here.
          ArrayList<GameActionSet> actionSets = unit.getPossibleActions(gameMap, movePath);
          for( GameActionSet actionSet : actionSets )
          {
            // See if we have the option to attack.
            if( actionSet.getSelected().getType() == GameAction.ActionType.ATTACK )
            {
              actions.offer(actionSet.getSelected());
              foundAction = true;
              break;
            }

            // Otherwise, see if we have the option to capture.
            if( actionSet.getSelected().getType() == GameAction.ActionType.CAPTURE )
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

      Queue<Unit> waitQueue = new ArrayDeque<Unit>();

      // If no attack/capture actions are available now, just move towards a non-allied building.
      if( actions.isEmpty() && !stateChange )
      {
        while (!travelQueue.isEmpty())
        {
          Unit unit = travelQueue.poll();

          // Find the possible destinations.
          ArrayList<XYCoord> destinations = Utils.findPossibleDestinations(unit, gameMap);

          Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), unownedProperties);
          if( !unownedProperties.isEmpty() ) // Sanity check - it shouldn't be, unless this function is called after we win.
          {
            log(String.format("  Seeking a property to send %s after", unit.toStringWithLocation()));
            int index = 0;
            XYCoord goal = null;
            Path path = null;
            boolean validTarget = false;

            // Loop until we find a valid property to go capture or run out of options.
            do
            {
              goal = unownedProperties.get(index++);
              path = Utils.findShortestPath(unit, goal, gameMap, true);
              validTarget = (myCo.isEnemy(gameMap.getLocation(goal).getOwner()) // Property is not allied.
                  && !capturingProperties.contains(goal) // We aren't already capturing it.
                  && (path.getPathLength() > 0)); // We can reach it.
              log(String.format("    %s at %s? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal,
                  (validTarget ? "Yes" : "No")));
            } while (!validTarget && (index < unownedProperties.size())); // Loop until we run out of properties to check.

            if( !validTarget )
            {
              // if this unit can't go anywhere useful, consider having it just wait
              waitQueue.offer(unit);
            }
            else
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
              GameAction move = new GameAction.WaitAction(gameMap, unit, movePath);
              actions.offer(move);
              stateChange = true;
              break;
            }
          }
        }
      }

      // If we can't even move towards an objective, *then* we wait.
      if( actions.isEmpty() && !stateChange )
      {
        while (!waitQueue.isEmpty())
        {
          Unit unit = waitQueue.poll();
          log("    Failed to find a path to a capturable property. Waiting");
          GameAction wait = new GameAction.WaitAction(gameMap, unit, Utils.findShortestPath(unit, unit.x, unit.y, gameMap));
          actions.offer(wait);
          break;
        }
      }

      // We will add all build commands at once, since they can't conflict.
      if( actions.isEmpty() )
      {
        Map<Location, ArrayList<UnitModel>> shoppingLists = new HashMap<>();
        for( Location loc : myCo.ownedProperties )
        {
          // I like combat units that are useful, so we skip ports for now
          if( loc.getEnvironment().terrainType != TerrainType.SEAPORT && loc.getResident() == null )
          {
            ArrayList<UnitModel> units = myCo.getShoppingList(loc.getEnvironment().terrainType);
            // Only add to the list if we could actually buy something here.
            if( !units.isEmpty() && units.get(0).moneyCost <= myCo.money )
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
            if( unit.weaponModels != null && unit.weaponModels.length > 0 && unit.moneyCost <= budget )
            {
              budget -= unit.moneyCost;
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
            budget += currentPurchase.moneyCost;
            for( UnitModel unit : units )
            {
              if( budget > unit.moneyCost && unit.moneyCost > currentPurchase.moneyCost )
                currentPurchase = unit;
            }
            // once we've found the most expensive thing we can buy here, record that
            budget -= currentPurchase.moneyCost;
            purchases.put(locShopList.getKey(), currentPurchase);
          }
        }
        // once we're satisfied with all our selections, put in the orders
        for( Entry<Location, UnitModel> lineItem : purchases.entrySet() )
        {
          GameAction action = new GameAction.UnitProductionAction(gameMap, myCo, lineItem.getValue(),
              lineItem.getKey().getCoordinates());
          actions.offer(action);
        }
      }

      // Return the next action, or null if actions is empty.
      nextAction = actions.poll();
    } while (nextAction != null || !stateChange);
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }
}
