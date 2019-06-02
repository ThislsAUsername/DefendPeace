package UI;

import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupColorFactionController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  OptionSelector colorSelector;
  OptionSelector factionSelector;

  public PlayerSetupColorFactionController(PlayerSetupInfo playerInfo)
  {
    colorSelector = new OptionSelector(UIUtils.getCOColors().length);
    factionSelector = new OptionSelector(UIUtils.getFactions().length);

    // Start the selectors at the initial values.
    myPlayerInfo = playerInfo;
    colorSelector.setSelectedOption(myPlayerInfo.currentColor);
    factionSelector.setSelectedOption(myPlayerInfo.currentFaction);
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case ENTER:
        // Apply change and return control.
        myPlayerInfo.currentColor = colorSelector.getSelectionNormalized();
        myPlayerInfo.currentFaction = factionSelector.getSelectionNormalized();
        done = true;
        break;
      case UP:
      case DOWN:
        factionSelector.handleInput(action);
        break;
      case LEFT:
      case RIGHT:
        colorSelector.handleInput(action);
        break;
      case BACK:
        // Don't apply changes; just return control.
        done = true;
        break;
      default:
        // Do nothing.
    }
    return done;
  }

  public int getSelectedColor()
  {
    return colorSelector.getSelectionNormalized();
  }
  
  public int getSelectedFaction()
  {
    return factionSelector.getSelectionNormalized();
  }
}
