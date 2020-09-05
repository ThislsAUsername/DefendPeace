package UI;

import java.util.Arrays;
import Engine.ConfigUtils;
import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class InputOptionsController implements IController
{
  private static final String OTHER_INPUT_FILENAME = "res/other_input.txt";

  public static GameOptionBool seekBuildingsLastOption = new GameOptionBool("Seek Units First", true);
  public static GameOption<?>[] allOptions = { seekBuildingsLastOption };
  public static OptionSelector actionCommandSelector = new OptionSelector( allOptions.length );

  static
  {
    if( !ConfigUtils.readConfigs(OTHER_INPUT_FILENAME, Arrays.asList(allOptions)) )
      System.out.println("Unable to read extra game input options from file.");
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;
    int kb = actionCommandSelector.getSelectionNormalized();

    switch(action)
    {
      case SELECT:
          allOptions[kb].storeCurrentValue();
        break;
      case BACK:
        if( !ConfigUtils.writeConfigs(OTHER_INPUT_FILENAME, Arrays.asList(allOptions)) )
          System.out.println("Unable to write extra game input options to file.");
        exitMenu = true;
        break;
      case DOWN:
      case UP:
        actionCommandSelector.handleInput(action);
        break;
      case LEFT:
      case RIGHT:
          allOptions[kb].handleInput(action);
        break;
        default:
          System.out.println("Warning: Unsupported input " + action + " in InputHandler.");
    }

    return exitMenu;
  }
}
