package Engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import CommandingOfficers.CommanderAbility;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameInput.GameInputHandler;
import Terrain.GameMap;
import Terrain.Location;
import UI.CO_InfoMenu;
import UI.InGameMenu;
import UI.InputHandler;
import UI.InputHandler.InputAction;
import UI.MapView;
import Units.Unit;

public class MapController implements IController, GameInputHandler.StateChangedCallback
{
  private GameInstance myGame;
  private MapView myView;

  // A few menus to control the in-game logical flow.
  private InGameMenu<MetaAction> metaActionMenu;
  private InGameMenu<CommanderAbility> coAbilityMenu;
  private InGameMenu<ConfirmExitEnum> confirmExitMenu;
  private InGameMenu<? extends Object> currentMenu;

  private GameInputHandler myInputStateHandler = null;
  private OptionSelector myInputStateOptionSelector = null;

  private int nextSeekIndex;

  private enum InputMode
  {
    MAP, METAACTION, CO_ABILITYMENU, CONFIRMEXIT, ANIMATION, EXITGAME, CO_INFO
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

  Queue<Unit> unitsToInit = null;
  private boolean isGameOver;

  // We use a different method for the CO Info menu than the others (MetaAction, etc) because
  // it has two different axes of control, and because it has no actions that can result.
  public boolean isInCoInfoMenu = false;
  private CO_InfoMenu coInfoMenu;

  /** Just a simple struct to hold the currently-selected unit and its tentative path. */
  private class ContemplatedAction
  {
    Unit actor = null;
    Path movePath = null;
    boolean aiming = false;

    public void clear()
    {
      actor = null;
      movePath = null;
      aiming = false;
    }
  }
  ContemplatedAction contemplatedAction;

  public MapController(GameInstance game, MapView view)
  {
    myGame = game;
    myView = view;
    myView.setController(this);
    metaActionMenu = new InGameMenu<MetaAction>(metaActionsNoAbility);
    coAbilityMenu = null;
    confirmExitMenu = new InGameMenu<ConfirmExitEnum>(confirmExitOptions);
    inputMode = InputMode.MAP;
    unitsToInit = new ArrayDeque<Unit>();
    isGameOver = false;
    coInfoMenu = new CO_InfoMenu(myGame.commanders.length);
    nextSeekIndex = 0;
    contemplatedAction = new ContemplatedAction();

    // Start the first turn.
    startNextTurn();

    // Initialize our game input handler.
    myInputStateHandler = new GameInputHandler(myGame.gameMap, myGame.activeCO, this);
    myInputStateOptionSelector = null;
  }

  /**
   * When the GameMap is in focus, all user input is directed through this function. It is
   * redirected to a specific handler based on what actions the user is currently taking.
   */
  @Override
  public boolean handleInput(InputHandler.InputAction input)
  {
    boolean exitMap = false;

    GameInputHandler.InputType mode = myInputStateHandler.getInputType();

    if( InputMode.METAACTION == inputMode )
    {
      handleMetaActionMenuInput(input);
    }
    else if( InputMode.CO_INFO == inputMode )
    {
      if( coInfoMenu.handleInput(input) )
      {
        isInCoInfoMenu = false;
        changeInputMode(InputMode.METAACTION);
      }
    }
    else if( InputMode.CONFIRMEXIT == inputMode )
    {
      // If they exit via menu, don't hang around for the victory animation.
      exitMap = handleConfirmExitMenuInput(input);
    }
    else if( InputMode.EXITGAME == inputMode )
    {
      // Once the game is over, wait for an ENTER or BACK input to return to the main menu.
      if( input == InputHandler.InputAction.BACK || input == InputHandler.InputAction.ENTER )
      {
        exitMap = true;
      }
    }
    else if( InputMode.ANIMATION == inputMode )
    {
      if( InputAction.BACK == input || InputAction.ENTER == input )
      {
        myView.cancelAnimation();
      }
    }
    else switch( mode )
    {
      case FREE_TILE_SELECT:
        System.out.println("handling free tile select.");
        handleFreeTileSelect(input);
        break;
      case PATH_SELECT:
        System.out.println("handling path select.");
        handleMovementInput(input);
        break;
      case MENU_SELECT:
        System.out.println("handling menu select.");
        handleActionMenuInput(input);
        break;
      case CONSTRAINED_TILE_SELECT:
        System.out.println("handling constrained tile select.");
        handleConstrainedTileSelect(input);
        break;
      case ACTION_READY:
      default:
        System.out.println("Invalid InputStateHandler mode in MapController! " + mode);
    }

    return exitMap;
  }

  /**
   * Allow a user to move the cursor freely around the map, and to select any tile.
   */
  private void handleFreeTileSelect(InputHandler.InputAction input)
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
      case SEEK: // Move the cursor to either the next unit that is ready to move, or an owned usable property.
        boolean found = false;
        int tries = 0;
        int numUnits = myGame.activeCO.units.size();
        ArrayList<XYCoord> usableProperties = Utils.findUsableProperties(myGame.activeCO, myGame.gameMap);
        int numFreeIndustries = usableProperties.size();
        int maxTries = numUnits + numFreeIndustries;
        while ((maxTries > 0) && !found && (tries < maxTries))
        {
          // Normalize the index to allow wrapping.
          if( nextSeekIndex >= maxTries )
          {
            nextSeekIndex = 0;
          }

          if( nextSeekIndex < numUnits )
          {
            // If we find a unit that is ready to go, move the cursor to it.
            Unit nextUnit = myGame.activeCO.units.get(nextSeekIndex);
            if( !nextUnit.isTurnOver )
            {
              myGame.setCursorLocation(nextUnit.x, nextUnit.y);
              found = true;
            }
          }
          else
          {
            myGame.setCursorLocation(usableProperties.get(nextSeekIndex - numUnits));
            found = true;
          }
          // Increment for the next loop cycle or SEEK input.
          ++tries;
          ++nextSeekIndex;
        }
        break;
      case ENTER:
        // See what is at the current cursor location, in precedence order of Unit, Building, Terrain.
        XYCoord cursorCoords = new XYCoord(myGame.getCursorX(), myGame.getCursorY());
        Location loc = myGame.gameMap.getLocation(cursorCoords);
        Unit unitActor = loc.getResident();
        if( null != unitActor )
        {
          if( unitActor.isTurnOver == false || unitActor.CO != myGame.activeCO )
          {
            contemplatedAction.actor = unitActor;

            myInputStateHandler = new GameInputHandler(myGame.gameMap, myGame.activeCO, this);
            //myInputStateHandler.select(contemplatedAction.actor);
            myInputStateHandler.select(cursorCoords);

            // Calculate movement options.
            //changeInputMode(InputMode.MOVEMENT);
          }
          else
          {
            changeInputMode(InputMode.METAACTION);
          }
        }
        else if( loc.getOwner() == myGame.activeCO && myGame.activeCO.getShoppingList(loc.getEnvironment().terrainType).size() > 0 )
        {
          XYCoord selectCoord = new XYCoord(myGame.getCursorX(), myGame.getCursorY());
          myInputStateHandler.select(selectCoord);
          //changeInputMode(InputMode.PRODUCTION);
        }
        else
        {
          // Display end turn (save/quit?) options.
          changeInputMode(InputMode.METAACTION);
        }
        break;
      case BACK:
        myInputStateHandler.back();
        break;
      default:
        System.out.println("WARNING! MapController.handleMapInput() was given invalid input enum (" + input + ")");
    }
  }

  /** Force the user to select one map tile from the InputStateHandler's selection. */
  private void handleConstrainedTileSelect(InputHandler.InputAction input)
  {
    switch(input)
    {
      case ENTER:
        myInputStateHandler.select(new XYCoord(myGame.getCursorX(), myGame.getCursorY()));
        break;
      case BACK:
        myInputStateHandler.back();
        break;
      case UP:
      case LEFT:
      case DOWN:
      case RIGHT:
        if( myInputStateHandler.getCoordinateOptions().size() == 0)
        {
          // If this option doesn't require a target, it should have been executed from handleActionMenuInput().
          // This function is just for target selection/choosing one action from the set.
          System.out.println("WARNING! Attempting to choose a target for a non-targetable action.");
        }

        ArrayList<XYCoord> targetLocations = myInputStateHandler.getCoordinateOptions();
        myInputStateOptionSelector.handleInput(input);
        myGame.setCursorLocation(targetLocations.get(myInputStateOptionSelector.getSelectionNormalized()));
        break;
      case NO_ACTION:
      case SEEK:     // Seek does nothing in this input state.
      default:
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
        // If this is a unit we can control.
        if( contemplatedAction.actor.CO == myGame.activeCO )
        {
          // Select the location and display possible actions.
          contemplatedAction.movePath.start(); // start the unit running
          myInputStateHandler.select(contemplatedAction.movePath);
        }
        // if we're selecting an enemy unit, hitting enter again will drop that selection
        if( contemplatedAction.actor.CO != myGame.activeCO )
        {
          // TODO: re-selecting the unit should do a threat range check?
          changeInputMode(InputMode.MAP);
        }
        break;
      case BACK:
        changeInputMode(InputMode.MAP);
        myInputStateHandler.back();
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
    if( null == currentMenu )
    {
      System.out.println("ERROR! MapController.handleActionMenuInput() called with no menu.");
      myInputStateHandler.back();
      return;
    }

    if( null == myInputStateHandler.getMenuOptions() )
    {
      System.out.println("ERROR! MapController.handleActionMenuInput() called with no menu options!");
      myInputStateHandler.back();
      return;
    }

    switch (input)
    {
      case ENTER:
        // Pass the user's selection to the state handler.
        myInputStateHandler.select(myInputStateHandler.getMenuOptions()[myInputStateOptionSelector.getSelectionNormalized()]);
        break;
      case BACK:
        myInputStateHandler.back();
        break;
      case NO_ACTION:
        break;
      default:
        myInputStateOptionSelector.handleInput(input);
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
            startNextTurn();
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
        CommanderAbility ability = coAbilityMenu.getSelectedOption();
        ability.activate(myGame.gameMap);

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
   * Updates context information to keep the input state in order.
   */
  @Override
  public void onStateChange()
  {
    GameInputHandler.InputType mode = myInputStateHandler.getInputType();
    myInputStateOptionSelector = null;
    currentMenu = null;

    switch( mode )
    {
      case CONSTRAINED_TILE_SELECT:
        myInputStateOptionSelector = new OptionSelector(myInputStateHandler.getCoordinateOptions().size());
        myGame.setCursorLocation(myInputStateHandler.getCoordinateOptions().get(myInputStateOptionSelector.getSelectionNormalized()));
        contemplatedAction.aiming = true;
        break;
      case MENU_SELECT:
        myInputStateOptionSelector = new OptionSelector(myInputStateHandler.getMenuOptions().length);
        currentMenu = new InGameMenu<>(myInputStateHandler.getMenuOptions());
        contemplatedAction.aiming = false;
        break;
      case ACTION_READY:
        System.out.println("handling ready action.");
        if( null != myInputStateHandler.getReadyAction() )
        {
          executeGameAction(myInputStateHandler.getReadyAction());
          myInputStateHandler.reset(); // Will probably cause several indirectly-recursive calls to this function.
        }
        contemplatedAction.aiming = false;
        break;
      case PATH_SELECT:
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap); // Get our first waypoint.
        break;
      case FREE_TILE_SELECT:
        // This state doesn't require any special handling.
        break;
        default:
          System.out.println("WARNING! Attempting to enter unknown input mode " + mode);
    }
  }

  /**
   * Updates the InputMode and the current menu to keep them in sync.
   */
  private void changeInputMode(InputMode input)
  {
    inputMode = input;
    switch (inputMode)
    {
      case MAP:
        contemplatedAction.clear();
        currentMenu = null;
        myGame.gameMap.clearAllHighlights();
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
        coAbilityMenu = new InGameMenu<CommanderAbility>(myGame.activeCO.getReadyAbilities());
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
        contemplatedAction.clear();
        myGame.gameMap.clearAllHighlights();
        currentMenu = null;
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
    if( null == contemplatedAction.movePath )
    {
      contemplatedAction.movePath = new Path(myView.getMapUnitMoveSpeed());
    }

    // If the new point already exists on the path, cut the extraneous points out.
    for( int i = 0; i < contemplatedAction.movePath.getPathLength(); ++i )
    {
      if( contemplatedAction.movePath.getWaypoint(i).x == x && contemplatedAction.movePath.getWaypoint(i).y == y )
      {
        contemplatedAction.movePath.snip(i);
        break;
      }
    }

    contemplatedAction.movePath.addWaypoint(x, y);

    if( !Utils.isPathValid(contemplatedAction.actor, contemplatedAction.movePath, myGame.gameMap) )
    {
      // The currently-built path is invalid. Try to generate a new one (may still return null).
      contemplatedAction.movePath = Utils.findShortestPath(contemplatedAction.actor, x, y, myGame.gameMap);
    }
  }

  /**
   * Execute the provided action and evaluate any aftermath.
   */
  private void executeGameAction(GameAction action)
  {
    if( null != action )
    {
      // Compile the GameAction to its component events.
      GameEventQueue events = action.getEvents(myGame.gameMap);

      // Send the events to the animator. They will be applied/executed in animationEnded().
      changeInputMode(InputMode.ANIMATION);
      myView.animate(events);
    }
    else
    {
      System.out.println("WARNING! Attempting to execute null GameAction.");
    }
  }

  public Unit getContemplatedActor()
  {
    return contemplatedAction.actor;
  }

  public Path getContemplatedMove()
  {
    return contemplatedAction.movePath;
  }

  public boolean isTargeting()
  {
    return contemplatedAction.aiming;
  }

  public void animationEnded(GameEvent event, boolean animEventQueueIsEmpty)
  {
    if( null != event )
    {
      event.performEvent(myGame.gameMap);

      // Now that the event has been completed, let the world know.
      GameEventListener.publishEvent(event);
    }

    if( animEventQueueIsEmpty && !unitsToInit.isEmpty() )
    {
      animEventQueueIsEmpty = false;
      Unit u = unitsToInit.poll();
      GameEventQueue events = u.initTurn(myGame.gameMap);
      myView.animate(events);
    }

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
  }

  private void startNextTurn()
  {
    nextSeekIndex = 0;

    // Tell the game a turn has changed. This will update the active CO.
    myGame.turn();

    // Reinitialize the InputStateHandler for the new turn.
    myInputStateHandler = new GameInputHandler(myGame.gameMap, myGame.activeCO, this);

    // Add the CO's units to the queue so we can initialize them.
    unitsToInit.addAll(myGame.activeCO.units);

    // Kick off the animation cycle, which will animate/init each unit.
    changeInputMode(InputMode.ANIMATION);
    myView.animate(null);
  }

  public CO_InfoMenu getCoInfoMenu()
  {
    return coInfoMenu;
  }
}
