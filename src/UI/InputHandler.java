package UI;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import Engine.ConfigUtils;
import Engine.IController;
import Engine.OptionSelector;
import Engine.Pair;

public class InputHandler implements IController
{
  private static final String KEYS_FILENAME = "res/keys.txt";

  public static OptionSelector actionCommandSelector = new OptionSelector( InputHandler.InputAction.values().length );
  public static OptionSelector actionKeySelector = new OptionSelector(1); // We will just use the absolute, and normalize per action.

  public enum InputAction
  {
    UP, DOWN, LEFT, RIGHT, SEEK, SELECT, BACK
  };

  static ArrayList<Integer> upDefaultKeyCodes = new ArrayList<Integer>(Arrays.asList(KeyEvent.VK_UP, KeyEvent.VK_W));
  static ArrayList<Integer> downDefaultKeyCodes = new ArrayList<Integer>(Arrays.asList(KeyEvent.VK_DOWN, KeyEvent.VK_S));
  static ArrayList<Integer> leftDefaultKeyCodes = new ArrayList<Integer>(Arrays.asList(KeyEvent.VK_LEFT, KeyEvent.VK_A));
  static ArrayList<Integer> rightDefaultKeyCodes = new ArrayList<Integer>(Arrays.asList(KeyEvent.VK_RIGHT, KeyEvent.VK_D));
  static ArrayList<Integer> selectDefaultKeyCodes = new ArrayList<Integer>(Arrays.asList(KeyEvent.VK_ENTER, KeyEvent.VK_SPACE));
  static ArrayList<Integer> backDefaultKeyCodes = new ArrayList<Integer>(Arrays.asList(KeyEvent.VK_ESCAPE, KeyEvent.VK_BACK_SPACE));
  static ArrayList<Integer> seekDefaultKeyCodes = new ArrayList<Integer>(Arrays.asList(KeyEvent.VK_Q));

  static HashMap<InputAction, ArrayList<Integer> > bindingsByInputAction;
  static {
    bindingsByInputAction = new HashMap<InputAction, ArrayList<Integer> >();

    // Load keys file if it exists
    boolean loadedFile = ConfigUtils.readConfigLists(KEYS_FILENAME,
                                                    (String s)->InputAction.valueOf(s),
                                                    (Scanner linescan)->
                                                    {
                                                      ArrayList<Integer> actionCodes = new ArrayList<Integer>();
                                                      while(linescan.hasNext())
                                                      {
                                                        actionCodes.add(linescan.nextInt());
                                                      }
                                                      return actionCodes;
                                                    },
                                                    bindingsByInputAction);
    if( loadedFile )
    {
      // Make sure we have assigned all possible InputActions before we declare success.
      if( bindingsByInputAction.size() != InputAction.values().length )
      {
        System.out.println("Saved key file did not have values for all actions! Reverting to defaults.");
        loadedFile = false;
      }
    }

    if( !loadedFile )
    {
      // Otherwise just set defaults
      bindingsByInputAction.put(InputAction.UP, upDefaultKeyCodes);
      bindingsByInputAction.put(InputAction.DOWN, downDefaultKeyCodes);
      bindingsByInputAction.put(InputAction.LEFT, leftDefaultKeyCodes);
      bindingsByInputAction.put(InputAction.RIGHT, rightDefaultKeyCodes);
      bindingsByInputAction.put(InputAction.SELECT, selectDefaultKeyCodes);
      bindingsByInputAction.put(InputAction.BACK, backDefaultKeyCodes);
      bindingsByInputAction.put(InputAction.SEEK, seekDefaultKeyCodes);
    }
  }

  // MovementInput variables
  static short upHeld = 0;
  static short downHeld = 0;
  static short leftHeld = 0;
  static short rightHeld = 0;

  private static int lastKeyPressedCode = 0;

  // Will be true when we are waiting to input a key assignment.
  private static boolean assigningKey = false;

  private InputHandler(){}
  private static InputHandler singleton;
  public static InputHandler getInstance()
  {
    if( null == singleton )
      singleton = new InputHandler();
    return singleton;
  }

  public static InputAction pressKey(KeyEvent key)
  {
    lastKeyPressedCode = key.getKeyCode();
    InputAction input = getActionFromKey(key);
    if( assigningKey )
    {
      // Assign the new key.
      int kb = actionCommandSelector.getSelectionNormalized();
      InputHandler.InputAction assignedAction = InputHandler.InputAction.values()[kb];
      InputHandler.bindLastPressedKey(assignedAction);
      assigningKey = false;

      // Ensure this key press isn't also interpreted as an action topside.
      input = null;
    }

    if( null != input )
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
    InputAction ia = null;
    int keyCode = event.getKeyCode();
    if( bindingsByInputAction.get(InputAction.UP).contains(keyCode) )
    {
      ia = InputAction.UP;
    }
    else if( bindingsByInputAction.get(InputAction.DOWN).contains(keyCode) )
    {
      ia = InputAction.DOWN;
    }
    else if( bindingsByInputAction.get(InputAction.LEFT).contains(keyCode) )
    {
      ia = InputAction.LEFT;
    }
    else if( bindingsByInputAction.get(InputAction.RIGHT).contains(keyCode) )
    {
      ia = InputAction.RIGHT;
    }
    else if( bindingsByInputAction.get(InputAction.SELECT).contains(keyCode) )
    {
      ia = InputAction.SELECT;
    }
    else if( bindingsByInputAction.get(InputAction.BACK).contains(keyCode) )
    {
      ia = InputAction.BACK;
    }
    else if( bindingsByInputAction.get(InputAction.SEEK).contains(keyCode) )
    {
      ia = InputAction.SEEK;
    }
    return ia;
  }

  public static void releaseKey(KeyEvent e)
  {
    InputAction input = getActionFromKey(e);
    if( null != input )
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

  public static ArrayList<String> getBoundKeyNames(InputAction action)
  {
    ArrayList<String> keyNames = new ArrayList<String>();
    if( bindingsByInputAction.containsKey(action) )
    {
      for( int keyCode : bindingsByInputAction.get(action) )
      {
        keyNames.add(KeyEvent.getKeyText(keyCode));
      }
    }
    else
    {
      keyNames.add("NO ACTION");
    }
    return keyNames;
  }

  public static ArrayList<Integer> getBoundKeyCodes(InputAction inputAction)
  {
    if( bindingsByInputAction.containsKey(inputAction) )
    {
      return bindingsByInputAction.get(inputAction);
    }
    return new ArrayList<Integer>();
  }

  public static boolean unbindKey(InputAction action, Integer keyCode)
  {
    boolean removed = true;
    ArrayList<Integer> bindings = bindingsByInputAction.get(action);
    if(bindings.size() > 1) // Ensure we don't remove the last key for this command.
    {
      removed = bindings.remove(keyCode);
    }
    else
    {
      System.out.println("Warning! Cannot unbind key as it would leave no keys for " + action);
    }
    return removed;
  }

  public static boolean bindLastPressedKey(InputAction action)
  {
    int keyCode = lastKeyPressedCode;
    // First, check if another action is assigned this KeyCode.
    InputAction priorKeyAction = null;
    for(InputAction ia : InputAction.values())
    {
      ArrayList<Integer> bindings = bindingsByInputAction.get(ia);
      if( bindings.contains(keyCode) )
      {
        if( bindings.size() < 2 )
        {
          // Attempting to add a key for this command, except this key is already
          // assigned to another command that has no alternate key. Stop.
          System.out.println("Warning! Cannot reassign key as it would leave no keys for " + ia);
          return false;
        }
        else
        {
          priorKeyAction = ia;
        }
      }
    }

    if(priorKeyAction != null)
    {
      Integer ikey = keyCode;
      bindingsByInputAction.get(priorKeyAction).remove(ikey);
    }
    bindingsByInputAction.get(action).add(keyCode);

    return true;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;
    int kb = actionCommandSelector.getSelectionNormalized();

    switch(action)
    {
      case SELECT:
        // If the currently-selected item is an existing key command, remove it.
        InputAction selectedAction = InputHandler.InputAction.values()[kb];
        OptionSelector actionKeys = getKeySelector(selectedAction);
        int keyIndex = actionKeys.getSelectionNormalized();
        if( keyIndex == actionKeys.size()-1 )
        {
          // Then this is the "Add" option.
          assigningKey = true;
        }
        else
        {
          Integer actualKeyCode = InputHandler.getBoundKeyCodes(selectedAction).get(keyIndex);
          InputHandler.unbindKey(selectedAction, actualKeyCode);
        }
        break;
      case BACK:
        // Write keys file.
        List<Pair<InputAction, String>> configItems = new ArrayList<Pair<InputAction,String>>();
        for( InputAction key : bindingsByInputAction.keySet() )
        {
          String val = "";
          for( Integer code : bindingsByInputAction.get(key) )
          {
            val += " " + code;
          }
          // substring to remove the first space
          configItems.add(Pair.from(key, val.substring(1)));
        }
        ConfigUtils.writeConfigItems(KEYS_FILENAME, configItems);
        exitMenu = true;
        break;
      case DOWN:
      case UP:
        actionCommandSelector.handleInput(action);
        break;
      case LEFT:
      case RIGHT:
          actionKeySelector.handleInput(action);
        break;
        default:
          System.out.println("Warning: Unsupported input " + action + " in InputHandler.");
    }

    return exitMenu;
  }

  public boolean isAssigningKey()
  {
    return assigningKey;
  }

  public OptionSelector getKeySelector(InputAction action)
  {
    OptionSelector keySelector = new OptionSelector(InputHandler.getBoundKeyCodes(action).size() + 1); // +1 for Add
    keySelector.setSelectedOption(actionKeySelector.getSelectionAbsolute());
    return keySelector;
  }
}
