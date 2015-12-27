package Engine;

import Terrain.Environment;
import Terrain.Location;
import UI.InputHandler;
import UI.GameMenu;
import Units.Unit;

public class MapController {

	private GameInstance myGame;
	
	private enum InputMode {MAP, MOVEMENT, ACTIONMENU, ACTION, PRODUCTION};
	public enum GameAction {ATTACK, LOAD, UNLOAD, WAIT, CANCEL};

	private InputMode inputMode;

	// Grid used to flag spaces as actionable or not; used for movement/attack/unload/etc.
	private boolean[][] inputGrid; // TODO - instead of maintaining a separate grid, maybe give Location a 'highlight' attribute?

	GameMenu actionMenu = null;
	Unit unitActor = null;

	// MovementInput variables
	static boolean upHeld = false;
	static boolean downHeld = false;
	static boolean leftHeld = false;
	static boolean rightHeld = false;

	// readied Action
	GameAction readyAction = null;

	public MapController(GameInstance game)
	{
		myGame = game;
		inputMode = InputMode.MAP;
		inputGrid = new boolean[myGame.gameMap.mapWidth][myGame.gameMap.mapHeight];
	}

	/**
	 * When the GameMap is in focus, all user input is directed through this function. It is
	 * redirected to a specific handler based on what actions the user is currently taking.
	 */
	public void handleInput(InputHandler.InputAction input)
	{
		System.out.println("handling " + input + " input in " + inputMode + " mode");
		switch(inputMode)
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
			default:
				System.out.println("Invalid InputMode in MapController! " + inputMode);
		}
	}
	
	/**
	 * When nothing is selected, user inputs go here. This is where the user can pan around the map, or
	 * select a unit or a building to take some action.
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
			unitActor = loc.getResident();
			// If there is a (TODO - still-active) Unit at location
			if(null != unitActor)
			{
				// Calculate movement options.
				//findPossibleDestinations(Unit, myGame, inputGrid); TODO
				inputMode = InputMode.MOVEMENT;
			}
			else if(Environment.Terrains.FACTORY == loc.getEnvironment().terrainType
					/*&& loc.getOwner() == myGame.activeCO*/)
			{
				System.out.println("found a factory");
				// TODO: Don't hard-code this. Also, is DamageChart the best place for UnitEnum?
				DamageChart.UnitEnum[] units = {DamageChart.UnitEnum.INFANTRY};
				actionMenu = new GameMenu(units);
				inputMode = InputMode.PRODUCTION;
			}
			break;
		case BACK:
			// TODO: display save/quit options or whatever.
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
		boolean inMoveableSpace = inputGrid[myGame.getCursorX()][myGame.getCursorY()];

		switch (input)
		{
		case UP:
			myGame.moveCursorUp();
			// Make sure we don't overshoot the reachable tiles by accident.
			if(inMoveableSpace && upHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()]);
			{
				myGame.moveCursorDown();
			}
			upHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case DOWN:
			myGame.moveCursorDown();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && downHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()]);
			{
				myGame.moveCursorUp();
			}
			downHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case LEFT:
			myGame.moveCursorLeft();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && leftHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()]);
			{
				myGame.moveCursorRight();
			}
			leftHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case RIGHT:
			myGame.moveCursorRight();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && rightHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()]);
			{
				myGame.moveCursorLeft();
			}
			rightHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case ENTER:
			if(inMoveableSpace) // If the selected space is within the reachable area
			{
				// Move the Unit to the location and display possible actions.
				moveUnit(unitActor, myGame.getCursorX(), myGame.getCursorY());
				actionMenu = new GameMenu(unitActor.getPossibleActions(myGame.gameMap));
				inputMode = InputMode.ACTIONMENU;
			}
			break;
		case BACK:
			unitActor = null;
			inputMode = InputMode.MAP;
			break;
		case NO_ACTION:
			// TODO - figure out how to make this work correctly so we don't overshoot our reachable Locations on accident.
			upHeld = false;
			downHeld = false;
			leftHeld = false;
			rightHeld = false;
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
		if(actionMenu == null)
		{
			System.out.println("Error! MapController.handleActionMenuInput() called when actionMenu is null!");
		}

		switch (input)
		{
		case ENTER:
			readyAction = (MapController.GameAction)actionMenu.getSelectedAction();
			inputMode = InputMode.ACTION;
			break;
		case BACK:
			actionMenu = null;
			inputMode = InputMode.MOVEMENT;
			break;
		case NO_ACTION:
			break;
			default:
				actionMenu.handleMenuInput(input);
		}
	}

	/**
	 * Once an action has been chosen, we need to choose the action target, and then execute.
	 */
	private void handleActionInput(InputHandler.InputAction input)
	{
		boolean inActionableSpace = inputGrid[myGame.getCursorX()][myGame.getCursorY()];

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
			if(inActionableSpace && (null != readyAction))
			{
				// Do the thing.
				switch(readyAction)
				{
				case ATTACK:
					Unit unitTarget = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident();
					if(unitTarget != null /* TODO && unitActor can target unitTarget*/)
					{
						//TODO: CombatEngine.resolveCombat(unitActor, unitTarget, myGame.gameMap);
					}
					break;
				case LOAD:
					Unit transport = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident();

					if(null != transport /* && transport.hasCargoSpace() */)
					{
						// TODO: Load up
					}
					break;
				case UNLOAD:
					// TODO: If unitActor is carrying a unit.
					if(null == myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident())
					{
						// TODO: Drop off the carried unit at this location and remove it from unitActor's hold
					}
					break;
				case CANCEL:
				case WAIT:
					default:
						System.out.println("WARNING! MapController.handleActionInput() was given invalid action enum (" + input + ")");
						inputMode = InputMode.ACTIONMENU;
				}
				inputMode = InputMode.MAP;
			}
			break;
		case BACK:
			inputMode = InputMode.ACTIONMENU;
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
		if(actionMenu == null)
		{
			System.out.println("Error! MapController.handleProductionMenuInput() called when actionMenu is null!");
		}

		switch (input)
		{
		case ENTER:
			System.out.println("creating unit");
			DamageChart.UnitEnum unit = (DamageChart.UnitEnum)actionMenu.getSelectedAction();

			Unit u = new Unit(myGame.activeCO, myGame.activeCO.getUnitModel(unit));
			addNewUnit(u, myGame.getCursorX(), myGame.getCursorY());
			inputMode = InputMode.MAP;
			break;
		case BACK:
			actionMenu = null;
			inputMode = InputMode.MAP;
			break;
		case NO_ACTION:
			break;
			default:
				actionMenu.handleMenuInput(input);
		}
	}

	private void moveUnit(Unit unit, int x, int y)
	{
		if(myGame.gameMap.getLocation(x, y).getResident() != null)
		{
			System.out.println("Error! Attempting to move unit to an occupied Location!");
			return;
		}
		// Update map
		myGame.gameMap.getLocation(unit.x, unit.y).setResident(null);
		myGame.gameMap.getLocation(x, y).setResident(unit);

		// update Unit itself
		unit.x = x;
		unit.y = y;
	}

	private void addNewUnit(Unit unit, int x, int y)
	{
		if(myGame.gameMap.getLocation(x, y).getResident() != null)
		{
			System.out.println("Error! Attempting to add a unit to an occupied Location!");
			return;
		}

		myGame.gameMap.getLocation(x, y).setResident(unit);
		unit.x = x;
		unit.y = y;
	}
}
