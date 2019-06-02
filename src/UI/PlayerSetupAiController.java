package UI;

import java.util.ArrayList;

import AI.AILibrary;
import AI.AIMaker;
import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupAiController implements IController
{
  PlayerSetupInfo myPlayerInfo;
  ArrayList<AIMaker> aiMakers;
  OptionSelector aiSelector;

  public PlayerSetupAiController(PlayerSetupInfo playerInfo)
  {
    myPlayerInfo = playerInfo;
    aiMakers = AILibrary.getAIList();
    aiSelector = new OptionSelector(aiMakers.size());
    aiSelector.setSelectedOption(myPlayerInfo.currentAi);
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case ENTER:
        // Apply changes and return control.
        myPlayerInfo.currentAi = aiSelector.getSelectionNormalized();
        done = true;
        break;
      case UP:
      case DOWN:
        aiSelector.handleInput(action);
        break;
      case BACK:
        // Cancel: return control without applying changes.
        done = true;
        break;
      case SEEK:
      case LEFT:
      case RIGHT:
      default:
        // Do nothing.
    }
    return done;
  }

  public int getSelectedAiIndex()
  {
    return aiSelector.getSelectionNormalized();
  }
}
