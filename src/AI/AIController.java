package AI;

import Engine.GameAction;
import Terrain.GameMap;

public interface AIController
{
  /** Called at the beginning of each turn. */
  public void initTurn(GameMap gameMap);

  /** Called at the end of each turn. */
  public void endTurn();

  /**
   * This is called during an AIPlayer's turn to request the player's actions.
   * @param gameMap The current state of the game.
   * @return A GameAction to execute, or null if the AIPlayer is ready to end its turn.
   */
  public GameAction getNextAction(GameMap gameMap);
  
  public AIMaker getAIInfo();
}
