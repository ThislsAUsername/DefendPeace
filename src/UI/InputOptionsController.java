package UI;

import java.util.Arrays;
import Engine.ConfigUtils;
import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class InputOptionsController implements IController
{
  private static final String OTHER_INPUT_FILENAME = Engine.Driver.JAR_DIR + "res/other_input.txt";

  public enum HideAbilityPreviews
  {
    Never, In_Fog, Hidden_Units;
    @Override
    public String toString()
    {
      return super.toString().replace("_", " ");
    }
  }
  /**
   *  Compare with {@link CombatContext.CalcType}
   */
  public static enum InputCalcType
  {
    NO_LUCK, PESSIMISTIC, OPTIMISTIC;
    @Override
    public String toString()
    {
      return super.toString().replace("_", " ");
    }
  };

  public static GameOptionBool seekBuildingsLastOption = new GameOptionBool("Seek Units First", true);
  public static GameOption<HideAbilityPreviews> previewFogPowersOption
                           = new GameOption<HideAbilityPreviews>("Hide Ability Previews", HideAbilityPreviews.values(), 2);
  public static GameOption<InputCalcType> damagePreviewTypeOption
                           = new GameOption<InputCalcType>("Damage Preview", InputCalcType.values(), 1);
  public static GameOption<?>[] allOptions = { seekBuildingsLastOption, previewFogPowersOption, damagePreviewTypeOption };
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
