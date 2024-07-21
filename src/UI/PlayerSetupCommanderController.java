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
import lombok.var;

public class PlayerSetupCommanderController implements IController
{
  private PlayerSetupInfo myPlayerInfo;
  public final ArrayList<CommanderInfo> cmdrInfos;
  public final boolean shouldSelectMultiCO;
  public final int noCmdr;
  // Lists, organized by x then y, of indices into the canonical commander list
  public final ArrayList<Integer>[][] cosByGameFaction;
  public final ArrayList<Integer>[][] cosByFactionGame;

  // It whines about the CO list list list creation
  @SuppressWarnings("unchecked")
  public PlayerSetupCommanderController(ArrayList<CommanderInfo> infos, PlayerSetupInfo playerInfo, TagMode tagMode)
  {
    cmdrInfos = infos;
    noCmdr = infos.size() - 1;
    myPlayerInfo = playerInfo;
    shouldSelectMultiCO = tagMode.supportsMultiCmdrSelect;

    var canonFactions = UIUtils.CANON_FACTIONS;

    int gameCount = UIUtils.SourceGames.values().length;
    int factionCount = canonFactions.length;
    cosByGameFaction = new ArrayList[gameCount][factionCount];
    cosByFactionGame = new ArrayList[factionCount][gameCount];

    for(   int game = 0   ;    game < gameCount   ; ++game )
      for( int faction = 0; faction < factionCount; ++faction )
        cosByGameFaction[game][faction] = new ArrayList<>();
    for(   int faction = 0; faction < factionCount; ++faction )
      for( int game = 0   ;    game < gameCount   ; ++game )
        cosByFactionGame[faction][game] = new ArrayList<>();

    for( int co = 0; co < cmdrInfos.size(); ++co )
    {
      CommanderInfo info = cmdrInfos.get(co);
      int game = info.game.ordinal();
      int faction = 0;
      for( ; faction < factionCount; ++faction )
        if( info.baseFaction == canonFactions[faction] )
          break;

      cosByGameFaction[game][faction].add(co);
      cosByFactionGame[faction][game].add(co);
    }
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
