package UI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Engine.Driver;
import Engine.GameInstance;
import Engine.IController;
import Engine.MapController;
import Engine.OptionSelector;
import Engine.SerializationUtils;

public class MainUIController implements IController
{
  public enum SubMenu { MAIN, SAVE_SELECT, GAME_SETUP, OPTIONS_SELECT, GRAPHICS_OPTIONS, SET_KEYBINDS, CONTROL_OPTIONS };
  private static List<SubMenu> optionsSubMenus = Arrays.asList(SubMenu.GRAPHICS_OPTIONS, SubMenu.SET_KEYBINDS, SubMenu.CONTROL_OPTIONS);
  private SubMenu currentSubMenuType = SubMenu.MAIN;

  // NOTE: This list of menu options is mirrored by the Sprite of option images we get from SpriteLibrary.
  final int NEW_GAME = 0;
  final int CONTINUE = 1;
  final int OPTIONS = 2;
  final int QUIT = 3;
  final int numMenuOptions = 4;

  private OptionSelector optionSelector = new OptionSelector(numMenuOptions);

  private MapSelectController gameSetup = new MapSelectController();
  private InputHandler controlOptionsSetup = InputHandler.getInstance();
  private InputOptionsController controlExtraOptionsSetup = new InputOptionsController();
  public InGameMenu<SaveInfo> saveMenu = null;
  public InGameMenu<SubMenu> optionsMenu = null;
  
  public SubMenu getSubMenuType()
  {
    return currentSubMenuType;
  }

  public OptionSelector getOptionSelector()
  {
    return optionSelector;
  }
  
  public MapSelectController getGameSetupController()
  {
    return gameSetup;
  }

  public InputHandler getControlOptionsSetupController()
  {
    return controlOptionsSetup;
  }

  @Override // From IController
  public boolean handleInput(InputHandler.InputAction action)
  {
    boolean exitGame = false;

    switch( currentSubMenuType )
    {
      case GAME_SETUP:
        // Pass the input action along to the active sub-handler.
        exitGame = gameSetup.handleInput(action);
        if(exitGame)
        {
          // If the subMenu was not MAIN, we go back to MAIN.
          currentSubMenuType = SubMenu.MAIN;
          exitGame = false;
        }
        break;
      case MAIN:
        exitGame = handleMainMenuInput(action);
        break;
      case SAVE_SELECT:
        handleSaveSelectMenuInput(action);
        break;
      case OPTIONS_SELECT:
        handleOptionMenuSelectInput(action);
        break;
      case GRAPHICS_OPTIONS:
        // Since different graphics engines could implement different available
        //   options, we cannot handle this input directly. Instead, just feed
        //   the user inputs to the graphics engine directly.
        exitGame = Driver.getInstance().gameGraphics.handleOptionsInput(action);

        if(exitGame)
        {
          // If the subMenu was not MAIN, we go back to MAIN.
          currentSubMenuType = SubMenu.MAIN;
          exitGame = false;
        }
        break;
      case SET_KEYBINDS:
        if( controlOptionsSetup.handleInput(action) )
          currentSubMenuType = SubMenu.MAIN;
        break;
      case CONTROL_OPTIONS:
        if( controlExtraOptionsSetup.handleInput(action) )
          currentSubMenuType = SubMenu.MAIN;
        break;
      default:
        System.out.println("Warning: Invalid input " + action + " in MainUIController.");
    }

    return exitGame;
  }

  private boolean handleMainMenuInput(InputHandler.InputAction action)
  {
    boolean exitMenu = false;

    switch( action )
    {
      case SELECT:
        // Grab the current option and roll it back to within the valid range, then evaluate.
        int chosenOption = optionSelector.getSelectionNormalized();

        switch( chosenOption )
        {
          case NEW_GAME:
            currentSubMenuType = SubMenu.GAME_SETUP;
          break;
          case CONTINUE:
            ArrayList<SaveInfo> saves = new ArrayList<SaveInfo>();
            final File folder = new File(Engine.Driver.JAR_DIR + "save/");
            if( folder.canRead() )
            {
              for( final File fileEntry : folder.listFiles() )
              {
                String filepath = fileEntry.getAbsolutePath();
                // Look for files with our extension
                if( !fileEntry.isDirectory() && filepath.endsWith(".svp") )
                {
                  String filename = fileEntry.getName();
                  String prettyName = filename.substring(0, filename.length()-4);

                  // Get any warning symbols for save-file incompatibilities.
                  String prepends = SerializationUtils.getSaveWarnings(filepath);
                  saves.add(new SaveInfo(filepath, filename, prepends + prettyName));
                }
              }
            }

            if( !saves.isEmpty() )
            {
              saveMenu = new InGameMenu<SaveInfo>(saves);
              currentSubMenuType = SubMenu.SAVE_SELECT;
            }
            else
            {
              System.out.println("WARNING: There are no valid saves to load.");
            }
            break;
          case OPTIONS:
            optionsMenu = new InGameMenu<SubMenu>(optionsSubMenus);
            currentSubMenuType = SubMenu.OPTIONS_SELECT;
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
      case LEFT:
      case RIGHT:
        // Pass along to the OptionSelector.
        optionSelector.handleInput(action);
        break;
      case BACK: // There's no "back" from here. Select Quit to exit.
        default:
          // Other actions (LEFT, RIGHT, BACK) not supported in the main menu.
    }

    return exitMenu;
  }

  private void handleOptionMenuSelectInput(InputHandler.InputAction action)
  {
    switch( action )
    {
      case SELECT:
        // Grab the selected option.
        SubMenu chosenOption = optionsMenu.getSelectedOption().item;
        optionsMenu = null; // We are done with this until next time.

        if( optionsSubMenus.contains(chosenOption) )
          currentSubMenuType = chosenOption;
        else
          System.out.println(String.format("Invalid Option SubMenu '%s' selected!", chosenOption.toString()));

        break;
      case BACK:
        optionsMenu = null;
        currentSubMenuType = SubMenu.MAIN;
        break;
      default:
          // Pass along to the OptionSelector.
          optionsMenu.handleMenuInput(action);
    }
  }

  private void handleSaveSelectMenuInput(InputHandler.InputAction action)
  {
    switch( action )
    {
      case SELECT:
        // Grab the current option and roll it back to within the valid range, then evaluate.
        SaveInfo chosenOption = saveMenu.getSelectedOption().item;

        // We've already successfully read the save file, so let's assume the user isn't messing with us
        GameInstance oldGame = SerializationUtils.loadSave(chosenOption.filePath);
        if( null != oldGame )
        {
          oldGame.saveFile = chosenOption.saveName; // Keep whatever name the user set
          // We don't need our save selection menu anymore...
          saveMenu = null;
          currentSubMenuType = SubMenu.MAIN;

          // Set up the game to run...
          MapView mv = Driver.getInstance().gameGraphics.createMapView(oldGame);
          MapController mapController = new MapController(oldGame, mv);

          // Mash the big red button and start the game.
          Driver.getInstance().changeGameState(mapController, mv);
        }
        else
        {
          System.out.println(String.format("WARNING: Hey man, messing with %s while I'm tryna use it ain't cool", chosenOption.filePath));
        }
        break;
      case BACK: // throw away the save list and go back
        saveMenu = null;
        currentSubMenuType = SubMenu.MAIN;
        break;
      default:
          // Pass along to the OptionSelector.
          saveMenu.handleMenuInput(action);
    }
  }
  
  public static class SaveInfo
  {
    public final String filePath, saveName, displayName;
    public SaveInfo(String pFilepath, String pSaveName, String pDisplayName)
    {
      filePath = pFilepath;
      saveName = pSaveName;
      displayName = pDisplayName;
    }
    
    @Override
    public String toString()
    {
      return displayName;
    }
  }
}
