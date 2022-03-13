package AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import CommandingOfficers.CommanderAbility;
import Engine.Army;
import Engine.GameAction;
import Engine.Utils;
import Engine.XYCoord;
import Engine.UnitActionLifecycles.CaptureLifecycle;
import Terrain.GameMap;
import Units.Unit;

/**
 * Base class for inversion of control of AI behaviors/composition
 */
public abstract class ModularAI implements AIController
{
  protected Army myArmy = null;

  // Updated on turn init
  protected ArrayList<XYCoord> unownedProperties;
  // List of possible AI modes, in order of precedence
  protected ArrayList<AIModule> aiPhases;
  // Sets the ordering for units in the unit queue fed to the modules
  protected Comparator<Unit> unitOrderSetter = new AIUtils.UnitCostComparator(false);

  private StringBuffer logger = new StringBuffer();
  private boolean shouldLog = true;
  protected int turnNum = 0;

  public ModularAI(Army army)
  {
    myArmy = army;
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    logger = new StringBuffer(); // Reset at the start of the turn so the AI's action log stays in memory between turns for review
    ++turnNum;
    // Create a list of every property we don't own, but want to.
    unownedProperties = AIUtils.findNonAlliedProperties(myArmy, gameMap);

    for( AIModule phase : aiPhases )
    {
      phase.initTurn(gameMap);
    }
  }

  @Override
  public void endTurn()
  {
    for( AIModule phase : aiPhases )
    {
      phase.endTurn();
    }
  }

  protected void log(String message)
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
    ArrayList<Unit> eligibleUnits = new ArrayList<Unit>();
    for( Unit unit : myArmy.getUnits() )
    {
      if( unit.isTurnOver || !gameMap.isLocationValid(unit.x, unit.y) )
        continue; // No actions for units that are stale or out of bounds.
      eligibleUnits.add(unit);
    }

    GameAction nextAction = null;
    PriorityQueue<Unit> unitQueue = new PriorityQueue<Unit>(Math.max(1, eligibleUnits.size()), unitOrderSetter);

    // Each module gets a shot at the whole unit queue, in order.
    // If no action's found, we're done
    for( AIModule phase : aiPhases )
    {
      unitQueue.clear();
      unitQueue.addAll(eligibleUnits);

      nextAction = phase.getNextAction(unitQueue, gameMap);
      if( null != nextAction )
        break;
    }
    log(String.format("Action: %s", nextAction));
    return nextAction;
  }

  public static interface AIModule extends Serializable
  {
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap map);
    public default void initTurn(GameMap gameMap) {}
    public default void endTurn() {}
  }

  public static class PowerActivator implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myCo;
    public final int aiPhase;

    public PowerActivator(Army co, int phase)
    {
      myCo = co;
      aiPhase = phase;
    }

    public boolean checked = false;
    @Override
    public void initTurn(GameMap gameMap) { checked = false; }
    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap map)
    {
      if( checked )
        return null;
      checked = true;

      CommanderAbility retVal = AIUtils.queueCromulentAbility(null, myCo, aiPhase);
      if( null != retVal )
        return new GameAction.AbilityAction(retVal);
      return null;
    }
  }

  public static class CaptureFinisher extends UnitActionFinder
  {
    private static final long serialVersionUID = 1L;
    public CaptureFinisher(Army co, ModularAI ai)
    {
      super(co, ai);
    }

    @Override
    public GameAction getUnitAction(Unit unit, GameMap map)
    {
      if( unit.getCaptureProgress() > 0 )
      {
        XYCoord position = new XYCoord(unit.x, unit.y);
        ai.unownedProperties.remove(position);
        return new CaptureLifecycle.CaptureAction(map, unit, Utils.findShortestPath(unit, position, map));
      }
      return null;
    }
  }

  public static abstract class UnitActionFinder implements AIModule
  {
    private static final long serialVersionUID = 1L;
    public Army myArmy;
    public final ModularAI ai;

    public UnitActionFinder(Army co, ModularAI ai)
    {
      myArmy = co;
      this.ai = ai;
    }

    @Override
    public GameAction getNextAction(PriorityQueue<Unit> unitQueue, GameMap gameMap)
    {
      for( Unit unit : unitQueue )
      {
        GameAction retVal = getUnitAction(unit, gameMap);
        if( null != retVal )
          return retVal;
      }
      return null;
    }

    protected abstract GameAction getUnitAction(Unit unit, GameMap gameMap);
  }
}
