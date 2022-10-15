package UI;

import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupColorFactionController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  OptionSelector colorSelector;
  OptionSelector factionSelector;
  public final String iconicUnitName;

  public PlayerSetupColorFactionController(PlayerSetupInfo playerInfo, String unitName)
  {
    colorSelector = new OptionSelector(UIUtils.getCOColors().length);
    factionSelector = new OptionSelector(UIUtils.getFactions().length);

    // Start the selectors at the initial values.
    myPlayerInfo = playerInfo;
    colorSelector.setSelectedOption(myPlayerInfo.coList.get(0).color);
    factionSelector.setSelectedOption(myPlayerInfo.coList.get(0).faction);
    
    iconicUnitName = unitName;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case SELECT:
        // Apply change and return control.
        myPlayerInfo.coList.get(0).color = colorSelector.getSelectionNormalized();
        myPlayerInfo.coList.get(0).faction = factionSelector.getSelectionNormalized();
        done = true;
        break;
      case SEEK:
        // Flip units
        myPlayerInfo.flipUnits = !myPlayerInfo.flipUnits;
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

  public boolean getShouldFlip()
  {
    return myPlayerInfo.flipUnits;
  }
}
