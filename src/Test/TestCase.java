package Test;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel.UnitEnum;

/**
 * Interface for building test cases for both unit tests and higher-level functionality as needed.
 */
public abstract class TestCase
{
  /**
   * Performs the designated test and returns the result.
   * @return true if the test passes, false otherwise.
   */
  public abstract boolean runTest();

  /**
   * Convenience function for use by all TestCases, to print a message if
   * the passed-in condition is false. 
   * @param condition The condition to test.
   * @param faultMsg The message to print if condition does not hold.
   * @return The condition that was passed in, to allow for logical operations on the condition. 
   */
  protected static boolean validate(boolean condition, String faultMsg)
  {
    if( !condition )
    {
      System.out.println(faultMsg);
    }
    return condition;
  }

  /**
   * Convenience function to create a new unit, add it to the GameMap, and get it back in one line.
   * @param map The map to which we want to add the unit.
   * @param co The Commander to whom the unit shall belon.
   * @param type The type of unit we are to construct.
   * @param x The X-location of the new unit.
   * @param y The Y-location of the new unit.
   * @return The newly-created unit.
   * 
   * NOTE: This function does not take into account existing units, so beware stomping.
   */
  protected static Unit addUnit(GameMap map, Commander co, UnitEnum type, int x, int y)
  {
    Unit u = new Unit(co, co.getUnitModel(type));
    map.addNewUnit(u, x, y);
    co.units.add( u );
    return u;
  }

  protected static void performGameAction( GameAction action, GameMap map )
  {
    GameEventQueue sequence = action.getGameEvents( map );
    for( GameEvent event : sequence )
    {
      event.performEvent( map );
    }
  }
}
