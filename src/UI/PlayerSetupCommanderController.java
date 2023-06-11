package UI;

import java.util.ArrayList;
import CommandingOfficers.*;
import Engine.Driver;
import Engine.IController;
import Engine.IView;
import Engine.GameScenario.TagMode;
import UI.InputHandler.InputAction;
import UI.PlayerSetupInfo.CODeets;
import UI.Art.SpriteArtist.PlayerSetupCommanderArtist;

public class PlayerSetupCommanderController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  public final ArrayList<CommanderInfo> cmdrInfos;
  public final boolean shouldSelectMultiCO;
  public final int noCmdr;

  public PlayerSetupCommanderController(ArrayList<CommanderInfo> infos, PlayerSetupInfo playerInfo, TagMode tagMode)
  {
    cmdrInfos = infos;
    noCmdr = infos.size() - 1;
    myPlayerInfo = playerInfo;
    shouldSelectMultiCO = tagMode.supportsMultiCmdrSelect;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    // This is terrible, bad, and wrong...
    //   but I wanted to get this refactor in without muddling the two halves.
    // I think routing control through the UI is where we're going in the future, and this will be resolved naturally if that happens.
    return PlayerSetupCommanderArtist.handleInput(action);
  }

  public ArrayList<Integer> getInitialCmdrs()
  {
    ArrayList<Integer> tagCmdrList = new ArrayList<>();

    for( CODeets deets : myPlayerInfo.coList )
      tagCmdrList.add(deets.co);

    return tagCmdrList;
  }

  public void applyCmdrChoices(ArrayList<Integer> tagCmdrList)
  {
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
  }

  public void startViewingCmdrInfo(int selectedCO)
  {
    CO_InfoController coInfoMenu = new CO_InfoController(cmdrInfos, selectedCO);
    IView infoView = Driver.getInstance().gameGraphics.createInfoView(coInfoMenu);

    // Give the new controller/view the floor
    Driver.getInstance().changeGameState(coInfoMenu, infoView);
  }
}
