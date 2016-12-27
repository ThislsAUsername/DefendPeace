package Engine;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.Combat.CombatEngine;
import Engine.Combat.CombatModifier;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.Environment.Terrains;
import UI.CO_InfoMenu;
import UI.InputHandler;
import UI.GameMenu;
import UI.MapView;
import Units.Unit;

public class MapController implements IController
{
  private GameInstance myGame;
  private MapView myView;

  private enum InputMode
  {
    MAP, MOVEMENT, ACTIONMENU, ACTION, PRODUCTION, METAACTION, CONFIRMEXIT, ANIMATION, EXITGAME, CO_INFO
  };

  public enum MetaAction
  {
    CO_INFO, MINOR_POWER, MAJOR_POWER, QUIT_GAME, END_TURN
  };
  private MetaAction[] metaActions = {MetaAction.CO_INFO, MetaAction.MINOR_POWER, MetaAction.MAJOR_POWER, MetaAction.QUIT_GAME, MetaAction.END_TURN};

  private enum ConfirmExit
  {
    EXIT_TO_MAIN_MENU, QUIT_APPLICATION
  };
  private ConfirmExit[] confirmExitOptions = {ConfirmExit.EXIT_TO_MAIN_MENU, ConfirmExit.QUIT_APPLICATION};

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
    inputMode = InputMode.MAP;
    isGameOver = false;
    myGame.setCursorLocation(6, 5);
    coInfoMenu = new CO_InfoMenu( myGame.commanders.length );
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
      case CONFIRMEXIT:
        // If they exit via menu, don't hang around for the victory animation.
        exitMap = handleConfirmExitMenuInput(input);
        break;
      case CO_INFO:
        if( coInfoMenu.handleInput( input ) )
        {
          isInCoInfoMenu = false;
          changeInputMode( InputMode.METAACTION );
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

    if (exitMap)
    {
      // re-initialize our list of CombatModifiers, so we know it will be empty for next game
      CombatEngine.modifiers = new ArrayList<CombatModifier>();
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
      case ENTER:
        // See what is at the current cursor location, in precedence order of Unit, Building, Terrain.
        Location loc = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY());
        Unit unitActor = loc.getResident();
        if( null != unitActor )
        {
          if( unitActor.isTurnOver == false || unitActor.CO != myGame.activeCO )
          {
            myView.currentAction = new GameAction(unitActor); // Start building a GameAction

            // Calculate movement options.
            changeInputMode(InputMode.MOVEMENT);
          }
          else
          {
            changeInputMode(InputMode.METAACTION);
          }
        }
        else if( Environment.Terrains.FACTORY == loc.getEnvironment().terrainType && loc.getOwner() == myGame.activeCO )
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
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        // System.out.println("inMoveableSpace = " + inMoveableSpace);
        // Make sure we don't overshoot the reachable tiles by accident.
        if( inMoveableSpace && InputHandler.isUpHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorDown();
        }
        break;
      case DOWN:
        myGame.moveCursorDown();
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isDownHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorUp();
        }
        break;
      case LEFT:
        myGame.moveCursorLeft();
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isLeftHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorRight();
        }
        break;
      case RIGHT:
        myGame.moveCursorRight();
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isRightHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorLeft();
        }
        break;
      case ENTER:
        if( inMoveableSpace && myView.currentAction.getActor().CO == myGame.activeCO ) // If the selected space is within
        // the reachable area
        {
          // Move the Unit to the location and display possible actions.
          currentMovePath.start(); // start the unit running
          myView.currentAction.setMovePath(currentMovePath);
          currentMovePath = null;
          changeInputMode(InputMode.ACTIONMENU);
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
    if( myView.currentMenu == null )
    {
      System.out.println("Error! MapController.handleActionMenuInput() called when myView.currentMenu is null!");
    }

    switch (input)
    {
      case ENTER:
        myView.currentAction.setActionType((GameAction.ActionType) myView.currentMenu.getSelectedAction());

        // If the action is completely constructed, execute it, else get the missing info.
        if( myView.currentAction.isReadyToExecute() )
        {
          executeGameAction( myView.currentAction );
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
        myView.currentMenu.handleMenuInput(input);
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
        if( inActionableSpace && (null != myView.currentAction) )
        {
          myView.currentAction.setActionLocation(myGame.getCursorX(), myGame.getCursorY());

          if( myView.currentAction.isReadyToExecute() )
          {
            executeGameAction( myView.currentAction );
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
    if( myView.currentMenu == null )
    {
      System.out.println("Error! MapController.handleProductionMenuInput() called when currentMenu is null!");
    }

    switch (input)
    {
      case ENTER:
        Units.UnitModel.UnitEnum unit = (Units.UnitModel.UnitEnum) myView.currentMenu.getSelectedAction();

        if( myGame.activeCO.getUnitModel(unit).moneyCost <= myGame.activeCO.money )
        {
          myGame.activeCO.money -= myGame.activeCO.getUnitModel(unit).moneyCost;
          Unit u = new Unit(myGame.activeCO, myGame.activeCO.getUnitModel(unit));
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
        myView.currentMenu.handleMenuInput(input);
    }
  }

  private void handleMetaActionMenuInput(InputHandler.InputAction input)
  {
    if( myView.currentMenu == null )
    {
      System.out.println("Error! MapController.handleMetaActionMenuInput() called when currentMenu is null!");
    }

    switch (input)
    {
      case ENTER:
        MetaAction action = (MetaAction) myView.currentMenu.getSelectedAction();

        if( action == MetaAction.CO_INFO )
        {
          isInCoInfoMenu = true;
          changeInputMode( InputMode.CO_INFO );
        }
        else if( action == MetaAction.MINOR_POWER)
        {
          myGame.activeCO.doAbilityMinor();
          changeInputMode( InputMode.MAP );
        }
        else if( action == MetaAction.MAJOR_POWER)
        {
          myGame.activeCO.doAbilityMajor();
          changeInputMode( InputMode.MAP );
        }
        else if( action == MetaAction.END_TURN )
        {
          myGame.turn();
          changeInputMode(InputMode.MAP);
        }
        else if( action == MetaAction.QUIT_GAME)
        {
          changeInputMode( InputMode.CONFIRMEXIT );
        }
        break;
      case BACK:
        changeInputMode(InputMode.MAP);
        break;
      case NO_ACTION:
        break;
      default:
        myView.currentMenu.handleMenuInput(input);
    }
  }

  private boolean handleConfirmExitMenuInput(InputHandler.InputAction input)
  {
    boolean quitGame = false;
    if( myView.currentMenu == null )
    {
      System.out.println("Error! MapController.handleMetaActionMenuInput() called when currentMenu is null!");
    }

    switch (input)
    {
      case ENTER:
        ConfirmExit action = (ConfirmExit) myView.currentMenu.getSelectedAction();

        if( action == ConfirmExit.EXIT_TO_MAIN_MENU )
        {
          // Go back to the main menu.
          quitGame = true;
        }
        else if( action == ConfirmExit.QUIT_APPLICATION )
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
        myView.currentMenu.handleMenuInput(input);
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
        Utils.findActionableLocations(myView.currentAction.getActor(), myView.currentAction.getActionType(),
            myView.currentAction.getMoveX(), myView.currentAction.getMoveY(), myGame.gameMap);
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
        myView.currentMenu = null;
        break;
      case ACTIONMENU:
        myGame.gameMap.clearAllHighlights();
        myView.currentMenu = new GameMenu(GameMenu.MenuType.ACTION, myView.currentAction.getActor().getPossibleActions(
            myGame.gameMap, myView.currentAction.getMoveX(), myView.currentAction.getMoveY()));
        myGame.setCursorLocation(myView.currentAction.getMoveX(), myView.currentAction.getMoveY());
        myView.currentAction.setActionType(GameAction.ActionType.INVALID); // We haven't chosen an action yet.
        break;
      case MAP:
        myView.currentAction = null;
        currentMovePath = null;
        myView.currentMenu = null;
        myGame.gameMap.clearAllHighlights();

        //        if( unitActor != null )
        //        {
        //          myGame.setCursorLocation(unitActor.x, unitActor.y);
        //          unitActor = null; // We are now in MAP mode; no unit is selected.
        //        }
        break;
      case MOVEMENT:
        Utils.findPossibleDestinations(myView.currentAction.getActor(), myGame);
        if( null != myView.currentAction )
        {
          myView.currentAction.setMovePath(null); // No destination chosen yet.
        }
        myView.currentMenu = null;
        myGame.setCursorLocation(myView.currentAction.getActor().x, myView.currentAction.getActor().y);
        currentMovePath = null;
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap); // Get our first waypoint.
        break;
      case PRODUCTION:
        myGame.gameMap.clearAllHighlights();
        myView.currentMenu = new GameMenu(GameMenu.MenuType.PRODUCTION, myGame.activeCO.getShoppingList());
        break;
      case METAACTION:
        myGame.gameMap.clearAllHighlights();
        myView.currentMenu = new GameMenu(GameMenu.MenuType.METAACTION, metaActions);
        break;
      case CONFIRMEXIT:
        myView.currentMenu = new GameMenu(GameMenu.MenuType.METAACTION, confirmExitOptions);
        break;
      case ANIMATION:
        myGame.gameMap.clearAllHighlights();
        break;
      case EXITGAME:
        myView.currentAction = null;
        myGame.gameMap.clearAllHighlights();
        myView.currentMenu = null;
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

    if( !Utils.isPathValid(myView.currentAction.getActor(), currentMovePath, myGame.gameMap) )
    {
      // The currently-built path is invalid. Try to generate a new one (may still return null).
      Utils.findShortestPath(myView.currentAction.getActor(), x, y, currentMovePath, myGame.gameMap);
    }
  }

  /**
   * Execute the provided action and evaluate any aftermath.
   */
  private void executeGameAction(GameAction action)
  {
    // Do the thing.
    if( action.execute(myGame.gameMap) )
    {
      // Check if there are any game-ending conditions.
      switch( action.getActionType() )
      {
        case ATTACK:
          // A fight happened. See if either CO is out of units.
          if( action.getActor().CO.units.isEmpty() )
          {
            // CO is out of units. Too bad.
            defeatCommander( action.getActor().CO );
          }
          // Now check for the defender.
          if( action.getTargetCO().units.isEmpty() )
          {
            // CO is out of units. Too bad.
            defeatCommander( action.getTargetCO() );
          }
          break;
        case CAPTURE:
          // Something was captured. Figure out who might be losing a property
          Commander targetCO = action.getTargetCO();

          // If the targetCO is non-null (the property being captured is non-neutral),
          //  then verify whether the defending CO still owns his HQ.
          if( targetCO != null && targetCO.HQLocation.getOwner() != targetCO )
          {
            // If targetCO no longer owns his HQ, too bad.
            defeatCommander( targetCO );
          }
          break;
        case INVALID:
        case LOAD:
        case UNLOAD:
        case WAIT:
          default:
            // No potentially game-ending state can be reached with these actions.
      }

      // Count the number of COs that are left.
      int activeNum = 0;
      for( int i = 0; i < myGame.commanders.length; ++i)
      {
        if( !myGame.commanders[i].isDefeated )
        {
          activeNum++;
        }
      }

      // If fewer than two COs yet survive, the game is over.
      if(activeNum < 2)
      {
        isGameOver = true;
      }

      // Kick the action off to the animator.
      changeInputMode(InputMode.ANIMATION);
      myView.animate(myView.currentAction); // Set up the animation for this action.
    }
    else
    {
      System.out.println("ERROR! Action failed to execute!");
      changeInputMode(InputMode.MAP); // try and reset;
    }
  }

  private void defeatCommander(Commander defeatedCO)
  {
    // Set the flag so that we know he's toast.
    defeatedCO.isDefeated = true;

    // Loop through the map and clean up any of the defeated CO's assets.
    GameMap map = myGame.gameMap;
    for(int y = 0; y < map.mapHeight; ++y)
    {
      for(int x = 0; x < map.mapWidth; ++x)
      {
        Location loc = map.getLocation(x, y);

        // Remove any units that remain.
        if(loc.getResident() != null && loc.getResident().CO == defeatedCO)
        {
          loc.setResident(null);
        }
        defeatedCO.units.clear(); // Remove from the CO array too, just to be thorough.

        // Downgrade the defeated commander's HQ to a city.
        defeatedCO.HQLocation.setEnvironment(Environment.getTile(Terrains.CITY, defeatedCO.HQLocation.getEnvironment().weatherType));

        // Release control of any buildings he owned.
        if(loc.isCaptureable() && loc.getOwner() == defeatedCO)
        {
          loc.setOwner(null);
        }
      }
    }
  }

  public Path getContemplatedMove()
  {
    return currentMovePath;
  }

  public void animationEnded()
  {
    if( isGameOver && inputMode != InputMode.EXITGAME )
    {
      // The last action ended the game, and the animation just finished.
      //  Now we wait for one more keypress before going back to the main menu.
      changeInputMode( InputMode.EXITGAME );

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
