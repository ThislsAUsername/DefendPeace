package Engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import CommandingOfficers.CommanderAbility;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
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

  // A few menus to control the in-game logical flow.
  private InGameMenu<UnitModel> productionMenu;
  private InGameMenu<GameActionSet> actionMenu;
  private InGameMenu<MetaAction> metaActionMenu;
  private InGameMenu<CommanderAbility> coAbilityMenu;
  private InGameMenu<ConfirmExitEnum> confirmExitMenu;
  private InGameMenu<? extends Object> currentMenu;

  private int nextSeekIndex;

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
    GameAction action = null;

    public void clear()
    {
      actor = null;
      movePath = null;
      action = null;
    }
  }
  ContemplatedAction contemplatedAction;

  public MapController(GameInstance game, MapView view)
  {
    myGame = game;
    myView = view;
    myView.setController(this);
    productionMenu = new InGameProductionMenu(myGame.commanders[0].getShoppingList(TerrainType.FACTORY)); // Just init with a valid default.
    actionMenu = null;
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
        Location loc = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY());
        Unit unitActor = loc.getResident();
        if( null != unitActor )
        {
          if( unitActor.isTurnOver == false || unitActor.CO != myGame.activeCO )
          {
            contemplatedAction.actor = unitActor;

            // Calculate movement options.
            changeInputMode(InputMode.MOVEMENT);
          }
          else
          {
            changeInputMode(InputMode.METAACTION);
          }
        }
        else if( loc.getOwner() == myGame.activeCO && myGame.activeCO.getShoppingList(loc.getEnvironment().terrainType).size() > 0 )
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
        // If the selected space is within the reachable area
        if( inMoveableSpace && contemplatedAction.actor.CO == myGame.activeCO )
        {
          // Select the location and display possible actions.
          contemplatedAction.movePath.start(); // start the unit running
          changeInputMode(InputMode.ACTIONMENU);
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
    if( currentMenu != actionMenu || null == actionMenu )
    {
      System.out.println("ERROR! MapController.handleActionMenuInput() called with wrong menu active!");
      return;
    }

    switch (input)
    {
      case ENTER:
        GameActionSet actionSet = actionMenu.getSelectedOption();

        // If the action type requires no target, there should only be one
        // GameAction in the set. Execute it.
        if( !actionSet.isTargetRequired() )
        {
          executeGameAction(actionSet.getSelected());
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
    if( null == actionMenu )
    {
      System.out.println("ERROR! MapController.handleActionInput() called with null action menu!");
      return;
    }

    GameActionSet actionOptions = actionMenu.getSelectedOption();

    if( !actionOptions.isTargetRequired() )
    {
      // If this option doesn't require a target, it should have been executed from handleActionMenuInput().
      // This function is just for target selection/choosing one action from the set.
      System.out.println("WARNING! Attempting to choose a target for a non-targetable action.");
    }

    switch (input)
    {
      case UP:
      case LEFT:
        // Select the previous possible action, set it into the contemplated action, and
        //  update the cursor location to the action's target locatin.
        actionOptions.prev();
        myGame.setCursorLocation(actionOptions.getSelected().getTargetLocation());
        contemplatedAction.action = actionOptions.getSelected();
        break;
      case DOWN:
      case RIGHT:
        // Select the next possible action, set it into the contemplated action, and
        //  update the cursor location to the action's target locatin.
        actionOptions.next();
        myGame.setCursorLocation(actionOptions.getSelected().getTargetLocation());
        contemplatedAction.action = actionOptions.getSelected();
        break;
      case ENTER:
        GameAction action = actionOptions.getSelected();
        if( null != action )
        {
          executeGameAction(action);
        }
        else
        {
          // Something went really wonky.
          System.out.println("Attempting to execute a null GameAction! Ignoring.");
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
   * Updates the InputMode and the current menu to keep them in sync.
   */
  private void changeInputMode(InputMode input)
  {
    inputMode = input;
    switch (inputMode)
    {
      case ACTION: // Provide a target for the chosen action.
        GameActionSet possibleActions = actionMenu.getSelectedOption();
        Utils.highlightLocations(myGame.gameMap, possibleActions.getTargetedLocations());

        // Set the contemplated action to the first possible action, and move the
        //  cursor to the targeted location.
        myGame.setCursorLocation(possibleActions.getSelected().getTargetLocation());
        contemplatedAction.action = possibleActions.getSelected();
        currentMenu = null;
        break;
      case ACTIONMENU: // Select which action to perform.
        myGame.gameMap.clearAllHighlights();
        actionMenu = new InGameMenu<GameActionSet>(contemplatedAction.actor.getPossibleActions(myGame.gameMap,
            contemplatedAction.movePath));
        currentMenu = actionMenu;
        myGame.setCursorLocation(contemplatedAction.movePath.getEnd().x, contemplatedAction.movePath.getEnd().y);
        break;
      case MAP:
        contemplatedAction.clear();
        currentMenu = null;
        myGame.gameMap.clearAllHighlights();
        break;
      case MOVEMENT:
        Utils.findPossibleDestinations(contemplatedAction.actor, myGame.gameMap);
        contemplatedAction.movePath = null;
        currentMenu = null;
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
    // Compile the GameAction to its component events.
    GameEventQueue events = action.getEvents(myGame.gameMap);

    // Send the events to the animator. They will be applied/executed in animationEnded().
    changeInputMode(InputMode.ANIMATION);
    myView.animate(events);
  }

  public Unit getContemplatedActor()
  {
    return contemplatedAction.actor;
  }

  public Path getContemplatedMove()
  {
    return contemplatedAction.movePath;
  }

  public GameAction getContemplatedAction()
  {
    return contemplatedAction.action;
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
