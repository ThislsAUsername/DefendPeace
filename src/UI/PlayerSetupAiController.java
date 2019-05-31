package UI;

import java.util.ArrayList;

import AI.AILibrary;
import AI.AIMaker;
import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupAiController implements IController
{
  ArrayList<AIMaker> aiMakers;
  OptionSelector aiSelector;
  private int startingIndex;

  public PlayerSetupAiController(int currentIndex)
  {
    aiMakers = AILibrary.getAIList();;
    aiSelector = new OptionSelector(aiMakers.size());
    aiSelector.setSelectedOption(currentIndex);
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
        aiSelector.handleInput(action);
        break;
      case BACK:
        // Cancel: reset the option selectors to ensure we don't confuse the caller.
        done = true;
        aiSelector.setSelectedOption(startingIndex);
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
