package AI;

import Engine.GameAction;
import Terrain.GameMap;

public interface AIController
{
  /** Called once when the game begins. */
  public void initialize(GameMap gameMap);

  /**
   * This is called during an AIPlayer's turn to request the player's actions.
   * @param gameMap The current state of the game.
   * @return A GameAction to execute, or null if the AIPlayer is ready to end its turn.
   */
  public GameAction getNextAction(GameMap gameMap);
}
