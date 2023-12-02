package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
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
import Units.UnitModel;

/**
 *  Just build tons of Infantry and try to rush the opponent.
 */
public class InfantrySpamAI implements AIController
{
  private static class instantiator implements AIMaker
  {
    @Override
    public AIController create(Army co)
    {
      return new InfantrySpamAI(co);
    }

    @Override
    public String getName()
    {
      return "ISAI";
    }

    @Override
    public String getDescription()
    {
      return
          "Infantry Spam AI (ISAI) knows there are two objectives in this game: Shoot things and capture things.\n" +
          "Infantry can do both, so why build anything else?";
    }
  }
  public static final AIMaker info = new instantiator();
  
  @Override
  public AIMaker getAIInfo()
  {
    return info;
  }
  
  Queue<GameAction> actions = new ArrayDeque<GameAction>();

  private Army myArmy = null;

  private ArrayList<XYCoord> unownedProperties;
  private ArrayList<XYCoord> capturingProperties;

  private StringBuffer logger = new StringBuffer();
  private boolean shouldLog = true;
  private int turnNum = 0;

  public InfantrySpamAI(Army army)
  {
    myArmy = army;
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    turnNum++;
    log(String.format("[======== ISAI initializing turn %s for %s =========]", turnNum, myArmy));

    // Make sure we don't have any hang-ons from last time.
    actions.clear();
    
    // Create a list of every property we don't own, but want to.
    unownedProperties = AIUtils.findNonAlliedProperties(myArmy, gameMap);
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
    log(String.format("[======== ISAI ending turn %s for %s =========]", turnNum, myArmy));
    if( shouldLog )
      System.out.println(logger.toString());
    logger = new StringBuffer();
  }

  private void log(String message)
  {
    if( shouldLog )
      logger.append(message).append('\n');
  }
  @Override
  public void setLogging(boolean value) { shouldLog = value; }

  @Override
  public GameAction getNextAction(GameMap gameMap)
  {
    // If we have more actions ready, don't bother calculating stuff.
    if( !actions.isEmpty() )
    {
      return actions.poll();
    }

    // Handle actions for each unit the CO owns.
    for( Unit unit : myArmy.getUnits() )
    {
      if( unit.isTurnOver || !gameMap.isLocationValid(unit.x, unit.y))
        continue; // No actions for units that are stale or out of bounds
      boolean foundAction = false;

      // Find the possible unit actions.
      Map<XYCoord, ArrayList<GameActionSet> > possibleActions = AIUtils.getAvailableUnitActions(unit, gameMap);

      for( XYCoord coord : possibleActions.keySet() )
      {
        // Figure out what I can do here.
        ArrayList<GameActionSet> actionSets = possibleActions.get(coord);
        for( GameActionSet actionSet : actionSets )
        {
          // See if we have the option to attack.
          if( actionSet.getSelected().getType() == UnitActionFactory.ATTACK )
          {
            actions.offer(actionSet.getSelected() );
            foundAction = true;
            break;
          }
          
          // Otherwise, see if we have the option to capture.
          if( actionSet.getSelected().getType() == UnitActionFactory.CAPTURE )
          {
            actions.offer(actionSet.getSelected() );
            capturingProperties.add(coord);
            foundAction = true;
            break;
          }
        }
        if(foundAction)break; // Only allow one action per unit.
      }
      if(foundAction)break; // Only one action per getNextAction() call, to avoid overlap.

      // If no attack/capture actions are available now, just move towards a non-allied building.
      Utils.sortLocationsByDistance( new XYCoord(unit.x, unit.y), unownedProperties);
      if( !unownedProperties.isEmpty() ) // Sanity check - it shouldn't be, unless this function is called after we win.
      {
        log(String.format("  Seeking a property to send %s after", unit.toStringWithLocation()));
        int index = 0;
        XYCoord goal = null;
        GamePath path = null;
        boolean validTarget = false;

        // Loop until we find a valid property to go capture or run out of options.
        do
        {
          goal = unownedProperties.get(index++);
          path = new Utils.PathCalcParams(unit, gameMap).setTheoretical().findShortestPath(goal);
          validTarget = (myArmy.isEnemy(gameMap.getLocation(goal).getOwner()) // Property is not allied.
                      && !capturingProperties.contains(goal)                // We aren't already capturing it.
                      && (path != null));                       // We can reach it.
          log(String.format("    %s at %s? %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal, (validTarget?"Yes":"No")));
        } while( !validTarget && (index < unownedProperties.size()) );      // Loop until we run out of properties to check.

        if( !validTarget )
        {
          log("    Failed to find a path to a capturable property. Waiting");
          // We couldn't find a valid move point (are we on an island?). Just give up.
          GameAction wait = new WaitLifecycle.WaitAction(unit, GamePath.stayPut(unit));
          actions.offer(wait);
          break;
        }

        log(String.format("    Selected %s at %s", gameMap.getLocation(goal).getEnvironment().terrainType, goal));

        GameAction move = AIUtils.moveTowardLocation(unit, goal, gameMap);
        if( null != move )
        {
          actions.offer(move);
          break;
        }
      }
    }

    // Check for an available buying enhancement power
    if( actions.isEmpty() )
    {
      AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_BUY);
    }
    
    // Finally, build more infantry. We will add all build commands at once, since they can't conflict.
    if( actions.isEmpty() )
    {
      int budget = myArmy.money;
      // Create a list of actions to build infantry on every open factory, then return these actions until done.
      for( int i = 0; i < gameMap.mapWidth; i++ )
      {
        for( int j = 0; j < gameMap.mapHeight; j++)
        {
          MapLocation loc = gameMap.getLocation(i, j);
          Commander buyer = loc.getOwner();
          if(null == buyer)
            continue;
          // If this terrain belongs to me, and I can build something on it, and I have the money, do so.
          if( loc.getEnvironment().terrainType == TerrainType.FACTORY && buyer.army == myArmy && loc.getResident() == null )
          {
            ArrayList<UnitModel> units = buyer.getShoppingList(loc);
            final int buyCost = buyer.getBuyCost(units.get(0), loc.getCoordinates());
            if( !units.isEmpty() && buyCost <= budget )
            {
              GameAction action = new GameAction.UnitProductionAction(buyer, units.get(0), loc.getCoordinates());
              budget -= buyCost;
              actions.offer( action );
            }
          }
        }
      }
    }

    // Check for a turn-ending power
    if( actions.isEmpty() )
    {
      AIUtils.queueCromulentAbility(actions, myArmy, CommanderAbility.PHASE_TURN_END);
    }

    // Return the next action, or null if actions is empty.
    GameAction nextAction = actions.poll();
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }
}
