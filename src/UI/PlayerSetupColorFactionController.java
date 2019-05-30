package UI;

import java.awt.Color;

import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupColorFactionController implements IController
{
  OptionSelector colorSelector;
  OptionSelector factionSelector;
  private Color startingColor;
  private UIUtils.Faction startingFaction;

  public PlayerSetupColorFactionController(Color startingColor, UIUtils.Faction startingFaction)
  {
    colorSelector = new OptionSelector(UIUtils.getCOColors().length);
    factionSelector = new OptionSelector(UIUtils.getFactions().length);

    // Start the selectors at the initial values.
    reset( startingColor, startingFaction );
  }

  public void reset(Color startingColor, UIUtils.Faction startingFaction)
  {
    // The number of factions/colors available should not change while the game is running, so don't bother updating those.
    this.startingColor = startingColor;
    this.startingFaction = startingFaction;
    setColorSelector(startingColor);
    setFactionSelector(startingFaction);
  }

  private void setColorSelector(Color color)
  {
    for(int i = 0; i < colorSelector.size(); ++i)
    {
      if( color.equals(UIUtils.getCOColors()[i]))
      {
        colorSelector.setSelectedOption(i);
      }
    }
  }

  private void setFactionSelector(UIUtils.Faction faction)
  {
    for(int i = 0; i < factionSelector.size(); ++i)
    {
      if( faction == UIUtils.getFactions()[i] )
      {
        factionSelector.setSelectedOption(i);
      }
    }
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
        factionSelector.handleInput(action);
        break;
      case LEFT:
      case RIGHT:
        colorSelector.handleInput(action);
        break;
      case BACK:
        // Cancel: reset the option selectors to ensure we don't confuse the caller.
        done = true;
        setColorSelector(startingColor);
        setFactionSelector(startingFaction);
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
