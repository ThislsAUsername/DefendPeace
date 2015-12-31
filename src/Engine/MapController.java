package Engine;

import Terrain.Environment;
import Terrain.Location;
import UI.InputHandler;
import UI.GameMenu;
import Units.Unit;

public class MapController {

	private GameInstance myGame;
	
	private enum InputMode {MAP, MOVEMENT, ACTIONMENU, ACTION, PRODUCTION, METAACTION};
	public enum GameAction {ATTACK, CAPTURE, LOAD, UNLOAD, WAIT, CANCEL};
	public enum MetaAction {END_TURN};

	private InputMode inputMode;

	private Unit unitActor = null;
	private int cancelX = -1;
	private int cancelY = -1;

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
			if(null != unitActor)
			{
				if(unitActor.isTurnOver == false)
				{
					// Calculate movement options.
					changeInputMode(InputMode.MOVEMENT);
				}
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
			if(inMoveableSpace && InputHandler.isUpHeld() && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorDown();
			}
			break;
		case DOWN:
			myGame.moveCursorDown();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && InputHandler.isDownHeld() && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorUp();
			}
			break;
		case LEFT:
			myGame.moveCursorLeft();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && InputHandler.isLeftHeld() && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorRight();
			}
			break;
		case RIGHT:
			myGame.moveCursorRight();
			// Make sure we don't overshoot the reachable space by accident.
			if(inMoveableSpace && InputHandler.isRightHeld() && !myGame.getCursorLocation().isHighlightSet())
			{
				myGame.moveCursorLeft();
			}
			break;
		case ENTER:
			if(inMoveableSpace && unitActor.CO == myGame.activeCO) // If the selected space is within the reachable area
			{
				// Move the Unit to the location and display possible actions.
				considerMove(unitActor, myGame.getCursorX(), myGame.getCursorY());
				changeInputMode(InputMode.ACTIONMENU);
			}
			break;
		case BACK:
			unitActor = null;
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
			placeUnit(unitActor, cancelX, cancelY);
			changeInputMode(InputMode.MOVEMENT);
			break;
		case NO_ACTION:
			break;
			default:
				myGame.currentMenu.handleMenuInput(input);
		}
		if(readyAction == MapController.GameAction.ATTACK ||
				readyAction == MapController.GameAction.UNLOAD)
		{
			changeInputMode(InputMode.ACTION);
		}
		else if(readyAction == MapController.GameAction.CAPTURE)
		{
			placeUnit(unitActor, unitActor.x, unitActor.y);
			readyAction = null;
			unitActor.isTurnOver = true;
			myGame.gameMap.getLocation(unitActor.x, unitActor.y).capture((int) unitActor.HP);
			changeInputMode(InputMode.MAP);
		}
		else if(readyAction == MapController.GameAction.WAIT)
		{
			placeUnit(unitActor, unitActor.x, unitActor.y);
			readyAction = null;
			unitActor.isTurnOver = true;
			changeInputMode(InputMode.MAP);
		}
		else if(readyAction == MapController.GameAction.LOAD)
		{
			Unit transport = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident();
	
			if(null != transport /* && transport.hasCargoSpace() */) // Already checked!
			{
				unitActor.x = -1;
				unitActor.y = -1;
				transport.heldUnits.add(unitActor);
				
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
						placeUnit(unitActor, unitActor.x, unitActor.y);
						Utils.findActionableLocations(unitTarget, GameAction.ATTACK, myGame.gameMap);
						boolean canCounter = myGame.gameMap.getLocation(unitActor.x, unitActor.y).isHighlightSet() && DamageChart.chartDamage(unitTarget, unitActor) != 0;
						CombatEngine.resolveCombat(unitActor, unitTarget, myGame.gameMap, canCounter);
						actionTaken = true;
						System.out.println("unitActor hp: " + unitActor.HP);
						System.out.println("unitTarget hp: " + unitTarget.HP);
						Utils.findActionableLocations(unitActor, GameAction.ATTACK, myGame.gameMap);
					}
					break;
				case UNLOAD:
					if(null == myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident())
					{
						Unit droppable = unitActor.heldUnits.get(0);
						unitActor.heldUnits.remove(droppable);
						placeUnit(droppable, myGame.getCursorX(), myGame.getCursorY());
						droppable.isTurnOver = true;
						placeUnit(unitActor, unitActor.x, unitActor.y);
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
					unitActor.isTurnOver = true;
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
			Utils.findActionableLocations(unitActor, readyAction, myGame.gameMap);
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
			// TODO: Also, is DamageChart the best place for UnitEnum?
			myGame.currentMenu = new GameMenu(GameMenu.MenuType.PRODUCTION, myGame.activeCO.getShoppingList());
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
	

	private void considerMove(Unit unit, int x, int y)
	{
		// Remove unit from the map
		myGame.gameMap.getLocation(unit.x, unit.y).setResident(null);

		// update our cancellation vars
		cancelX = unit.x;
		cancelY = unit.y;
		// update Unit itself
		unit.x = x;
		unit.y = y;
	}

	private void placeUnit(Unit unit, int x, int y)
	{
		if(myGame.gameMap.getLocation(x, y).getResident() != null)
		{
			System.out.println("Error! Attempting to move unit to an occupied Location!");
			return;
		}
		// Update map
		myGame.gameMap.getLocation(x, y).setResident(unit);

		// update our cancellation vars
		cancelX = -1;
		cancelY = -1;
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
		myGame.activeCO.units.add(unit);
	}
}
