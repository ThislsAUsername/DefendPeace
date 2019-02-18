package UI;

import java.awt.Color;
import java.util.ArrayList;

import AI.AILibrary;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderLibrary;
import Engine.Driver;
import Engine.GameInstance;
import Engine.IController;
import Engine.MapController;
import Engine.OptionSelector;
import Engine.Utils;
import Terrain.MapMaster;
import UI.InputHandler.InputAction;
import UI.Art.SpriteArtist.SpriteLibrary;

/**
 * Controller for choosing COs and colors after the map has been chosen.
 * Left/Right changes the CO selector with focus.
 * Up/Down changes the CO selected for that slot.
 * LS/RS changes the color of the selected CO?
 */
public class COSetupController implements IController
{
  // This optionSelector determines which player's Commander we are choosing.
  private OptionSelector playerSelector;
  private GameBuilder gameBuilder = null;
  COSetupInfo[] coSelectors;

  public COSetupController( GameBuilder builder )
  {
    // Once we hit go, we plug all the COs we chose into gameBuilder.
    gameBuilder = builder;
    int numCos = gameBuilder.mapInfo.getNumCos();
    playerSelector = new OptionSelector(numCos);
    coSelectors = new COSetupInfo[numCos];

    // Start by making default CO/color selections.
    for(int co = 0; co < numCos; ++co)
    {
      // Set up our option selection framework
      coSelectors[co] = new COSetupInfo(numCos, co, CommanderLibrary.getCommanderList(), SpriteLibrary.getCOColors(), SpriteLibrary.getFactionNames(), AILibrary.getAIList());
    }
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;
    switch(action)
    {
      case ENTER:
        // We have locked in our selection. Stuff it into the GameBuilder and then kick off the game.
        for(int i = 0; i < coSelectors.length; ++i)
        {
          gameBuilder.addCO(coSelectors[i].makeCommander());
        }

        // Build the CO list and the new map and create the game instance.
        Commander[] cos = gameBuilder.commanders.toArray(new Commander[gameBuilder.commanders.size()]);
        MapMaster map = new MapMaster( cos, gameBuilder.mapInfo );
        if( map.initOK() )
        {
          GameInstance newGame = new GameInstance(map, cos);

          MapView mv = Driver.getInstance().gameGraphics.createMapView(newGame);
          MapController mapController = new MapController(newGame, mv);

          // Mash the big red button and start the game.
          Driver.getInstance().changeGameState(mapController, mv);
        }
        exitMenu = true;
        break;
      case BACK:
        exitMenu = true;
        break;
      case DOWN:
      case UP:
        coSelectors[playerSelector.getSelectionNormalized()].getCurrentOptionSelector().handleInput(action);
        break;
      case LEFT:
      case RIGHT:
        // If we're at the edge of one of our option sets, switch to a new CO
        if( coSelectors[playerSelector.getSelectionNormalized()].pickOption(action) )
        {
          playerSelector.handleInput(action);
          // Reset the selector to the beginning or end, for selection continuity
          if (UI.InputHandler.InputAction.LEFT == action)
            coSelectors[playerSelector.getSelectionNormalized()].setSelectedOption(COSetupInfo.OptionList.values().length-1);
          else
            coSelectors[playerSelector.getSelectionNormalized()].setSelectedOption(0);
        }
        break;
      case NO_ACTION:
        break;
        default:
          System.out.println("Warning: Unsupported input " + action + " in CO setup menu.");
    }
    return exitMenu;
  }

  public int getHighlightedPlayer()
  {
    return playerSelector.getSelectionNormalized();
  }
  
  public COSetupInfo getPlayerInfo(int p)
  {
    return coSelectors[p];
  }

  public CommanderInfo getPlayerCo(int p)
  {
    return coSelectors[p].getCurrentCO();
  }

  public Color getPlayerColor(int p)
  {
    return coSelectors[p].getCurrentColor();
  }
}
