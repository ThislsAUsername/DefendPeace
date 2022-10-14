package UI;

import java.util.ArrayList;

import CommandingOfficers.*;
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
  public final boolean shouldSelectMultiCO;
  private final int noCmdr;

  public ArrayList<ArrayList<Integer>> cmdrBins;
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

    tagCmdrList = new ArrayList<>();
    tagCmdrList.addAll(myPlayerInfo.coList);

    final int lastCO = tagCmdrList.get(tagCmdrList.size() - 1);

    cmdrBins = new ArrayList<>();

    int lastBin = -1;
    int startBin = 0;
    int startBinIndex = 0;

    int coIndex = 0;
    while (coIndex < infos.size())
    {
      ++lastBin;
      int binSize = coIndex + 2; // TODO
      ArrayList<Integer> bin = new ArrayList<>();
      bin.add(noCmdr); // Put No CO at the start of each bin?
      for( int binIndex = 1;
          binIndex < binSize && coIndex < infos.size();
          ++binIndex, ++coIndex )
      {
        if( noCmdr == coIndex )
          continue; // Don't throw in extras

        bin.add(coIndex);
        if( lastCO == coIndex )
        {
          startBin = lastBin;
          startBinIndex = binIndex;
        }
      }
      cmdrBins.add(bin);
    }

    cmdrBinSelector = new OptionSelector(lastBin + 1);
    cmdrBinSelector.setSelectedOption(startBin);
    cmdrInBinSelector = new OptionSelector(cmdrBins.get(startBin).size());
    cmdrInBinSelector.setSelectedOption(startBinIndex);
    rightGlueColumn = startBinIndex;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    final int selectedBin    = cmdrBinSelector.getSelectionNormalized();
    final int selectedColumn = cmdrInBinSelector.getSelectionNormalized();
    final int selectedCO     = cmdrBins.get(selectedBin).get(selectedColumn);
    switch(action)
    {
      case SELECT:
        rightGlueColumn = 0;

        // Check for CO addition
        if( !shouldSelectMultiCO )
        {
          // Apply change and return control.
          tagCmdrList.clear();
          tagCmdrList.add(selectedCO);
          myPlayerInfo.coList = tagCmdrList;
          done = true;
        }
        else
        {
          if( tagCmdrList.size() == 0 || noCmdr != selectedCO )
            tagCmdrList.add(selectedCO);

          // Drop back to No CO so a double-tap finishes selection
          cmdrInBinSelector.setSelectedOption(0);

          if( noCmdr == selectedCO )
          {
            done = true;
            myPlayerInfo.coList = tagCmdrList;
          }
        }
        break;
      case UP:
      case DOWN:
      {
        final int binPicked = cmdrBinSelector.handleInput(action);
        final int destBinSize = cmdrBins.get(binPicked).size();
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
        // Check for CO deletion
        if( !shouldSelectMultiCO )
        {
          // Cancel: return control without applying changes.
          done = true;
        }
        else
        {
          if( tagCmdrList.size() == 0 )
            done = true;
          else
          {
            // Consider moving selection to the new last CO?
            tagCmdrList.remove(tagCmdrList.size() - 1);
          }
        }
        break;
      case SEEK:
        CO_InfoController coInfoMenu = new CO_InfoController(cmdrInfos, selectedCO);
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
