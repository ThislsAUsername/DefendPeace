package Engine;

import java.util.ArrayList;

import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ResupplyEvent;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;

public interface TurnInitAction
{
  /**
   * Evaluates the state of the game relative to self, and adds any desired events
   * to the passed-in event queue. The event queue should not otherwise be modified.
   * @param self The unit to which this TurnInitAction is attached.
   * @param map The current game map.
   * @param events The output event queue. Only put, no take.
   */
  public abstract void initTurn(Unit self, GameMap map, GameEventQueue events);

  /**
   * Re-supplies fuel and ammunition for any friendly adjacent units.
   */
  public static class ResupplyAction implements TurnInitAction
  {
    @Override
    public void initTurn(Unit self, GameMap map, GameEventQueue events)
    {
      // Build an array of each adjacent location.
      ArrayList<Location> locations = new ArrayList<Location>();
      locations.add(map.getLocation(self.x, self.y - 1));
      locations.add(map.getLocation(self.x, self.y + 1));
      locations.add(map.getLocation(self.x - 1, self.y));
      locations.add(map.getLocation(self.x + 1, self.y));

      // For each location, see if there is a friendly unit to re-supply.
      for( Location loc : locations )
      {
        Unit other = loc.getResident();
        if( other != null && other.CO == self.CO )
        {
          events.add(new ResupplyEvent(other));
        }
      }
    }
  }
}
