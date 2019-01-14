package UI;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderLibrary;
import Engine.Driver;
import Engine.GameInstance;
import Engine.IController;
import Engine.MapController;
import Engine.OptionSelector;
import Engine.Utils;
import Terrain.GameMap;
import UI.InputHandler.InputAction;
import UI.Art.SpriteArtist.SpriteLibrary;
import UI.Art.SpriteArtist.SpriteLibrary.UnitSpriteSetKey;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;

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
  OptionSelector[] coSelectors;
  OptionSelector[] colorSelectors;
  OptionSelector[] spriteSelectors;
  public static String[] spriteSetKeys;

  public COSetupController( GameBuilder builder )
  {
    // Once we hit go, we plug all the COs we chose into gameBuilder.
    gameBuilder = builder;
    int numCos = gameBuilder.mapInfo.getNumCos();
    playerSelector = new OptionSelector(numCos);
    // One entry of two numbers for each player, for Commander and color.
    coSelectors = new OptionSelector[numCos];
    colorSelectors = new OptionSelector[numCos];
    spriteSelectors = new OptionSelector[numCos];
    
    spriteSetKeys = Utils.fetchFactionNames();

    // Start by making default CO/color selections.
    for(int co = 0; co < numCos; ++co)
    {
      // Set up our option selection framework for CO and color choices.
      coSelectors[co] = new OptionSelector(CommanderLibrary.getCommanderList().size());
      colorSelectors[co] = new OptionSelector(SpriteLibrary.coColorList.length);
      spriteSelectors[co] = new OptionSelector(spriteSetKeys.length);

      // Defaulting to the first available CO, and assigning colors in sequence.
      // TODO: Consider changing this to sequential or random COs once we have enough.
      coSelectors[co].setSelectedOption(0); // Commander choice
      colorSelectors[co].setSelectedOption(co); // Color choice
      spriteSelectors[co].setSelectedOption(0); // sprite set choice
    }
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;
    switch(action)
    {
      case ENTER:
        ArrayList<CommanderInfo> coList = CommanderLibrary.getCommanderList();
        // We have locked in our selection. Stuff it into the GameBuilder and then kick off the game.
        for(int i = 0; i < coSelectors.length; ++i)
        {
          Commander co = CommanderLibrary.makeCommander(coList.get(coSelectors[i].getSelectionNormalized()),
              SpriteLibrary.coColorList[colorSelectors[i].getSelectionNormalized()], spriteSetKeys[spriteSelectors[i].getSelectionNormalized()]);
          gameBuilder.addCO(co);
          for (UnitEnum ue : UnitModel.UnitEnum.values()) {
          UnitSpriteSetKey key = UnitSpriteSetKey.instance(ue, co);
          SpriteLibrary.createMapUnitSpriteSet(key);
          }
        }

        // Build the CO list and the new map and create the game instance.
        Commander[] cos = gameBuilder.commanders.toArray(new Commander[gameBuilder.commanders.size()]);
        GameMap map = new GameMap( cos, gameBuilder.mapInfo );
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
        //coSelectors.get(playerSelector.getSelectionNormalized()).handleInput(action);
        coSelectors[playerSelector.getSelectionNormalized()].handleInput(action);
        break;
      case LEFT:
      case RIGHT:
        playerSelector.handleInput(action);
        break;
      case SEEK:
        spriteSelectors[playerSelector.getSelectionNormalized()].handleInput(InputAction.RIGHT);
        break;
      case NO_ACTION:
        default:
          System.out.println("Warning: Unsupported input " + action + " in CO setup menu.");
    }
    return exitMenu;
  }

  public int getHighlightedPlayer()
  {
    return playerSelector.getSelectionNormalized();
  }

  public int getPlayerCo(int p)
  {
    return coSelectors[p].getSelectionNormalized();
  }

  public int getPlayerColor(int p)
  {
    return colorSelectors[p].getSelectionNormalized();
  }

  public int getPlayerFaction(int p)
  {
    return spriteSelectors[p].getSelectionNormalized();
  }
}
