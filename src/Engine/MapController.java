package Engine;

import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import UI.InputHandler;
import UI.GameMenu;
import UI.MapView;
import Units.Unit;

public class MapController
{
  private GameInstance myGame;
  private MapView myView;

  private enum InputMode {MAP, MOVEMENT, ACTIONMENU, ACTION, PRODUCTION, METAACTION, ANIMATION};
  public enum MetaAction {END_TURN};

  private InputMode inputMode;

  private Path currentMovePath;

  public MapController(GameInstance game, MapView view)
  {
    myGame = game;
    myView = view;
    myView.setController(this);
    inputMode = InputMode.MAP;
    myGame.setCursorLocation(6,5);
  }

  /**
   * When the GameMap is in focus, all user input is directed through this function. It is
   * redirected to a specific handler based on what actions the user is currently taking.
   */
  public void handleInput(InputHandler.InputAction input)
  {
    System.out.println("handling " + input + " input in " + inputMode + " mode");
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
      case ANIMATION:
        if(input == InputHandler.InputAction.BACK || input == InputHandler.InputAction.ENTER);
        {
            // Tell the animation to cancel.
            myView.cancelAnimation();
        }
        break;
      default:
        System.out.println("Invalid InputMode in MapController! " + inputMode);
    }
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
        else if( Environment.Terrains.FACTORY == loc.getEnvironment().terrainType
            && loc.getOwner() == myGame.activeCO )
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
        if( inMoveableSpace && InputHandler.isUpHeld()
            && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorDown();
        }
        break;
      case DOWN:
        myGame.moveCursorDown();
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isDownHeld()
            && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorUp();
        }
        break;
      case LEFT:
        myGame.moveCursorLeft();
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isLeftHeld()
            && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorRight();
        }
        break;
      case RIGHT:
        myGame.moveCursorRight();
        buildMovePath(myGame.getCursorX(), myGame.getCursorY(), myGame.gameMap);
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isRightHeld()
            && !myGame.getCursorLocation().isHighlightSet() )
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
		if(myView.currentMenu == null)
		{
			System.out.println("Error! MapController.handleActionMenuInput() called when myView.currentMenu is null!");
		}

		switch (input)
		{
		case ENTER:
			myView.currentAction.setActionType( (GameAction.ActionType) myView.currentMenu.getSelectedAction() );

	    // If the action is completely constructed, execute it, else get the missing info.
	    if(myView.currentAction.isReadyToExecute())
	    {
	      if(myView.currentAction.execute(myGame.gameMap))
	      {
            changeInputMode(InputMode.ANIMATION);
            myView.animate(myView.currentAction);
	      }
	      else
	      {
	        System.out.println("ERROR! Action failed to execute!");
	        changeInputMode(InputMode.MAP); // try and reset;
	      }
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

          if(myView.currentAction.isReadyToExecute())
          {
            // Do the thing.
            if(myView.currentAction.execute(myGame.gameMap))
            {
                // Kick it off to the animator.
                changeInputMode(InputMode.ANIMATION);
                myView.animate(myView.currentAction);
            }
            else
            {
              System.out.println("ERROR! Action failed to execute!");
              changeInputMode(InputMode.MAP); // try and reset;
            }
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
    System.out.println("handleProduction");
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
          System.out.println("creating unit");
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

        if( action == MetaAction.END_TURN )
        {
          myGame.turn();
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

  /**
   * Updates the InputMode and the current menu to keep them in sync.
   */
  private void changeInputMode(InputMode input)
  {
    inputMode = input;
    switch (inputMode)
    {
      case ACTION:
        Utils.findActionableLocations(myView.currentAction.getActor(), myView.currentAction.getActionType(), myView.currentAction.getMoveX(), myView.currentAction.getMoveY(), myGame.gameMap);
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
        myView.currentMenu = new GameMenu(GameMenu.MenuType.ACTION,
        		myView.currentAction.getActor().getPossibleActions(myGame.gameMap, myView.currentAction.getMoveX(), myView.currentAction.getMoveY()));
        myGame.setCursorLocation(myView.currentAction.getMoveX(), myView.currentAction.getMoveY());
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
        if(null != myView.currentAction)
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
        myView.currentMenu = new GameMenu(GameMenu.MenuType.PRODUCTION,
            myGame.activeCO.getShoppingList());
        break;
      case METAACTION:
        myGame.gameMap.clearAllHighlights();
        MetaAction[] actions = { MetaAction.END_TURN };
        myView.currentMenu = new GameMenu(GameMenu.MenuType.METAACTION, actions);
        break;
      case ANIMATION:
          myGame.gameMap.clearAllHighlights();
          break;
      default:
        System.out.println("WARNING! MapController.changeInputMode was given an invalid InputMode "
            + inputMode);
    }
  }

  private void buildMovePath(int x, int y, GameMap map)
  {
    if(null == currentMovePath)
    {
      currentMovePath = new Path();
    }

    // If the new point already exists on the path, cut the extraneous points out.
    for(int i = 0; i < currentMovePath.getPathLength(); ++i)
    {
		if(currentMovePath.getWaypoint(i).x == x &&
		   currentMovePath.getWaypoint(i).y == y)
		{
			currentMovePath.snip(i);
			break;
		}
    }

    currentMovePath.addWaypoint(x, y, myView.getMapUnitMoveSpeed());

    if(!Utils.isPathValid(myView.currentAction.getActor(), currentMovePath, myGame.gameMap))
    {
      // The currently-built path is invalid. Try to generate a new one (may still return null).
      currentMovePath = Utils.findShortestPath(myView.currentAction.getActor(), x, y, myView.getMapUnitMoveSpeed(), myGame.gameMap);
    }
  }

  public Path getContemplatedMove()
  {
	  return currentMovePath;
  }

  public void animationEnded()
  {
	  changeInputMode(InputMode.MAP);
  }
}
