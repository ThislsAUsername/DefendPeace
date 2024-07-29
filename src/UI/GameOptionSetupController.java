package UI;

import Engine.GameScenario.FogMode;
import Engine.GameScenario.TagMode;

import java.util.HashMap;
import java.util.Scanner;

import Engine.ConfigUtils;
import Engine.IController;
import Engine.OptionSelector;
import Terrain.Environment.Weathers;
import UI.InputHandler.InputAction;
import Units.UnitModelScheme;

public class GameOptionSetupController implements IController
{
  public final int GAME_OPTIONS_CONFIG_KEY = -1; // Player indices start at 0, so -1 should be safe.

  private GameOption<FogMode> fowOption = new GameOption<FogMode>("Fog of War", FogMode.values(), 0);
  private GameOption<Integer> startingFundsOption = new GameOptionInt("Starting Funds", 0, 50000, 1000, 0);
  private GameOption<Integer> incomeOption = new GameOptionInt("Income", 0, 20000, 250, 1000);
  private GameOption<Weathers> weatherOption = new GameOption<Weathers>("Weather", Weathers.values(), 0);
  private GameOption<UnitModelScheme> unitSchemeOption;
  private GameOption<TagMode> tagsOption = new GameOption<TagMode>("Tag Mode", TagMode.values(), 0);
  private GameOption<Boolean> securityOption = new GameOptionBool("Protect Turns?", false);

  // Get a list of all GameOptions.
  public GameOption<?>[] gameOptions;
  // The options last used on this map, if any
  HashMap<Integer, String> initialPicksMap;

  public OptionSelector optionSelector;
  private PlayerSetupController coSelectMenu;

  private boolean isInSubmenu = false;
  private boolean changesMade = false;

  // GameBuilder to set options.
  private GameBuilder gameBuilder = null;

  public GameOptionSetupController(GameBuilder aGameBuilder)
  {
    gameBuilder = aGameBuilder;

    unitSchemeOption = new GameOption<UnitModelScheme>("Unit set", gameBuilder.mapInfo.getValidUnitModelSchemes(), 0);

    // Default to the first valid unit scheme
    for (int i = 0; !unitSchemeOption.getSelectedObject().schemeValid && i < unitSchemeOption.size(); ++i )
      unitSchemeOption.setSelectedOption(i);

    gameOptions = new GameOption<?>[] {fowOption, startingFundsOption, incomeOption, weatherOption, unitSchemeOption, tagsOption, securityOption};
    optionSelector = new OptionSelector( gameOptions.length );

    // Read in the last settings we used on this map, if available
    initialPicksMap = new HashMap<Integer, String>();
    ConfigUtils.readConfigLists(PlayerSetupController.buildSettingsFileName(gameBuilder),
                                (String s)->Integer.valueOf(s),
                                (Scanner linescan)->linescan.nextLine(),
                                initialPicksMap);

    if( initialPicksMap.containsKey(GAME_OPTIONS_CONFIG_KEY) )
    {
      String fullConfig = initialPicksMap.get(GAME_OPTIONS_CONFIG_KEY);
      String[] s = fullConfig.split("\\s+"); // Split (string integers)
      int si = 0; // Split Index
      while (s[si].isEmpty()) ++si; // Skip any sneaky empty splits

      for( int i = 0; i < gameOptions.length; ++i )
        if( s.length > si )
          gameOptions[i].setSelectedOption(Integer.valueOf(s[si++]));
    }
    else
    {
      final int incomeIndexDefault = 3; // 250 = 0, increment 250
      final int incomeIndexHF = incomeIndexDefault + 4;
      if( gameBuilder.mapInfo.mapName.startsWith("HF") )
        incomeOption.setSelectedOption(incomeIndexHF);
      if( gameBuilder.mapInfo.mapName.startsWith("FoW") )
        fowOption.setSelectedOption(1);
    }
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;

    if(isInSubmenu)
    {
      exitMenu = coSelectMenu.handleInput(action);
      if(exitMenu)
      {
        isInSubmenu = false;

        // If BACK was chosen for the child menu, then we take control again (if
        // the child menu exited via entering a game, then action will be ENTER).
        if(action == InputAction.BACK)
        {
          // Don't pass control back up the chain.
          exitMenu = false;
        }
        else
        {
          optionSelector.setSelectedOption(0);
        }
      }
    }
    else
    {
      exitMenu = handleGameOptionInput(action);
    }
    return exitMenu;
  }

  private boolean handleGameOptionInput(InputAction action)
  {
    boolean exitMenu = false;
    switch(action)
    {
      case SELECT:
        // Shove our selections into the initialPicksMap so they'll be persisted
        StringBuilder fullConfig = new StringBuilder();
        for( int i = 0; i < gameOptions.length; ++i )
          fullConfig.append(" " + gameOptions[i].getSelectionNormalized());
        initialPicksMap.put(GAME_OPTIONS_CONFIG_KEY, fullConfig.toString());

        // Set the selected options and transition to the player setup screen.
        for( GameOption<?> go : gameOptions ) go.storeCurrentValue();
        gameBuilder.fogMode       = fowOption.getSelectedObject();
        gameBuilder.startingFunds = startingFundsOption.getSelectedObject();
        gameBuilder.incomePerCity = incomeOption.getSelectedObject();
        gameBuilder.defaultWeather = (Weathers)weatherOption.getSelectedObject();
        gameBuilder.unitModelScheme = unitSchemeOption.getSelectedObject();
        gameBuilder.tagMode = (TagMode)tagsOption.getSelectedObject();
        gameBuilder.isSecurityEnabled = securityOption.getSelectedObject();
        if( null == coSelectMenu )
          coSelectMenu = new PlayerSetupController(gameBuilder, initialPicksMap, changesMade);
        isInSubmenu = true;
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
        int opt = optionSelector.getSelectionNormalized();
        gameOptions[opt].handleInput(action);
        changesMade = true;
        break;
        default:
          System.out.println("Warning: Unsupported input " + action + " in map select menu.");
    }
    return exitMenu;
  }

  public boolean inSubmenu()
  {
    return isInSubmenu;
  }

  public PlayerSetupController getSubController()
  {
    return coSelectMenu;
  }
}
