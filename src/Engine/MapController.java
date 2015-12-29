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
		boolean inMoveableSpace = myGame.getCursorLocation().isHighlightSet();

		switch (input)
		{
		case UP:
			myGame.moveCursorUp();
//			System.out.println("inMoveableSpace = " + inMoveableSpace);
			// Make sure we don't overshoot the reachable tiles by accident.
			if(inMoveableSpace && upHeld && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorDown();
			}
			upHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case DOWN:
			myGame.moveCursorDown();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && downHeld && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorUp();
			}
			downHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case LEFT:
			myGame.moveCursorLeft();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && leftHeld && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorRight();
			}
			leftHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case RIGHT:
			myGame.moveCursorRight();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && rightHeld && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorLeft();
			}
			rightHeld = true; // Set true after the check, so it is still possible to move out of the reachable tiles.
			break;
		case ENTER:
			if(inMoveableSpace && unitActor.CO == myGame.activeCO) // If the selected space is within the reachable area
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
			break;
		case BACK:
			changeInputMode(InputMode.MOVEMENT);
			break;
		case NO_ACTION:
			break;
			default:
				myGame.currentMenu.handleMenuInput(input);
		}
		if(readyAction == MapController.GameAction.ATTACK)
		{
			changeInputMode(InputMode.ACTION);
		}
		else if(readyAction == MapController.GameAction.WAIT)
		{
			readyAction = null;
			changeInputMode(InputMode.MAP);
		}
		else if(readyAction == MapController.GameAction.LOAD)
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
			if(inActionableSpace && (null != readyAction))
			{
				boolean actionTaken = false;
				// Do the thing.
				switch(readyAction)
				{
				case ATTACK:
					Unit unitTarget = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident();
					if(unitTarget != null && DamageChart.chartDamage(unitActor, unitTarget) != 0)
					{
						Utils.findActionableLocations(unitTarget, null, myGame);
						boolean canCounter = myGame.gameMap.getLocation(unitActor.x, unitActor.y).isHighlightSet() && DamageChart.chartDamage(unitTarget, unitActor) != 0;
						CombatEngine.resolveCombat(unitActor, unitTarget, myGame.gameMap, canCounter);
						actionTaken = true;
						System.out.println("unitActor hp: " + unitActor.HP);
						System.out.println("unitTarget hp: " + unitTarget.HP);
						Utils.findActionableLocations(unitActor, null, myGame);
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
			Utils.findActionableLocations(unitActor, null, myGame);
			myGame.currentMenu = null;
			break;
		case ACTIONMENU:
			myGame.gameMap.clearAllHighlights();
			readyAction = null;
			myGame.currentMenu = new GameMenu(GameMenu.MenuType.ACTION, unitActor.getPossibleActions(myGame.gameMap));
			break;
		case MAP:
			if(unitActor != null)
			{
				myGame.setCursorLocation(unitActor.x, unitActor.y);
				unitActor = null; // We are now in MAP mode; no unit is selected.
			}
			myGame.gameMap.clearAllHighlights();
			myGame.currentMenu = null;
			break;
		case MOVEMENT:
			Utils.findPossibleDestinations(unitActor, myGame);
			myGame.currentMenu = null;
			break;
		case PRODUCTION:
			myGame.gameMap.clearAllHighlights();
			// TODO: Don't hard-code this. Also, is DamageChart the best place for UnitEnum?
			DamageChart.UnitEnum[] units = {DamageChart.UnitEnum.INFANTRY, DamageChart.UnitEnum.MECH};
			myGame.currentMenu = new GameMenu(GameMenu.MenuType.PRODUCTION, units);
			break;
		case METAACTION:
			myGame.gameMap.clearAllHighlights();
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
