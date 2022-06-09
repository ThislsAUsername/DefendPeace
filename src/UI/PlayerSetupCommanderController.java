package UI;

import java.util.ArrayList;

import CommandingOfficers.CommanderInfo;
import Engine.Driver;
import Engine.IController;
import Engine.IView;
import Engine.OptionSelector;
import Engine.GameScenario.TagMode;
import UI.InputHandler.InputAction;

public class PlayerSetupCommanderController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  private ArrayList<CommanderInfo> cmdrInfos;
  private int noCmdrIndex;
  public final boolean shouldSelectMultiCO;
  public OptionSelector cmdrSelector;
  public OptionSelector tagIndex;
  public ArrayList<Integer> tagCmdrList;

  public PlayerSetupCommanderController(ArrayList<CommanderInfo> infos, PlayerSetupInfo playerInfo, TagMode tagMode)
  {
    cmdrInfos = infos;
    noCmdrIndex = infos.size() - 1;
    myPlayerInfo = playerInfo;
    shouldSelectMultiCO = tagMode.supportsMultiCmdrSelect;

    // Make sure we start with the cursor on the currently-selected Commander.
    cmdrSelector = new OptionSelector(infos.size());
    cmdrSelector.setSelectedOption(myPlayerInfo.coList.get(0));

    tagCmdrList = new ArrayList<>();
    tagCmdrList.addAll(myPlayerInfo.coList);
    if( shouldSelectMultiCO && noCmdrIndex != tagCmdrList.get(0) )
      tagCmdrList.add(noCmdrIndex); // Append a No CO

    tagIndex = new OptionSelector(tagCmdrList.size());
    tagIndex.setSelectedOption(0);
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case SELECT:
        // Apply change and return control.
        if( tagCmdrList.size() > 1 )
          tagCmdrList.remove(tagCmdrList.size() - 1); // Chop off the final No CO
        myPlayerInfo.coList = tagCmdrList;
        done = true;
        break;
      case UP:
      case DOWN:
      {
        int tagPicked = tagIndex.getSelectionNormalized();
        // Check for CO addition
        if( shouldSelectMultiCO && cmdrSelector.getSelectionNormalized() == noCmdrIndex )
        {
          tagCmdrList.add(noCmdrIndex);
          tagIndex.reset(tagCmdrList.size());
          tagIndex.setSelectedOption(tagPicked);
        }
        final int cmdrPicked = cmdrSelector.handleInput(action);
        tagCmdrList.set(tagPicked, cmdrPicked);
        // Check for CO deletion
        if( shouldSelectMultiCO && cmdrPicked == noCmdrIndex )
        {
          tagCmdrList.remove(tagPicked);
          tagIndex.reset(tagCmdrList.size());
          tagIndex.setSelectedOption(tagPicked);
          cmdrSelector.setSelectedOption(tagCmdrList.get(tagPicked));
        }
      }
        break;
      case LEFT:
      case RIGHT:
      {
        final int tagPicked = tagIndex.handleInput(action);
        final int cmdrPicked = tagCmdrList.get(tagPicked);
        cmdrSelector.setSelectedOption(cmdrPicked);
      }
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
      default:
        // Do nothing.
    }
    return done;
  }
}
