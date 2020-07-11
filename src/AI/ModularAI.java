package AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.XYCoord;
import Terrain.GameMap;
import Units.Unit;

/**
 * Base class for inversion of control of AI behaviors/composition
 */
public abstract class ModularAI implements AIController
{
  protected Commander myCo = null;

  // Updated on turn init
  protected ArrayList<XYCoord> unownedProperties;
  // List of possible AI modes, in order of precedence
  protected ArrayList<AIModule> aiPhases;
  // Used to determine what the default priority for unit actions is
  protected Comparator<Unit> unitOrderSetter = new AIUtils.UnitCostComparator(false);

  private StringBuffer logger = new StringBuffer();
  protected int turnNum = 0;

  public ModularAI(Commander co)
  {
    myCo = co;
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    ++turnNum;
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
  }

  protected void log(String message)
  {
    System.out.println(message);
    logger.append(message).append('\n');
  }

  @Override
  public GameAction getNextAction(GameMap gameMap)
  {
    ArrayList<Unit> eligibleUnits = new ArrayList<Unit>();
    for( Unit unit : myCo.units )
    {
      if( unit.isTurnOver || !gameMap.isLocationValid(unit.x, unit.y))
        continue; // No actions for units that are stale or out of bounds.
      eligibleUnits.add(unit);
    }

    GameAction nextAction = null;
    PriorityQueue<Unit> unitQueue = new PriorityQueue<Unit>(13, unitOrderSetter);
    
    // Each module gets a shot at the whole unit queue, in order.
    // If no action's found, we're done
    for( AIModule phase : aiPhases )
    {
      unitQueue.clear();
      unitQueue.addAll(eligibleUnits);

      nextAction = phase.getNextAction(unitQueue);
      if( null != nextAction)
        break;
    }
    log(String.format("  Action: %s", nextAction));
    return nextAction;
  }

  public static interface AIModule extends Serializable
  {
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue);
  }
}
