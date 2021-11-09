package Engine;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.TurnInitEvent;
import Engine.GameInput.GameInputHandler;
import Terrain.MapLocation;
import UI.CO_InfoController;
import UI.DamageChartController;
import UI.GameStatsController;
import UI.InGameMenu;
import UI.InputHandler;
import UI.InputHandler.InputAction;
import UI.InputOptionsController;
import UI.MapView;
import Units.Unit;

public class MapController implements IController, GameInputHandler.StateChangedCallback
{
  private GameInstance myGame;
  private MapView myView;

  // A menu to display options to the player.
  private InGameMenu<? extends Object> currentMenu;

  // A GameInputHandler to convert inputs into player actions, and
  // a reference to the current GameInputState's OptionSelector.
  private GameInputHandler myGameInputHandler = null;
  private OptionSelector myGameInputOptionSelector = null;

  private int nextSeekIndex;
  private ArrayList<XYCoord> seekLocations;

  private enum InputMode
  {
    INPUT, ANIMATION, EXITGAME
  };

  private InputMode inputMode;

  private boolean isGameOver;

  public MapController(GameInstance game, MapView view)
  {
    myGame = game;
    myView = view;
    myView.setController(this);
    inputMode = InputMode.INPUT;
    isGameOver = false;
    nextSeekIndex = 0;

    // Start the first turn (or the next one if loading a protected save).
    if( myGame.requireInitOnLoad() )
      startNextTurn();

    // Initialize our game input handler.
    myGameInputHandler = new GameInputHandler(myGame.activeCO.myView, myGame.activeCO, this);
  }

  /**
   * When the GameMap is in focus, all user input is directed through this function. It is
   * redirected to a specific handler based on what actions the user is currently taking.
   * @return True if the game is over, false otherwise.
   */
  @Override
  public boolean handleInput(InputHandler.InputAction input)
  {
    boolean exitMap = false;

    switch (inputMode)
    {
      case ANIMATION:
        if( InputAction.BACK == input || InputAction.SELECT == input )
        {
          myView.cancelAnimation();
        }
        break;
      case EXITGAME:
        // Once the game is over, wait for an ENTER or BACK input to return to the main menu.
        if( input == InputHandler.InputAction.BACK || input == InputHandler.InputAction.SELECT )
        {
          exitMap = true;
        }
        break;
      case INPUT:
          exitMap = handleGameInput(input);
        break;
      default:
        System.out.println("WARNING! Received invalid InputAction " + input);
    }

    if( exitMap )
    {
      myGame.endGame();
    }

    return exitMap;
  }

  /**
   * Use the GameInputHandler to make sense of the user's input.
   */
  private boolean handleGameInput(InputHandler.InputAction input)
  {
    GameInputHandler.InputType mode = myGameInputHandler.getInputType();

    switch (mode)
    {
      case FREE_TILE_SELECT:
        handleFreeTileSelect(input);
        break;
      case PATH_SELECT:
        handlePathSelect(input);
        break;
      case MENU_SELECT:
        handleActionMenuInput(input);
        break;
      case CONSTRAINED_TILE_SELECT:
        handleConstrainedTileSelect(input);
        break;
      case ACTION_READY:
      default:
        System.out.println("Invalid InputStateHandler mode in MapController! " + mode);
    }
    Object[] options = myGameInputHandler.getMenuOptions();
    OptionSelector selector = myGameInputOptionSelector;
    if( null != options && options.length > 0 && null != selector )
      myGameInputHandler.consider(options[selector.getSelectionNormalized()]);
    else
      myGameInputHandler.consider(myGame.getCursorCoord());

    return myGameInputHandler.shouldLeaveMap();
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
        
        if( null == seekLocations )
        {
          // Populate our list of seek candidates and sort them by distance from the cursor.
          nextSeekIndex = 0; // We are going to rebuild the list; start from the start.
          boolean seekBuildingsLast = InputOptionsController.seekBuildingsLastOption.getSelectedObject();

          // First get all active units, sorted.
          ArrayList<XYCoord> unitLocations = new ArrayList<XYCoord>();
          unitLocations.addAll(Utils.findLocationsNearUnits(myGame.gameMap, myGame.activeCO.units, 0));
          unitLocations.removeIf(xy -> myGame.gameMap.getLocation(xy).getResident().isTurnOver);
          if( seekBuildingsLast )
            Utils.sortLocationsByDistance(myGame.getCursorCoord(), unitLocations);

          // Find all usable properties, sorted.
          ArrayList<XYCoord> usableProperties = Utils.findUsableProperties(myGame.activeCO, myGame.gameMap);
          usableProperties.removeIf(xy -> myGame.gameMap.getLocation(xy).getResident() != null);
          if( seekBuildingsLast )
            Utils.sortLocationsByDistance(myGame.getCursorCoord(), usableProperties);

          // We'll loop over units first, then properties.
          seekLocations = new ArrayList<XYCoord>();
          seekLocations.addAll(unitLocations);
          seekLocations.addAll(usableProperties);
          if( !seekBuildingsLast )
            Utils.sortLocationsByDistance(myGame.getCursorCoord(), seekLocations);
        }

        if( !seekLocations.isEmpty() )
        {
          // Normalize the index to allow wrapping.
          if( nextSeekIndex >= seekLocations.size() )
          {
            nextSeekIndex = 0;
          }

          // Move to the next location.
          XYCoord seekCoord = seekLocations.get(nextSeekIndex++);

          // Don't allow seeking to the current location.
          if( myGame.getCursorCoord().equals(seekCoord) ) seekCoord = seekLocations.get(nextSeekIndex++ % seekLocations.size());

          myGame.setCursorLocation(seekCoord);
        }

        break;
      case SELECT:
        // Pass the current cursor location to the GameInputHandler.
        myGameInputHandler.select(myGame.getCursorCoord());
        break;
      case BACK:
        if( !myGameInputHandler.isTargeting() )
        {
          // If we hit BACK while over a unit, add it to the threat overlay for this CO
          MapLocation loc = myGame.gameMap.getLocation(myGame.getCursorCoord());
          Unit resident = loc.getResident();
          if( null != resident )
          {
            ArrayList<Unit> threats = myGame.activeCO.threatsToOverlay;
            if( threats.contains(resident) )
              threats.remove(resident);
            else
              threats.add(resident);
          }
        }
        myGameInputHandler.back();
        break;
      default:
        System.out.println("WARNING! MapController.handleFreeTileSelect() was given invalid input enum (" + input + ")");
    }
  }

  /** Force the user to select one map tile from the InputStateHandler's selection. */
  private void handleConstrainedTileSelect(InputHandler.InputAction input)
  {
    ArrayList<XYCoord> targetLocations = myGameInputHandler.getCoordinateOptions();

    switch (input)
    {
      case SELECT:
        myGameInputHandler.select(myGame.getCursorCoord());
        break;
      case BACK:
        myGameInputHandler.back();
        break;
      case UP:
      case LEFT:
      case DOWN:
      case RIGHT:
        if( myGameInputHandler.getCoordinateOptions().size() == 0 )
        {
          // If this option doesn't require a target, it should have been executed from handleActionMenuInput().
          // This function is just for target selection/choosing one action from the set.
          System.out.println("WARNING! Attempting to choose a target for a non-targetable action.");
        }

        // Switch to free-tile select in target-rich environments
        boolean useFreeSelect = false;
        if( targetLocations.size() > 6 )
          useFreeSelect = true;

        if( useFreeSelect )
          handleFreeTileSelect(input);
        else
        {
          myGameInputOptionSelector.handleInput(input);
          myGame.setCursorLocation(targetLocations.get(myGameInputOptionSelector.getSelectionNormalized()));
        }
        break;
      case SEEK: // SEEK allows constrained select even when in target-rich environments
        myGameInputOptionSelector.handleInput(InputHandler.InputAction.DOWN);
        myGame.setCursorLocation(targetLocations.get(myGameInputOptionSelector.getSelectionNormalized()));
      default:
    }
  }

  /**
   * When a unit is selected, user input flows through here to choose where the unit should move.
   */
  private void handlePathSelect(InputHandler.InputAction input)
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
        break;
      case DOWN:
        myGame.moveCursorDown();
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isDownHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorUp();
        }
        break;
      case LEFT:
        myGame.moveCursorLeft();
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isLeftHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorRight();
        }
        break;
      case RIGHT:
        myGame.moveCursorRight();
        // Make sure we don't overshoot the reachable space by accident.
        if( inMoveableSpace && InputHandler.isRightHeld() && !myGame.getCursorLocation().isHighlightSet() )
        {
          myGame.moveCursorLeft();
        }
        break;
      case SELECT:
        myGameInputHandler.select(myGame.getCursorCoord());
        break;
      case BACK:
        myGameInputHandler.back();
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
      myGameInputHandler.back();
      return;
    }

    if( null == myGameInputHandler.getMenuOptions() || 0 == myGameInputHandler.getMenuOptions().length )
    {
      System.out.println("ERROR! MapController.handleActionMenuInput() called with no menu options!");
      myGameInputHandler.back();
      return;
    }

    switch (input)
    {
      case SELECT:
        // Pass the user's selection to the state handler.
        myGameInputHandler.select(myGameInputHandler.getMenuOptions()[myGameInputOptionSelector.getSelectionNormalized()]);
        break;
      case BACK:
        myGameInputHandler.back();
        break;
      default:
        currentMenu.handleMenuInput(input);
    }
  }

  /**
   * Updates context information to keep the input state in order.
   */
  @Override
  public void onStateChange()
  {
    // Whenever we have a context change, clear the seek list.
    seekLocations = null;

    GameInputHandler.InputType inputType = myGameInputHandler.getInputType();
    myGameInputOptionSelector = myGameInputHandler.getOptionSelector();

    myGame.gameMap.clearAllHighlights();
    // Set the target-location highlights.
    ArrayList<XYCoord> options = myGameInputHandler.getCoordinateOptions();
    if ( null != options)
      for( XYCoord xyc : options )
      {
        myGame.gameMap.getLocation(xyc).setHighlight(true);
      }

    currentMenu = null;

    switch (inputType)
    {
      case CONSTRAINED_TILE_SELECT:
        if( null != options && options.size() > 0 )
          myGame.setCursorLocation(options.get(0));
        break;
      case FREE_TILE_SELECT:
      case PATH_SELECT:
        break; // no special behavior
      case MENU_SELECT:
        GamePath path = myGameInputHandler.myStateData.path;
        if( null != path && path.getPathLength() > 0 )
        {
          myGame.setCursorLocation(path.getEnd().GetCoordinates());
        }
        else
        {
          XYCoord coord = myGameInputHandler.getUnitCoord();
          if( null != coord )
          {
            myGame.setCursorLocation(coord);
          }
        }
        currentMenu = new InGameMenu<>(myGameInputHandler.getMenuOptions(), myGameInputOptionSelector);
        break;
      case ACTION_READY:
        if( null != myGameInputHandler.getReadyAction() )
        {
          executeGameAction(myGameInputHandler.getReadyAction());
        }
        myGameInputHandler.reset(); // Reset the input handler to get rid of stale state
        break;
      case END_TURN:
        // If security is enabled, save and quit at the end of each turn after the first.
        if( myGame.isSecurityEnforced() )
        {
          // Generate a password if needed.
          if( !myGame.activeCO.hasPassword() )
          {
            PasswordManager.setPass(myGame.activeCO);
          }

          // Save the game, display a message, and exit to the main menu.
          boolean endTurn = true;
          String saveName = SerializationUtils.writeSave(myGame, endTurn);
          ArrayList<String> saveMsg = new ArrayList<String>();
          saveMsg.add("Saved game to");
          saveMsg.add(saveName);

          GameEventQueue outro = new GameEventQueue();
          boolean hideMap = true;
          outro.add(new TurnInitEvent(myGame.activeCO, myGame.getCurrentTurn(), hideMap, saveMsg));
          myView.animate(outro);

          changeInputMode(InputMode.EXITGAME);
        }
        else // If security is off, just go to the next turn.
          startNextTurn();
        break;
      case LEAVE_MAP:
        // Handled as a special case in handleGameInput().
        break;
      case SAVE:
        boolean advanceTurnOnLoad = false;
        SerializationUtils.writeSave(myGame, advanceTurnOnLoad);
        myGameInputHandler.reset(); // SAVE is a terminal state. Reset the input handler.
        break;
      case CO_STATS:
        GameStatsController coStatsMenu = new GameStatsController(myGame);
        IView statsView = Driver.getInstance().gameGraphics.createInfoView(coStatsMenu);

        myGameInputHandler.reset(); // CO_STATS is a terminal state. Reset the input handler.
        // Give the new controller/view the floor
        Driver.getInstance().changeGameState(coStatsMenu, statsView);
        break;
      case CO_INFO:
        CO_InfoController coInfoMenu = new CO_InfoController(myGame);
        IView infoView = Driver.getInstance().gameGraphics.createInfoView(coInfoMenu);

        myGameInputHandler.reset(); // CO_INFO is a terminal state. Reset the input handler.
        // Give the new controller/view the floor
        Driver.getInstance().changeGameState(coInfoMenu, infoView);
        break;
      case DAMAGE_CHART:
        // Pull out the first enemy available, or ourselves
        Commander targetCO = myGame.activeCO;
        for( Commander co : myGame.commanders )
          if( myGame.activeCO.isEnemy(co) )
          {
            targetCO = co;
            break;
          }
        DamageChartController dcc = new DamageChartController(myGame.activeCO, targetCO);
        IView dcv = Driver.getInstance().gameGraphics.createDamageChartView(dcc);

        myGameInputHandler.reset(); // DAMAGE_CHART is a terminal state. Reset the input handler.
        // Give the new controller/view the floor
        Driver.getInstance().changeGameState(dcc, dcv);
        break;
      default:
        System.out.println("WARNING! Attempting to switch to unknown input type " + inputType);
    }
  }

  /**
   * Updates the InputMode and the current menu to keep them in sync.
   */
  private void changeInputMode(InputMode input)
  {
    // Assign the new input mode.
    if( inputMode != InputMode.EXITGAME )
      inputMode = input;

    if( null != myGameInputHandler )
    {
      // If we are changing input modes, we
      // know we don't have a valid action right now.
      myGameInputHandler.reset();
      currentMenu = null;
    }
  }

  /**
   * Execute the provided action and evaluate any aftermath.
   */
  private boolean executeGameAction(GameAction action)
  {
    boolean actionOK = false; // Not sure if it's a well-formed action yet.
    if( null != action )
    {
      // Compile the GameAction to its component events.
      GameEventQueue events = action.getEvents(myGame.gameMap);

      if( events.size() > 0 )
      {
        actionOK = true; // Invalid actions don't produce events.
        // Send the events to the animator. They will be applied/executed in animationEnded().
        changeInputMode(InputMode.ANIMATION);
        myView.animate(events);
      }
    }
    else
    {
      System.out.println("WARNING! Attempting to execute null GameAction.");
    }
    return actionOK;
  }

  public void animationEnded(GameEvent event, boolean animEventQueueIsEmpty)
  {
    if( null != event )
    {
      event.performEvent(myGame.gameMap);

      // Now that the event has been completed, let the world know.
      GameEventQueue events = GameEventListener.publishEvent(event, myGame);
      if( !events.isEmpty() )
      {
        animEventQueueIsEmpty = false;
        myView.animate(events);
      }
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
        // The animation for the last action just completed. If an AI is in control,
        // fetch the next action. Otherwise, return control to the player.
        if( myGame.activeCO.isAI() )
        {
          GameAction aiAction = myGame.activeCO.getNextAIAction(myGame.gameMap);
          boolean endAITurn = false;
          if( aiAction != null )
          {
            if( !executeGameAction(aiAction) )
            {
              // If aiAction fails to execute, the AI's turn is over. We don't want
              // to waste time getting more actions if it can't build them properly.
              System.out.println("WARNING! AI Action " + aiAction.toString() + " Failed to execute!");
              endAITurn = true;
            }
          }
          else
          {
            endAITurn = true;
          } // The AI can return a null action to signal the end of its turn.
          if( endAITurn )
            startNextTurn();
        }
        else
        {
          // Back to normal input mode.
          changeInputMode(InputMode.INPUT);
          myGameInputHandler.reset();
        }
      }
    }
  }

  private void startNextTurn()
  {
    nextSeekIndex = 0;

    // Tell the game a turn has changed. This will update the active CO.
    GameEventQueue turnEvents = new GameEventQueue();
    boolean turnOK = myGame.turn(turnEvents);

    // Reinitialize the InputStateHandler for the new turn.
    myGameInputHandler = new GameInputHandler(myGame.activeCO.myView, myGame.activeCO, this);

    if( !turnEvents.isEmpty() ) // If there's nothing to animate, don't animate it twice
    {
      // Kick off the animation cycle, which will animate/init each unit.
      myView.animate(turnEvents);

      // If the GameInstance isn't allowing the next CO to go, exit to the main menu.
      changeInputMode(turnOK ? InputMode.ANIMATION : InputMode.EXITGAME);
    }
    else
    {
      myView.animate(null);
    }
  }

  public Unit getContemplatedActor()
  {
    return myGameInputHandler.getActingUnit();
  }

  public XYCoord getContemplationCoord()
  {
    return myGameInputHandler.getUnitCoord();
  }

  public GamePath getContemplatedMove()
  {
    return myGameInputHandler.myStateData.path;
  }

  public boolean isTargeting()
  {
    return myGameInputHandler.isTargeting();
  }

  public Collection<DamagePopup> getDamagePopups()
  {
    return myGameInputHandler.myStateData.damagePopups;
  }

  /** Returns the currently-active in-game menu, or null if no menu is in use. */
  public InGameMenu<? extends Object> getCurrentGameMenu()
  {
    return currentMenu;
  }
}
