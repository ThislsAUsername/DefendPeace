package UI;

import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.*;
import Engine.Driver;
import Engine.IController;
import Engine.IView;
import Engine.OptionSelector;
import Engine.GameScenario.TagMode;
import UI.InputHandler.InputAction;
import UI.PlayerSetupInfo.CODeets;
import UI.UIUtils.COSpriteSpec;

public class PlayerSetupCommanderController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  private ArrayList<CommanderInfo> cmdrInfos;
  public final boolean shouldSelectMultiCO;
  public boolean amPickingTagIndex;
  private final int noCmdr;

  // Range: [0, tag count], to handle the "done" button.
  public OptionSelector tagIndexSelector;

  public ArrayList<ArrayList<Integer>> cmdrBins;
  public ArrayList<COSpriteSpec> binColorSpec;
  public OptionSelector cmdrBinSelector;
  public OptionSelector cmdrInBinSelector;
  public ArrayList<Integer> tagCmdrList;
  public int rightGlueColumn;

  public PlayerSetupCommanderController(ArrayList<CommanderInfo> infos, PlayerSetupInfo playerInfo, TagMode tagMode)
  {
    cmdrInfos = infos;
    noCmdr = infos.size() - 1;
    myPlayerInfo = playerInfo;
    shouldSelectMultiCO = tagMode.supportsMultiCmdrSelect;
    amPickingTagIndex = shouldSelectMultiCO;

    tagCmdrList = new ArrayList<>();
    for( CODeets deets : myPlayerInfo.coList )
      tagCmdrList.add(deets.co);

    final int firstCO = tagCmdrList.get(0);

    // Pad with an extra No CO so we can add tag partners
    if( shouldSelectMultiCO && noCmdr != firstCO )
      tagCmdrList.add(noCmdr);

    cmdrBins = new ArrayList<>();
    binColorSpec = new ArrayList<>();

    int lastBin = -1;
    int startBin = 0;
    int startBinIndex = 0;

    // Set up our bins - each one contains a NO CO + all the COs from one canon faction
    int coIndex = 0;
    // List of what bins we've already created, so we don't depend on specific ordering/structure in the CO list
    HashMap<COSpriteSpec, Integer> factionIndex = new HashMap<>();
    for (; coIndex < infos.size(); ++coIndex)
    {
      CommanderInfo info = infos.get(coIndex);
      int binIndex;
      final COSpriteSpec canonFaction = info.baseFaction;
      if( factionIndex.containsKey(canonFaction) )
        binIndex = factionIndex.get(canonFaction);
      else
      {
        ++lastBin;
        binIndex = lastBin;
        factionIndex.put(canonFaction, binIndex);

        ArrayList<Integer> bin = new ArrayList<>();
        cmdrBins.add(bin);
        binColorSpec.add(canonFaction);
      }

      cmdrBins.get(binIndex).add(coIndex);
      if( firstCO == coIndex ) // Select the last CO in the tag by default
      {
        startBin = binIndex;
        startBinIndex = cmdrBins.get(binIndex).size()-1;
      }
    }

    tagIndexSelector = new OptionSelector(1);
    syncTagIndexSelector();

    cmdrBinSelector = new OptionSelector(lastBin + 1);
    cmdrBinSelector.setSelectedOption(startBin);
    cmdrInBinSelector = new OptionSelector(cmdrBins.get(startBin).size());
    cmdrInBinSelector.setSelectedOption(startBinIndex);
    rightGlueColumn = startBinIndex;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;
    if( amPickingTagIndex )
      exitMenu = handleTagChoiceInput(action);
    else
      exitMenu = handleCmdrChoiceInput(action);
    return exitMenu;
  }

  private boolean handleTagChoiceInput(InputAction action)
  {
    boolean done = false;
    final int selTagIndex = tagIndexSelector.getSelectionNormalized();
    switch(action)
    {
      case SELECT:
        amPickingTagIndex = false;

        if( selTagIndex >= tagCmdrList.size() )
        {
          // User says we're done - apply changes and get out.

          // Handle the pesky No CO at the end.
          if( 1 < tagCmdrList.size() )
            tagCmdrList.remove(tagCmdrList.size() - 1);

          // Ensure the array lengths match
          while (myPlayerInfo.coList.size() > tagCmdrList.size())
            myPlayerInfo.coList.remove(myPlayerInfo.coList.size() - 1);
          while (myPlayerInfo.coList.size() < tagCmdrList.size())
          {
            CODeets deets = new CODeets();
            deets.color   = myPlayerInfo.coList.get(0).color;
            deets.faction = myPlayerInfo.coList.get(0).faction;
            myPlayerInfo.coList.add(deets);
          }

          // Apply our choices
          for( int i = 0; i < myPlayerInfo.coList.size(); ++i )
            myPlayerInfo.coList.get(i).co = tagCmdrList.get(i);
          done = true;
        }
        break;
      case UP:
      case DOWN:
      case LEFT:
      case RIGHT:
      {
        tagIndexSelector.handleInput(action);
      }
      break;
      case BACK:
        // Cancel: return control without applying changes.
        done = true;
        break;
      case SEEK:
        // Kick out the selected CO
        if( selTagIndex+1 < tagCmdrList.size() )
        {
          tagCmdrList.remove(selTagIndex);
          syncTagIndexSelector();
        }
        break;
      case VIEWMODE:
        int selectedCO = tagCmdrList.get(selTagIndex);
        startViewingCmdrInfo(selectedCO);
        break;
      default:
        // Do nothing.
    }
    return done;
  }

  private boolean handleCmdrChoiceInput(InputAction action)
  {
    boolean done = false;
    final int selectedBin    = cmdrBinSelector.getSelectionNormalized();
    final int selectedColumn = cmdrInBinSelector.getSelectionNormalized();
    // Value of selection; index into the list of CO infos
    final int selectedCO     = cmdrBins.get(selectedBin).get(selectedColumn);
    switch(action)
    {
      case SELECT:
        // Are we bimodal?
        if( !shouldSelectMultiCO )
        {
          // No; apply change and return control.
          myPlayerInfo.coList.get(0).co = selectedCO;
          done = true;
        }
        else // Yes
        {
          amPickingTagIndex = true;

          final int selTagIndex = tagIndexSelector.getSelectionNormalized();
          // handleTagChoiceInput() should ensure this index is in [0, tag count)

          tagCmdrList.set(selTagIndex, selectedCO);

          // Add/remove if appropriate
          if( selTagIndex + 1 >= tagCmdrList.size() )
          {
            tagCmdrList.add(noCmdr); // Extend the list if we just added a new tag partner
            syncTagIndexSelector();
          }
        }
        break;
      case UP:
      case DOWN:
      {
        final int binPicked = cmdrBinSelector.handleInput(action);
        final int destBinSize = cmdrBins.get(binPicked).size();
        // Selection column clamps to the max for the new bin
        cmdrInBinSelector.reset(destBinSize);
        final int destColumn = Math.min(destBinSize - 1, rightGlueColumn);
        cmdrInBinSelector.setSelectedOption(destColumn);
      }
        break;
      case LEFT:
      case RIGHT:
      {
        rightGlueColumn = cmdrInBinSelector.handleInput(action);
      }
      break;
      case BACK:
        if( !shouldSelectMultiCO )
        {
          // Cancel: return control without applying changes.
          done = true;
        }
        else
        {
          amPickingTagIndex = true;
        }
        break;
      case VIEWMODE:
        startViewingCmdrInfo(selectedCO);
        break;
      default: // SEEK
        // Do nothing.
    }
    return done;
  }

  private void syncTagIndexSelector()
  {
    final int tagIndex = tagIndexSelector.getSelectionNormalized();
    tagIndexSelector.reset(tagCmdrList.size() + 1);
    tagIndexSelector.setSelectedOption(tagIndex);
  }

  private void startViewingCmdrInfo(int selectedCO)
  {
    CO_InfoController coInfoMenu = new CO_InfoController(cmdrInfos, selectedCO);
    IView infoView = Driver.getInstance().gameGraphics.createInfoView(coInfoMenu);

    // Give the new controller/view the floor
    Driver.getInstance().changeGameState(coInfoMenu, infoView);
  }
}
