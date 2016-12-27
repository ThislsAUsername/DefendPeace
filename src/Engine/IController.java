package Engine;

import UI.InputHandler;

public interface IController
{
  /**
   * Take in and process 'action'. Return true if this controller requires no further input.
   * @param action The action the user has ordered.
   * @return true if this controller is done (game over, etc), false if more input is needed.
   */
  public boolean handleInput(InputHandler.InputAction action);
}
