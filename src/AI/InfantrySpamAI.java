package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.UnitModel;

/**
 *  Just build tons of Infantry and try to rush the opponent.
 */
public class InfantrySpamAI implements AIController
{
  Queue<GameAction> actions = new ArrayDeque<GameAction>();
  
  private Commander myCo = null;
  
  public InfantrySpamAI(Commander co)
  {
    myCo = co;
  }

  @Override
  public void initialize(GameMap gameMap)
  { /* No initialization needed */ }

  @Override
  public GameAction getNextAction(GameMap gameMap)
  {
    if( actions.isEmpty() )
    {
      // Create a list of actions to build infantry on every open factory, then return these actions until done.
      for( int i = 0; i < gameMap.mapWidth; i++ )
      {
        for( int j = 0; j < gameMap.mapHeight; j++)
        {
          Location loc = gameMap.getLocation(i, j);
          // If this terrain belongs to me, and I can build something on it, and I have the money, do so.
          if( loc.getEnvironment().terrainType == TerrainType.FACTORY && loc.getOwner() == myCo && loc.getResident() == null )
          {
            ArrayList<UnitModel> units = myCo.getShoppingList(loc.getEnvironment().terrainType);
            if( !units.isEmpty() && units.get(0).moneyCost <= myCo.money )
            {
              GameAction action = new GameAction.UnitProductionAction(gameMap, myCo, units.get(0), loc.getCoordinates());
              System.out.println(String.format("Producing %s at %s", units.get(0).toString(), loc.getCoordinates()));
              actions.offer( action );
            }
          }
        }
      }
    }

    // Return the next action, or null if actions is empty.
    return actions.poll();
  }

}
