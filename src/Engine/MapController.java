package Engine;

import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.Environment;
import Terrain.Environment.Terrains;
import Terrain.GameMap;
import Terrain.Location;
import UI.CO_InfoMenu;
import UI.InGameMenu;
import UI.InGameProductionMenu;
import UI.InputHandler;
import UI.MapView;
import Units.Unit;
import Units.UnitModel;

public class MapController implements IController
{
  private GameInstance myGame;
  private MapView myView;

  private GameAction currentAction = null;

  // A few menus to control the in-game logical flow.
  private InGameMenu<UnitModel> productionMenu;
  private InGameMenu<GameAction.ActionType> actionMenu;
  private InGameMenu<MetaAction> metaActionMenu;
  private InGameMenu<String> coAbilityMenu;
  private InGameMenu<ConfirmExitEnum> confirmExitMenu;
  private InGameMenu<? extends Object> currentMenu;

  private int nextSelectedUnitIndex;

  private enum InputMode
  {
    MAP, MOVEMENT, ACTIONMENU, ACTION, PRODUCTION, METAACTION, CO_ABILITYMENU, CONFIRMEXIT, ANIMATION, EXITGAME, CO_INFO
  };

  public enum MetaAction
  {
    CO_INFO, CO_ABILITY, QUIT_GAME, END_TURN
  };

  private MetaAction[] metaActionsAbility = { MetaAction.CO_INFO, MetaAction.CO_ABILITY, MetaAction.QUIT_GAME,
      MetaAction.END_TURN };
  private MetaAction[] metaActionsNoAbility = { MetaAction.CO_INFO, MetaAction.QUIT_GAME, MetaAction.END_TURN };

  private enum ConfirmExitEnum
  {
    EXIT_TO_MAIN_MENU, QUIT_APPLICATION
  };

  private ConfirmExitEnum[] confirmExitOptions = { ConfirmExitEnum.EXIT_TO_MAIN_MENU, ConfirmExitEnum.QUIT_APPLICATION };

  private InputMode inputMode;

  private boolean isGameOver;

  private Path currentMovePath;

  // We use a different method for the CO Info menu than the others (MetaAction, etc) because
  // it has two different axes of control, and because it has no actions that can result.
  public boolean isInCoInfoMenu = false;
  private CO_InfoMenu coInfoMenu;

  public MapController(GameInstance game, MapView view)
  {
    myGame = game;
    myView = view;
    myView.setController(this);
    productionMenu = new InGameProductionMenu(myGame.commanders[0].getShoppingList(Terrains.FACTORY)); // Just init with a valid default.
    GameAction.ActionType[] defaultAction = { GameAction.ActionType.WAIT }; // Again, just a valid default. Will be replaced when needed.
    actionMenu = new InGameMenu<GameAction.ActionType>(defaultAction);
    metaActionMenu = new InGameMenu<MetaAction>(metaActionsNoAbility);
    coAbilityMenu = null;
    confirmExitMenu = new InGameMenu<ConfirmExitEnum>(confirmExitOptions);
    inputMode = InputMode.MAP;
    isGameOver = false;
    myGame.setCursorLocation(6, 5);
    coInfoMenu = new CO_InfoMenu(myGame.commanders.length);
    nextSelectedUnitIndex = 0;
  }

  /**
   * When the GameMap is in focus, all user input is directed through this function. It is
   * redirected to a specific handler based on what actions the user is currently taking.
   */
  @Override
  public boolean handleInput(InputHandler.InputAction input)
  {
    boolean exitMap = false;
    switch (inputMode)
    {
      case MAP:
        handleMapInput(input);
        break;
      case MOVEMENT:
        handleMovementInput(input);
        break;
      case ACTIONMENU:
        handleActionMenuInput(input);
        break;
      case ACTION:
        handleActionInput(input);
        break;
      case PRODUCTION:
        handleProductionMenuInput(input);
        break;
      case METAACTION:
        handleMetaActionMenuInput(input);
        break;
      case CO_ABILITYMENU:
        handleCoAbilityMenuInput(input);
        break;
      case CONFIRMEXIT:
        // If they exit via menu, don't hang around for the victory animation.
        exitMap = handleConfirmExitMenuInput(input);
        break;
      case CO_INFO:
        if( coInfoMenu.handleInput(input) )
        {
          isInCoInfoMenu = false;
          changeInputMode(InputMode.METAACTION);
        }
        break;
      case ANIMATION:
        if( input == InputHandler.InputAction.BACK || input == InputHandler.InputAction.ENTER )
        {
          // Tell the animation to cancel.
          myView.cancelAnimation();
        }
        break;
      case EXITGAME:
        // Once the game is over, wait for an ENTER or BACK input to return to the main menu.
        if( input == InputHandler.InputAction.BACK || input == InputHandler.InputAction.ENTER )
        {
          exitMap = true;
        }
        break;
      default:
        System.out.println("Invalid InputMode in MapController! " + inputMode);
    }

    return exitMap;
  }

  /**
   * When nothing is selected, user inputs go here. This is where the user can pan around the map,
   * or select a unit or a building to take some action.
   */
  private void handleMapInput(InputHandler.InputAction input)
  {
    switch (input)
    {
      case UP:
        myGame.moveCursorUp();
        break;
      case DOWN:
        myGame.moveCursorDown();
        break;
      case LEFT:
        myGame.moveCursorLeft();
        break;
      case RIGHT:
        myGame.moveCursorRight();
        break;
      case SEEK: // Move the cursor to the next unit that is ready to move.
        boolean found = false;
        int tries = 0;
        int maxTries = myGame.activeCO.units.size();
        while ((maxTries > 0) && !found && (tries < maxTries))
        {
          // Normalize the index to allow wrapping.
          if( nextSelectedUnitIndex >= maxTries )
          {
            nextSelectedUnitIndex = 0;
          }

          // If we find a unit that is ready to go, move the cursor to it.
          Unit nextUnit = myGame.activeCO.units.get(nextSelectedUnitIndex);
          if( !nextUnit.isTurnOver )
          {
            myGame.setCursorLocation(nextUnit.x, nextUnit.y);
            found = true;
          }
          // Increment for the next loop cycle or SEEK input.
          ++tries;
          ++nextSelectedUnitIndex;
        }
        break;
      case ENTER:
        // See what is at the current cursor location, in precedence order of Unit, Building, Terrain.
        Location loc = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY());
        Unit unitActor = loc.getResident();
        if( null != unitActor )
        {
          if( unitActor.isTurnOver == false || unitActor.CO != myGame.activeCO )
          {
            currentAction = new GameAction(unitActor); // Start building a GameAction

            // Calculate movement options.
            changeInputMode(InputMode.MOVEMENT);
          }
          else
          {
            changeInputMode(InputMode.METAACTION);
          }
        }
        else if( (Environment.Terrains.FACTORY == loc.getEnvironment().terrainType
            || Environment.Terrains.AIRPORT == loc.getEnvironment().terrainType
            || Environment.Terrains.SEAPORT == loc.getEnvironment().terrainType) && loc.getOwner() == myGame.activeCO )
        {
          changeInputMode(InputMode.PRODUCTION);
        }
        else
        {
          // Display end turn (save/quit?) options.
          changeInputMode(InputMode.METAACTION);
        }
        break;
      case BACK:
        break;
      default:
        System.out.println("WARNING! MapController.handleMapInput() was given invalid input enum (" + input + ")");
    }
  }

  /** Returns the currently-active in-game menu, or null if no menu is in use. */
  public InGameMenu<? extends Object> getCurrentGameMenu()
  {
    return currentMenu;
  }

  /**
   * When a unit is selected, user input flows through here to choose where the unit should move.
   */
  private void handleMovementInput(InputHandler.InputAction input)
  {
    boolean inMoveableSpace = myGame.getCursorLocation().isHighlightSet();

    switch (input)
    {
      case UP:
        myGame.moveCursorUp();
        // System.out.println("inMoveableSpace = " + inMoveableSpace);
        // Make sure we don't overshoot the reachable tiles by accident.
        if( inMoveableSpace && InputHandler.isUpHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorDown();
        }
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        break;
      case DOWN:
        myGame.moveCursorDown();
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isDownHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorUp();
        }
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        break;
      case LEFT:
        myGame.moveCursorLeft();
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isLeftHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorRight();
        }
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        break;
      case RIGHT:
        myGame.moveCursorRight();
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isRightHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorLeft();
        }
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        break;
      case ENTER:
        if( inMoveableSpace && currentAction.getActor().CO == myGame.activeCO ) // If the selected space is within
        // the reachable area
        {
          // Move the Unit to the location and display possible actions.
          currentMovePath.start(); // start the unit running
          currentAction.setMovePath(currentMovePath);
          currentMovePath = null;
          changeInputMode(InputMode.ACTIONMENU);
        }
        // if we're selecting an enemy unit, hitting enter again will drop that selection
        if( currentAction.getActor().CO != myGame.activeCO )
        {
          // TODO: re-selecting the unit should do a threat range check?
          changeInputMode(InputMode.MAP);
        }
        break;
      case BACK:
        changeInputMode(InputMode.MAP);
        break;
      case NO_ACTION:
        break;
      default:
        System.out.println("WARNING! MapController.handleMovementInput() was given invalid input enum (" + input + ")");
    }
  }

  /**
   * Once a unit's movement has been chosen, user input goes here to select an action to perform.
   */
  private void handleActionMenuInput(InputHandler.InputAction input)
  {
    if( currentMenu != actionMenu )
    {
      System.out.println("ERROR! MapController.handleActionMenuInput() called with wrong menu active!");
      return;
    }

    switch (input)
    {
      case ENTER:
        currentAction.setActionType(actionMenu.getSelectedOption());

        // If the action is completely constructed, execute it, else get the missing info.
        if( currentAction.isReadyToExecute() )
        {
          executeGameAction(currentAction);
        }
        else
        {
          changeInputMode(InputMode.ACTION);
        }

        break;
      case BACK:
        changeInputMode(InputMode.MOVEMENT);
        break;
      case NO_ACTION:
        break;
      default:
        currentMenu.handleMenuInput(input);
    }
  }

  /**
   * Once an action has been chosen, we need to choose the action target, and then execute.
   */
  private void handleActionInput(InputHandler.InputAction input)
  {
    boolean inActionableSpace = myGame.getCursorLocation().isHighlightSet();

    switch (input)
    {
      case UP:
        myGame.moveCursorUp();
        break;
      case DOWN:
        myGame.moveCursorDown();
        break;
      case LEFT:
        myGame.moveCursorLeft();
        break;
      case RIGHT:
        myGame.moveCursorRight();
        break;
      case ENTER:
        if( inActionableSpace && (null != currentAction) )
        {
          currentAction.setActionLocation(myGame.getCursorX(), myGame.getCursorY());

          if( currentAction.isReadyToExecute() )
          {
            executeGameAction(currentAction);
          }
          else
          {
            System.out.println("WARNING! Action not constructed correctly!");
            changeInputMode(InputMode.MAP); // try and reset;
          }
        }
        break;
      case BACK:
        changeInputMode(InputMode.ACTIONMENU);
        break;
      case NO_ACTION:
      default:
        System.out.println("WARNING! MapController.handleActionInput() was given invalid input enum (" + input + ")");
    }
  }

  /** We just selected a production building - what can it do? */
  private void handleProductionMenuInput(InputHandler.InputAction input)
  {
    if( currentMenu != productionMenu )
    {
      System.out.println("ERROR! MapController.handleProductionMenuInput() called with wrong menu active!");
      return;
    }

    switch (input)
    {
      case ENTER:
        Units.UnitModel model = productionMenu.getSelectedOption();

        if( model.moneyCost <= myGame.activeCO.money )
        {
          myGame.activeCO.money -= model.moneyCost;
          Unit u = new Unit(myGame.activeCO, model);
          myGame.activeCO.units.add(u);
          myGame.gameMap.addNewUnit(u, myGame.getCursorX(), myGame.getCursorY());
        }
        else
        {
          System.out.println("Not enough money.");
        }
        changeInputMode(InputMode.MAP);
        break;
      case BACK:
        changeInputMode(InputMode.MAP);
        break;
      case NO_ACTION:
        break;
      default:
        currentMenu.handleMenuInput(input);
    }
  }

  private void handleMetaActionMenuInput(InputHandler.InputAction input)
  {
    if( currentMenu != metaActionMenu )
    {
      System.out.println("ERROR! MapController.handleMetaActionMenuInput() called with wrong menu active!");
      return;
    }

    switch (input)
    {
      case ENTER:
        MetaAction action = metaActionMenu.getSelectedOption();

        // Nested switch statement woo!
        switch (action)
        {
          case CO_INFO:
            isInCoInfoMenu = true;
            changeInputMode(InputMode.CO_INFO);
            break;
          case CO_ABILITY:
            changeInputMode(InputMode.CO_ABILITYMENU);
            break;
          case END_TURN:
            nextSelectedUnitIndex = 0;
            myGame.turn();
            changeInputMode(InputMode.MAP);
            break;
          case QUIT_GAME:
            changeInputMode(InputMode.CONFIRMEXIT);
            break;
          default:
            System.out.println("WARNING! Unknown MetaActionEnum " + action);
        }

        break;
      case BACK:
        changeInputMode(InputMode.MAP);
        break;
      case NO_ACTION:
        break;
      default:
        currentMenu.handleMenuInput(input);
    }
  }

  private void handleCoAbilityMenuInput(InputHandler.InputAction input)
  {
    if( currentMenu != coAbilityMenu )
    {
      System.out.println("ERROR! MapController.handleCoAbilityMenuInput() called with wrong menu active!");
      return;
    }

    switch (input)
    {
      case ENTER:
        // Get the chosen ability and let it do its thing.
        String abilityName = coAbilityMenu.getSelectedOption();
        myGame.activeCO.doAbility(abilityName, myGame);

        changeInputMode(InputMode.MAP);

        //TODO: Figure out animating powers
        //changeInputMode(InputMode.ANIMATION);
        //myView.animate(???); // Set up the animation for this action.

        break;
      case BACK:
        changeInputMode(InputMode.METAACTION);
        break;
      case NO_ACTION:
        break;
      default:
        currentMenu.handleMenuInput(input);
    }
  }

  private boolean handleConfirmExitMenuInput(InputHandler.InputAction input)
  {
    boolean quitGame = false;
    if( currentMenu != confirmExitMenu )
    {
      System.out.println("ERROR! MapController.handleMetaActionMenuInput() called with wrong menu active!");
      return false;
    }

    switch (input)
    {
      case ENTER:
        ConfirmExitEnum action = confirmExitMenu.getSelectedOption();

        if( action == ConfirmExitEnum.EXIT_TO_MAIN_MENU )
        {
          // Go back to the main menu.
          quitGame = true;
        }
        else if( action == ConfirmExitEnum.QUIT_APPLICATION )
        {
          // Exit the application entirely.
          System.exit(0);
        }
        break;
      case BACK:
        changeInputMode(InputMode.METAACTION);
        break;
      case NO_ACTION:
        break;
      default:
        currentMenu.handleMenuInput(input);
    }
    return quitGame;
  }

  /**
   * Updates the InputMode and the current menu to keep them in sync.
   */
  private void changeInputMode(InputMode input)
  {
    inputMode = input;
    switch (inputMode)
    {
      case ACTION:
        Utils.findActionableLocations(currentAction.getActor(), currentAction.getActionType(), currentAction.getMoveX(),
            currentAction.getMoveY(), myGame.gameMap);
        boolean set = false;
        for( int w = 0; w < myGame.gameMap.mapWidth; ++w )
        {
          for( int h = 0; h < myGame.gameMap.mapHeight; ++h )
          {
            if( myGame.gameMap.getLocation(w, h).isHighlightSet() )
            {
              myGame.setCursorLocation(w, h);
              set = true;
              break;
            }
          }
          if( set )
            break;
        }
        currentMenu = null;
        break;
      case ACTIONMENU:
        myGame.gameMap.clearAllHighlights();
        actionMenu.resetOptions(
            currentAction.getActor().getPossibleActions(myGame.gameMap, currentAction.getMoveX(), currentAction.getMoveY()));
        currentMenu = actionMenu;
        myGame.setCursorLocation(currentAction.getMoveX(), currentAction.getMoveY());
        currentAction.setActionType(GameAction.ActionType.INVALID); // We haven't chosen an action yet.
        break;
      case MAP:
        currentAction = null;
        currentMovePath = null;
        currentMenu = null;
        myGame.gameMap.clearAllHighlights();

        //        if( unitActor != null )
        //        {
        //          myGame.setCursorLocation(unitActor.x, unitActor.y);
        //          unitActor = null; // We are now in MAP mode; no unit is selected.
        //        }
        break;
      case MOVEMENT:
        Utils.findPossibleDestinations(currentAction.getActor(), myGame);
        if( null != currentAction )
        {
          currentAction.setMovePath(null); // No destination chosen yet.
        }
        currentMenu = null;
        myGame.setCursorLocation(currentAction.getActor().x, currentAction.getActor().y);
        currentMovePath = null;
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap); // Get our first waypoint.
        break;
      case PRODUCTION:
        myGame.gameMap.clearAllHighlights();
        productionMenu.resetOptions(
            myGame.activeCO.getShoppingList(myGame.gameMap.getEnvironment(myGame.getCursorX(), myGame.getCursorY()).terrainType));
        currentMenu = productionMenu;
        break;
      case METAACTION:
        myGame.gameMap.clearAllHighlights();
        if( myGame.activeCO.getReadyAbilities().size() > 0 )
        {
          metaActionMenu.resetOptions(metaActionsAbility);
        }
        else
        {
          metaActionMenu.resetOptions(metaActionsNoAbility);
        }
        currentMenu = metaActionMenu;
        break;
      case CO_ABILITYMENU:
        coAbilityMenu = new InGameMenu<String>(myGame.activeCO.getReadyAbilities());
        currentMenu = coAbilityMenu;
        break;
      case CONFIRMEXIT:
        confirmExitMenu.zero();
        currentMenu = confirmExitMenu;
        break;
      case ANIMATION:
        myGame.gameMap.clearAllHighlights();
        break;
      case EXITGAME:
        currentAction = null;
        myGame.gameMap.clearAllHighlights();
        currentMenu = null;
        currentMovePath = null;
        break;
      case CO_INFO:
        // No action needed.
        break;
      default:
        System.out.println("WARNING! MapController.changeInputMode was given an invalid InputMode " + inputMode);
    }
  }

  private void buildMovePath(int x, int y, GameMap map)
  {
    if( null == currentMovePath )
    {
      currentMovePath = new Path(myView.getMapUnitMoveSpeed());
    }

    // If the new point already exists on the path, cut the extraneous points out.
    for( int i = 0; i < currentMovePath.getPathLength(); ++i )
    {
      if( currentMovePath.getWaypoint(i).x == x && currentMovePath.getWaypoint(i).y == y )
      {
        currentMovePath.snip(i);
        break;
      }
    }

    currentMovePath.addWaypoint(x, y);

    if( !Utils.isPathValid(currentAction.getActor(), currentMovePath, myGame.gameMap) )
    {
      // The currently-built path is invalid. Try to generate a new one (may still return null).
      Utils.findShortestPath(currentAction.getActor(), x, y, currentMovePath, myGame.gameMap);
    }
  }

  /**
   * Execute the provided action and evaluate any aftermath.
   */
  private void executeGameAction(GameAction action)
  {
    // Compile the GameAction to its component events.
    GameEventQueue events = action.getGameEvents(myGame.gameMap);

    // Send the events to the animator. They will be applied/executed in animationEnded().
    changeInputMode(InputMode.ANIMATION);
    myView.animate(events);
  }

  public GameAction getContemplatedAction()
  {
    return currentAction;
  }

  public Path getContemplatedMove()
  {
    return currentMovePath;
  }

  public void animationEnded(GameEvent event, boolean animEventQueueIsEmpty)
  {
    event.performEvent(myGame.gameMap);

    // If we are done animating the last action, check to see if the game is over.
    if( animEventQueueIsEmpty )
    {
      // Count the number of COs that are left.
      int activeNum = 0;
      for( int i = 0; i < myGame.commanders.length; ++i )
      {
        if( !myGame.commanders[i].isDefeated )
        {
          activeNum++;
        }
      }

      // If fewer than two COs yet survive, the game is over.
      if( activeNum < 2 )
      {
        isGameOver = true;
      }
    }

    if( isGameOver && inputMode != InputMode.EXITGAME )
    {
      // The last action ended the game, and the animation just finished.
      //  Now we wait for one more keypress before going back to the main menu.
      changeInputMode(InputMode.EXITGAME);

      // Signal the view to animate the victory/defeat overlay.
      myView.gameIsOver();
    }
    else
    {
      // The animation for the last action just completed. Back to normal input mode.
      changeInputMode(InputMode.MAP);
    }
  }

  public CO_InfoMenu getCoInfoMenu()
  {
    return coInfoMenu;
  }
}
