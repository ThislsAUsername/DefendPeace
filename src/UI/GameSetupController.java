package UI;

import java.awt.Color;

import Terrain.GameMap;
import Terrain.MapLibrary;
import UI.Art.SpriteArtist.SpriteMapView;
import UI.InputHandler.InputAction;
import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Engine.Driver;
import Engine.GameInstance;
import Engine.IController;
import Engine.MapController;
import Engine.OptionSelector;

public class GameSetupController implements IController
{
  private OptionSelector optionSelector = new OptionSelector( MapLibrary.getMapList().size() );

  public int getSelectedOption()
  {
    return optionSelector.getSelectionNormalized();
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;
    switch(action)
    {
      case ENTER:
        // TODO: Move CO/color selection stuff to where it belongs, whenever that exists.
        Commander co1 = new CmdrStrong();
        Commander co2 = new Commander();
        Commander co3 = new Commander();
        Commander[] cos = { co1, co2 };

        cos[0].myColor = Color.pink;
        cos[1].myColor = Color.cyan;
        //cos[2].myColor = Color.orange;

        // Build the new map and create the game instance.
        GameMap map = new GameMap( cos, MapLibrary.getMapList().get( optionSelector.getSelectionNormalized() ) );
        GameInstance newGame = new GameInstance(map, cos);

        SpriteMapView smv = new SpriteMapView(newGame);
        MapController mapController = new MapController(newGame, smv);

        // Mash the big red button and start the game.
        Driver.getInstance().changeGameState(mapController, smv);
        exitMenu = true;
        break;
      case BACK:
        exitMenu = true;
        break;
      case DOWN:
      case UP:
        optionSelector.handleInput(action);
        break;
      case LEFT:
      case RIGHT:
      case NO_ACTION:
        default:
          System.out.println("Warning: Unsupported input " + action + " in map select menu.");
    }
    return exitMenu;
  }
}
