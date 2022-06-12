package Test;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.XYCoord;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModelScheme;

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
   * Convenience function to create a new unit, add it to the MapMaster, and get it back in one line.
   * @param map The map to which we want to add the unit.
   * @param co The Commander to whom the unit shall belong.
   * @param type The type of unit we are to construct.
   * @param x The X-location of the new unit.
   * @param y The Y-location of the new unit.
   * @return The newly-created unit.
   * 
   * NOTE: This function does not take into account existing units, so beware stomping.
   */
  protected static Unit addUnit(MapMaster map, Commander co, long type, int x, int y)
  {
    Unit u = new Unit(co, co.getUnitModel(type, false));
    map.addNewUnit(u, x, y);
    co.units.add( u );
    return u;
  }

  /**
   * Convenience function to create a new unit, add it to the map, and return it.
   * @param map The map to which we want to add the unit.
   * @param co The Commander to whom the unit shall belong.
   * @param typename The name of the desired unit as specified in the UnitModel.
   * @param xyc Where to place the new unit.
   * @return The newly-created unit.
   */
  protected static Unit addUnit(MapMaster map, Commander co, String typename, XYCoord xyc)
  {
    return addUnit(map, co, typename, xyc.xCoord, xyc.yCoord);
  }

  /**
   * Convenience function to create a new unit, add it to the map, and return it.
   * @param map The map to which we want to add the unit.
   * @param co The Commander to whom the unit shall belong.
   * @param typename The name of the desired unit as specified in the UnitModel.
   * @param x The X-location of the new unit.
   * @param y The Y-location of the new unit.
   * @return The newly-created unit.
   */
  protected static Unit addUnit(MapMaster map, Commander co, String typename, int x, int y)
  {
    Unit u = new Unit(co, UnitModelScheme.getModelFromString(typename, co.unitModels));
    map.addNewUnit(u, x, y);
    co.units.add( u );
    return u;
  }

  /** Attempts to execute the given action. Returns true if it succeeds, false otherwise. */
  protected static boolean performGameAction( GameAction action, GameInstance game )
  {
    GameEventQueue sequence = action.getEvents(game.gameMap);
    if( sequence.size() == 0 ) return false;
    performEvents(game, sequence);
    return true;
  }

  public static void performEvents(GameInstance game, GameEventQueue sequence)
  {
    for( GameEvent event : sequence )
    {
      event.performEvent( game.gameMap );
      GameEventListener.publishEvent(event, game);
    }
  }
}
