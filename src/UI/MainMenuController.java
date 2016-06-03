package UI;

import java.awt.Color;

import Terrain.GameMap;
import UI.Art.SpriteArtist.SpriteMapView;
import UI.InputHandler.InputAction;
import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Engine.Driver;
import Engine.GameInstance;
import Engine.IController;
import Engine.MapController;
import Engine.OptionSelector;

public class MainMenuController implements IController
{
  // There are three options from the first game menu:
  final int NEW_GAME = 0;
  final int OPTIONS = 1;
  final int QUIT = 2;
  // This list of menu options is mirrored by the Sprite of option images we get from SpriteLibrary.
  final int[] menuOptions = {NEW_GAME, OPTIONS, QUIT};

  private OptionSelector optionSelector = null;

  public MainMenuController()
  {
    optionSelector = new OptionSelector(menuOptions.length);
  }

  public OptionSelector getOptionSelector()
  {
    return optionSelector;
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;

    switch( action )
    {
      case ENTER:
        // Grab the current option and roll it back to within the valid range, then evaluate.
        int chosenOption = optionSelector.getSelectionNormalized();

        switch( chosenOption )
        {
          case NEW_GAME:
          // TODO: Move all this stuff where it belongs, whenever that exists.
          Commander co1 = new CmdrStrong();
          Commander co2 = new Commander();
          Commander[] cos = { co1, co2 };

          cos[0].myColor = Color.pink;
          cos[1].myColor = Color.cyan;
          GameMap map = new GameMap(cos);
          GameInstance newGame = new GameInstance(map, cos);

          SpriteMapView smv = new SpriteMapView(newGame);
          MapController mapController = new MapController(newGame, smv);
          Driver.getInstance().changeGameState(mapController, smv);

          break;
          case OPTIONS:
            System.out.println("WARNING! Options menu not supported yet!");
            break;
          case QUIT:
            // Let the caller know we are done here.
            exitMenu = true;
            break;
            default:
              System.out.println("WARNING! Invalid menu option chosen.");
        }
        break;
      case DOWN:
      case UP:
        // Pass Up/Down along to the OptionSelector.
        optionSelector.handleInput(action);
        break;
        default:
          // Other actions (LEFT, RIGHT, BACK) not supported in the main menu.
    }

    return exitMenu;
  }

}
