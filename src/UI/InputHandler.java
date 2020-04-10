package UI;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
public class InputHandler
{
  public enum InputAction
  {
    NO_ACTION, UP, DOWN, LEFT, RIGHT, SEEK, SELECT, BACK
  };

  static Integer[] upDefaultKeyCodes = {KeyEvent.VK_UP, KeyEvent.VK_W};
  static Integer[] downDefaultKeyCodes = {KeyEvent.VK_DOWN, KeyEvent.VK_S};
  static Integer[] leftDefaultKeyCodes = {KeyEvent.VK_LEFT, KeyEvent.VK_A};
  static Integer[] rightDefaultKeyCodes = {KeyEvent.VK_RIGHT, KeyEvent.VK_D};
  static Integer[] selectDefaultKeyCodes = {KeyEvent.VK_ENTER, KeyEvent.VK_SPACE};
  static Integer[] backDefaultKeyCodes = {KeyEvent.VK_ESCAPE, KeyEvent.VK_BACK_SPACE};
  static Integer[] seekDefaultKeyCodes = {KeyEvent.VK_Q};

  static ArrayList<Integer> upKeyCodes = new ArrayList<Integer>(Arrays.asList(upDefaultKeyCodes));
  static ArrayList<Integer> downKeyCodes = new ArrayList<Integer>(Arrays.asList(downDefaultKeyCodes));
  static ArrayList<Integer> leftKeyCodes = new ArrayList<Integer>(Arrays.asList(leftDefaultKeyCodes));
  static ArrayList<Integer> rightKeyCodes = new ArrayList<Integer>(Arrays.asList(rightDefaultKeyCodes));
  static ArrayList<Integer> selectKeyCodes = new ArrayList<Integer>(Arrays.asList(selectDefaultKeyCodes));
  static ArrayList<Integer> backKeyCodes = new ArrayList<Integer>(Arrays.asList(backDefaultKeyCodes));
  static ArrayList<Integer> seekKeyCodes = new ArrayList<Integer>(Arrays.asList(seekDefaultKeyCodes));

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
    int keyCode = event.getKeyCode();
    if( upKeyCodes.contains(keyCode) )
    {
      ia = InputAction.UP;
    }
    else if( downKeyCodes.contains(keyCode) )
    {
      ia = InputAction.DOWN;
    }
    else if( leftKeyCodes.contains(keyCode) )
    {
      ia = InputAction.LEFT;
    }
    else if( rightKeyCodes.contains(keyCode) )
    {
      ia = InputAction.RIGHT;
    }
    else if( selectKeyCodes.contains(keyCode) )
    {
      ia = InputAction.SELECT;
    }
    else if( backKeyCodes.contains(keyCode) )
    {
      ia = InputAction.BACK;
    }
    else if( seekKeyCodes.contains(keyCode) )
    {
      ia = InputAction.SEEK;
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
