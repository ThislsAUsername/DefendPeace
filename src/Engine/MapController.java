package Engine;

import Terrain.Environment;
import Terrain.Location;
import UI.InputHandler;
import UI.GameMenu;
import Units.Unit;

public class MapController {

	private GameInstance myGame;
	
	private enum InputMode {MAP, MOVEMENT, ACTIONMENU, ACTION, PRODUCTION, METAACTION};
	public enum GameAction {ATTACK, LOAD, UNLOAD, WAIT, CANCEL};
	public enum MetaAction {END_TURN};

	private InputMode inputMode;

	// Grid used to flag spaces as actionable or not; used for movement/attack/unload/etc.
	private boolean[][] inputGrid; // TODO - instead of maintaining a separate grid, maybe give Location a 'highlight' attribute?

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
		case METAACTION:
			handleMetaActionMenuInput(input);
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
				for (int i = 0; i < inputGrid.length; i++) {
					for (int j = 0; j < inputGrid[i].length; j++) {
						inputGrid[i][j] = false;
					}
				}
				for (int i = 0; i < inputGrid.length; i++) {
					for (int j = 0; j < inputGrid[i].length; j++) {
						int dist = Math.abs(unitActor.y-j) + Math.abs(unitActor.x-i);
						if (dist <= unitActor.model.movePower) {
							inputGrid[i][j] = true;
						}
					}
				}
				changeInputMode(InputMode.MOVEMENT);
			}
			else if(Environment.Terrains.FACTORY == loc.getEnvironment().terrainType
					&& loc.getOwner() == myGame.activeCO)
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
			// TODO: Figure out what to do here.
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
//			System.out.println("inMoveableSpace = " + inMoveableSpace);
			// Make sure we don't overshoot the reachable tiles by accident.
			if(inMoveableSpace && upHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()])
			{
				myGame.moveCursorDown();
			}
			upHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case DOWN:
			myGame.moveCursorDown();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && downHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()])
			{
				myGame.moveCursorUp();
			}
			downHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case LEFT:
			myGame.moveCursorLeft();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && leftHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()])
			{
				myGame.moveCursorRight();
			}
			leftHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case RIGHT:
			myGame.moveCursorRight();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && rightHeld && !inputGrid[myGame.getCursorX()][myGame.getCursorY()])
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
				changeInputMode(InputMode.ACTIONMENU);
			}
			break;
		case BACK:
			unitActor = null;
			changeInputMode(InputMode.MAP);
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
		if(myGame.currentMenu == null)
		{
			System.out.println("Error! MapController.handleActionMenuInput() called when myGame.currentMenu is null!");
		}

		switch (input)
		{
		case ENTER:
			readyAction = (MapController.GameAction)myGame.currentMenu.getSelectedAction();
			changeInputMode(InputMode.ACTION);
			break;
		case BACK:
			changeInputMode(InputMode.MOVEMENT);
			break;
		case NO_ACTION:
			break;
			default:
				myGame.currentMenu.handleMenuInput(input);
		}
		if(readyAction == MapController.GameAction.WAIT)
		{
			readyAction = null;
			changeInputMode(InputMode.MAP);
		}
		if(readyAction == MapController.GameAction.LOAD)
		{
			// TODO: Figure out how to handle moving onto a space with a transport before loading. 
			Unit transport = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident();
	
			if(null != transport /* && transport.hasCargoSpace() */)
			{
				// TODO: Load up
				
				readyAction = null;
				changeInputMode(InputMode.MAP);
			}
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
				boolean actionTaken = false;
				// Do the thing.
				switch(readyAction)
				{
				case ATTACK:
					Unit unitTarget = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident();
					if(unitTarget != null /* TODO && unitActor can target unitTarget*/)
					{
						//TODO: CombatEngine.resolveCombat(unitActor, unitTarget, myGame.gameMap);
						actionTaken = true;
					}
					break;
				case UNLOAD:
					// TODO: If unitActor is carrying a unit.
					if(null == myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident())
					{
						// TODO: Drop off the carried unit at this location and remove it from unitActor's hold
						actionTaken = true;
					}
					break;
				case CANCEL:
				case WAIT:
					default:
						System.out.println("WARNING! MapController.handleActionInput() was given invalid action enum (" + input + ")");
						changeInputMode(InputMode.ACTIONMENU);
				}
				
				// Only switch back to map mode if we actually acted. 
				if(actionTaken)
				{
					myGame.currentMenu = null;
					changeInputMode(InputMode.MAP);
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
		if(myGame.currentMenu == null)
		{
			System.out.println("Error! MapController.handleProductionMenuInput() called when currentMenu is null!");
		}

		switch (input)
		{
		case ENTER:
			DamageChart.UnitEnum unit = (DamageChart.UnitEnum)myGame.currentMenu.getSelectedAction();

			if (myGame.activeCO.getUnitModel(unit).moneyCost <= myGame.activeCO.money) {
				System.out.println("creating unit");
				myGame.activeCO.money -= myGame.activeCO.getUnitModel(unit).moneyCost;
				Unit u = new Unit(myGame.activeCO, myGame.activeCO.getUnitModel(unit));
				addNewUnit(u, myGame.getCursorX(), myGame.getCursorY());
			} else {
				System.out.println("not enough money");
				}
			changeInputMode(InputMode.MAP);
			break;
		case BACK:
			changeInputMode(InputMode.MAP);
			break;
		case NO_ACTION:
			break;
			default:
				myGame.currentMenu.handleMenuInput(input);
		}
	}
	
	private void handleMetaActionMenuInput(InputHandler.InputAction input)
	{
		if(myGame.currentMenu == null)
		{
			System.out.println("Error! MapController.handleMetaActionMenuInput() called when currentMenu is null!");
		}

		switch (input)
		{
		case ENTER:
			MetaAction action = (MetaAction)myGame.currentMenu.getSelectedAction();

			if(action == MetaAction.END_TURN)
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
				myGame.currentMenu.handleMenuInput(input);
		}
	}
	
	/**
	 * Updates the InputMode and the GameInstance's current menu to keep them in sync.
	 * 
	 * NOTE: This function assumes that unitActor is set correctly before it is called.
	 */
	private void changeInputMode(InputMode input)
	{
		inputMode = input;
		switch(inputMode)
		{
		case ACTION:
			myGame.currentMenu = null;
			break;
		case ACTIONMENU:
			myGame.currentMenu = new GameMenu(GameMenu.MenuType.ACTION, unitActor.getPossibleActions(myGame.gameMap));
			break;
		case MAP:
			myGame.currentMenu = null;
			break;
		case MOVEMENT:
			myGame.currentMenu = null;
			break;
		case PRODUCTION:
			// TODO: Don't hard-code this. Also, is DamageChart the best place for UnitEnum?
			DamageChart.UnitEnum[] units = {DamageChart.UnitEnum.INFANTRY, DamageChart.UnitEnum.MECH};
			myGame.currentMenu = new GameMenu(GameMenu.MenuType.PRODUCTION, units);
			break;
		case METAACTION:
			MetaAction[] actions = {MetaAction.END_TURN}; 
			myGame.currentMenu = new GameMenu(GameMenu.MenuType.METAACTION, actions);
			break;
			default:
				System.out.println("WARNING! MapController.changeInputMode was given an invalid InputMode " + inputMode);
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
