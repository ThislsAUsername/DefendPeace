package UI;

import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupColorFactionController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  public final boolean shouldSelectMultiCO;
  OptionSelector colorSelector;
  OptionSelector factionSelector;
  public int[] colorPicks, factionPicks;
  public int tagIndex;
  public final String iconicUnitName;

  public PlayerSetupColorFactionController(PlayerSetupInfo playerInfo, String unitName)
  {
    colorSelector = new OptionSelector(UIUtils.getCOColors().length);
    factionSelector = new OptionSelector(UIUtils.getFactions().length);

    final int coCount = playerInfo.coList.size();
    colorPicks   = new int[coCount];
    factionPicks = new int[coCount];
    for(int i = 0; i < coCount; ++i)
    {
      colorPicks[i]   = playerInfo.coList.get(i).color;
      factionPicks[i] = playerInfo.coList.get(i).faction;
    }

    // Start the selectors at the initial values.
    myPlayerInfo = playerInfo;
    shouldSelectMultiCO = coCount > 1; // Don't need the TagMode since we only assign stuff for as many COs as we have
    tagIndex = 0;
    colorSelector.setSelectedOption(colorPicks[tagIndex]);
    factionSelector.setSelectedOption(factionPicks[tagIndex]);

    iconicUnitName = unitName;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case SELECT:
        colorPicks[tagIndex]   = colorSelector  .getSelectionNormalized();
        factionPicks[tagIndex] = factionSelector.getSelectionNormalized();
        ++tagIndex;
        // If we have more tag COs, stick around and handle those...
        if( tagIndex < colorPicks.length )
        {
          // Navigate the cursor to the current value of that index's position
          // Is this good UX? Nobody knows!
          colorSelector  .setSelectedOption(colorPicks  [tagIndex]);
          factionSelector.setSelectedOption(factionPicks[tagIndex]);
        }
        else // Otherwise, apply change and return control.
        {
          for( int i = 0; i < colorPicks.length; ++i )
          {
            myPlayerInfo.coList.get(i).color   = colorPicks  [i];
            myPlayerInfo.coList.get(i).faction = factionPicks[i];
          }
          done = true;
        }
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
        // Don't apply changes
        --tagIndex;
        if( tagIndex < 0 )
          done = true;
        else // If we're not backing out, point the cursor at the new-old selection
        {
          colorSelector  .setSelectedOption(colorPicks  [tagIndex]);
          factionSelector.setSelectedOption(factionPicks[tagIndex]);
        }
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
