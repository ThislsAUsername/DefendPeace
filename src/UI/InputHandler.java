package UI;

import java.awt.event.KeyEvent;
public class InputHandler
{
  public enum InputAction
  {
    NO_ACTION, UP, DOWN, LEFT, RIGHT, SEEK, ENTER, BACK
  };

  // MovementInput variables
  static short upHeld = 0;
  static short downHeld = 0;
  static short leftHeld = 0;
  static short rightHeld = 0;

  /**
   * No reason to make an instance of this class.
   */
  private InputHandler()
  {}

  public static InputAction pressKey(KeyEvent key)
  {
    InputAction input = getActionFromKey(key);
    switch (input)
    {
      case UP:
        upHeld++;
        break;
      case DOWN:
        downHeld++;
        break;
      case LEFT:
        leftHeld++;
        break;
      case RIGHT:
        rightHeld++;
        break;
      default:
        break;
    }
    return input;
  }

  /**
   * Converts a keyboard event into an action that the rest of the game understands.
   * This can allow for re-binding keys, etc by changing the mapping.
   * @param event
   * @return
   */
  private static InputAction getActionFromKey(java.awt.event.KeyEvent event)
  {
    InputAction ia = InputAction.NO_ACTION;
    switch (event.getKeyCode())
    {
      case KeyEvent.VK_UP:
      case KeyEvent.VK_W:
        ia = InputAction.UP;
        break;
      case KeyEvent.VK_DOWN:
      case KeyEvent.VK_S:
        ia = InputAction.DOWN;
        break;
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_A:
        ia = InputAction.LEFT;
        break;
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_D:
        ia = InputAction.RIGHT;
        break;
      case KeyEvent.VK_ENTER:
      case KeyEvent.VK_SPACE:
        ia = InputAction.ENTER;
        break;
      case KeyEvent.VK_BACK_SPACE:
      case KeyEvent.VK_ESCAPE:
        ia = InputAction.BACK;
        break;
      case KeyEvent.VK_Q:
        ia = InputAction.SEEK;
        break;
    }
    return ia;
  }

  public static void releaseKey(KeyEvent e)
  {
    InputAction input = getActionFromKey(e);
    switch (input)
    {
      case UP:
        upHeld = 0;
        break;
      case DOWN:
        downHeld = 0;
        break;
      case LEFT:
        leftHeld = 0;
        break;
      case RIGHT:
        rightHeld = 0;
        break;
      default:
        break;
    }
  }

  public static boolean isUpHeld()
  {
    return upHeld > 1;
  }

  public static boolean isDownHeld()
  {
    return downHeld > 1;
  }

  public static boolean isLeftHeld()
  {
    return leftHeld > 1;
  }

  public static boolean isRightHeld()
  {
    return rightHeld > 1;
  }

}
