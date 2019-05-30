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
  ArrayList<CommanderInfo> cmdrInfos;
  OptionSelector cmdrSelector;
  private int startingIndex;

  public PlayerSetupCommanderController(ArrayList<CommanderInfo> infos, int currentIndex)
  {
    cmdrInfos = infos;
    cmdrSelector = new OptionSelector(infos.size());
    cmdrSelector.setSelectedOption(currentIndex);
    startingIndex = currentIndex;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case ENTER:
        done = true;
        break;
      case UP:
      case DOWN:
        cmdrSelector.handleInput(action);
        break;
      case BACK:
        // Cancel: reset the option selectors to ensure we don't confuse the caller.
        done = true;
        cmdrSelector.setSelectedOption(startingIndex);
        break;
      case SEEK:
        CO_InfoController coInfoMenu = new CO_InfoController(cmdrInfos);
        IView infoView = Driver.getInstance().gameGraphics.createInfoView(coInfoMenu);

        // Get the info menu to select the current CO
        for( int i = 0; i < cmdrSelector.getSelectionNormalized(); i++ )
        {
          coInfoMenu.handleInput(UI.InputHandler.InputAction.DOWN);
        }

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

  public int getSelectedCommander()
  {
    return cmdrSelector.getSelectionNormalized();
  }
}
