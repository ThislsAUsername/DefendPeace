package UI;

import Engine.IController;
import Engine.OptionSelector;
import Terrain.Environment.Weathers;
import UI.InputHandler.InputAction;
import Units.UnitModelScheme;

public class GameOptionSetupController implements IController
{
  private GameOption<Boolean> fowOption = new GameOptionBool("Fog of War", false);
  private GameOption<Integer> startingFundsOption = new GameOptionInt("Starting Funds", 0, 50000, 1000, 0);
  private GameOption<Integer> incomeOption = new GameOptionInt("Income", 250, 20000, 250, 1000);
  private GameOption<Weathers> weatherOption = new GameOption<Weathers>("Weather", Weathers.values(), 0);
  private GameOption<UnitModelScheme> unitSchemeOption;

  // Get a list of all GameOptions.
  public GameOption<?>[] gameOptions;

  public OptionSelector optionSelector;
  private PlayerSetupController coSelectMenu;

  private boolean isInSubmenu = false;

  // GameBuilder to set options.
  private GameBuilder gameBuilder = null;

  public GameOptionSetupController(GameBuilder aGameBuilder)
  {
    gameBuilder = aGameBuilder;

    unitSchemeOption = new GameOption<UnitModelScheme>("Unit set", gameBuilder.mapInfo.getValidUnitModelSchemes(), 0);

    // Default to the first valid unit scheme
    for (int i = 0; !unitSchemeOption.getSelectedObject().schemeValid && i < unitSchemeOption.size(); ++i )
      unitSchemeOption.setSelectedOption(i);

    gameOptions = new GameOption<?>[] {fowOption, startingFundsOption, incomeOption, weatherOption, unitSchemeOption};
    optionSelector = new OptionSelector( gameOptions.length );
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
        // Set the selected options and transition to the player setup screen.
        for( GameOption<?> go : gameOptions ) go.storeCurrentValue();
        gameBuilder.isFowEnabled = fowOption.getSelectedObject();
        gameBuilder.startingFunds = startingFundsOption.getSelectedObject();
        gameBuilder.incomePerCity = incomeOption.getSelectedObject();
        gameBuilder.defaultWeather = (Weathers)weatherOption.getSelectedObject();
        gameBuilder.unitModelScheme = unitSchemeOption.getSelectedObject();
        coSelectMenu = new PlayerSetupController( gameBuilder );
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
        break;
      case NO_ACTION:
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
