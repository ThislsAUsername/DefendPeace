package UI;

import java.util.ArrayList;

import CommandingOfficers.CommanderInfo;
import Engine.Driver;
import Engine.IController;
import Engine.IView;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupCommanderController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  private ArrayList<CommanderInfo> cmdrInfos;
  public OptionSelector cmdrSelector;

  public PlayerSetupCommanderController(ArrayList<CommanderInfo> infos, PlayerSetupInfo playerInfo)
  {
    cmdrInfos = infos;
    myPlayerInfo = playerInfo;

    // Make sure we start with the cursor on the currently-selected Commander.
    cmdrSelector = new OptionSelector(infos.size());
    cmdrSelector.setSelectedOption(myPlayerInfo.currentCo);
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case ENTER:
        // Apply change and return control.
        myPlayerInfo.currentCo = cmdrSelector.getSelectionNormalized();
        done = true;
        break;
      case UP:
      case DOWN:
        cmdrSelector.handleInput(action);
        break;
      case BACK:
        // Cancel: return control without applying changes.
        done = true;
        break;
      case SEEK:
        CO_InfoController coInfoMenu = new CO_InfoController(cmdrInfos, cmdrSelector.getSelectionNormalized());
        IView infoView = Driver.getInstance().gameGraphics.createInfoView(coInfoMenu);

        // Give the new controller/view the floor
        Driver.getInstance().changeGameState(coInfoMenu, infoView);
        break;
      case LEFT:
      case RIGHT:
      default:
        // Do nothing.
    }
    return done;
  }
}
