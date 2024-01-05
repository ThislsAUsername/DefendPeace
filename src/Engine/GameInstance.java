package Engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;

import CommandingOfficers.Commander;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.TurnInitEvent;
import Engine.StateTrackers.StateTracker;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Units.Unit;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.MapPerspective;

public class GameInstance implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public String saveFile;
  public Terrain.MapMaster gameMap;
  public Army[] armies;
  private int activeCoNum;
  public Army activeArmy = null;
  private int cursorX = 0;
  private int cursorY = 0;

  HashMap<Integer, XYCoord> playerCursors = null;

  private Weathers defaultWeather;

  private GameScenario gameScenario;
  public final GameScenario.GameRules rules;
  boolean isSecurityEnabled;

  private transient SplittableRandom rng;
  public final long rngSeed;
  private long rngNumbersGenerated = 0; // This isn't big enough to hold the period of our RNG, but one can hope games won't have enough combat to overflow this

  /** Measured in CO-turns, not days. */
  private int fogOnUntil;
  private int currentDay;
  private boolean currentTurnEnded = true; // Set to false when saving a game mid-turn.

  public GameInstance(Army[] armies, MapMaster map)
  {
    this(new GameScenario(), armies, map, Weathers.CLEAR, false);
  }
  public GameInstance(GameScenario scenario, Army[] armies, MapMaster map, Weathers weather, boolean useSecurity)
  {
    if( armies.length < 2 )
    {
      System.out.println("WARNING! Creating a game with fewer than two armies.");
    }
    gameScenario = scenario;
    rules = scenario.rules;
    isSecurityEnabled = useSecurity;

    rngSeed = new SplittableRandom().nextLong();
    rng = new SplittableRandom(rngSeed);

    fogOnUntil = 0;
    currentDay = 1;

    gameMap = map;
    gameMap.game = this;
    defaultWeather = weather;
    // Pre-populate weather for day 1 turn 1
    for( int i = 0; i < gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < gameMap.mapHeight; j++ )
      {
        MapLocation loc = gameMap.getLocation(i, j);
        loc.setForecast(defaultWeather, 0);
      }
    }

    this.armies = armies;
    activeCoNum = -1; // No army is active yet.

    // Set the initial cursor locations for each player.
    playerCursors = new HashMap<Integer, XYCoord>();
    for( int i = 0; i < armies.length; ++i )
    {
      armies[i].money = gameScenario.rules.startingFunds;
      armies[i].myView = new MapPerspective(map, armies[i]);
      armies[i].myView.game = this;
      armies[i].myView.resetFog();
      if( !armies[i].HQLocations.isEmpty() )
      {
        playerCursors.put(i, armies[i].HQLocations.get(0));
      }
      else
      {
        System.out.println("Warning! Army " + i + " does not have an HQ location!");
        playerCursors.put(i, new XYCoord(1, 1));
      }
      armies[i].initForGame(this);
    }
    setCursorLocation(playerCursors.get(0).x, playerCursors.get(0).y);
    
    saveFile = getSaveName();
  }

  public int calcCurrentCOTurn()
  {
    return (currentDay * armies.length) + activeCoNum;
  }
  public boolean isFogEnabled()
  {
    return fogOnUntil > calcCurrentCOTurn() || rules.fogMode.fogDefaultsOn;
  }
  public void setFog(int days)
  {
    int thisTurn = calcCurrentCOTurn();
    int extraTurns = days * armies.length;
    fogOnUntil = thisTurn + extraTurns;
  }

  // WeakHashMap isn't serializable, so we can't use Collections.newSetFromMap(new WeakHashMap<GameEventListener, Boolean>());
  public transient Set<GameEventListener> eventListeners = new HashSet<GameEventListener>();

  public Map<Class<? extends StateTracker>, StateTracker> stateTrackers =
      new HashMap<Class<? extends StateTracker>, StateTracker>();

  public int getActiveCOIndex()
  {
    return getCOIndex(activeArmy);
  }
  public int getCOIndex(Army co)
  {
    for( int i = 0; i < armies.length; ++i )
    {
      if( co == armies[i] )
        return i;
    }
    return -1;
  }

  public void setCursorLocation(XYCoord loc)
  {
    setCursorLocation(loc.x, loc.y);
  }
  public void setCursorLocation(int x, int y)
  {
    if( x < 0 || y < 0 || x > gameMap.mapWidth - 1 || y > gameMap.mapHeight - 1 )
    {
      System.out.println("ERROR! GameInstance.setLocation() was given an out-of-bounds location. Ignoring.");
      return;
    }
    cursorX = x;
    cursorY = y;
  }

  public int getCursorX()
  {
    return cursorX;
  }
  public int getCursorY()
  {
    return cursorY;
  }
  public XYCoord getCursorCoord()
  {
    return new XYCoord(cursorX, cursorY);
  }
  public MapLocation getCursorLocation()
  {
    return gameMap.getLocation(cursorX, cursorY);
  }
  public void moveCursorUp()
  {
    cursorY -= 1;
    if( cursorY < 0 )
      cursorY = 0;
    //		System.out.println("moveCursorUp");
  }
  public void moveCursorDown()
  {
    cursorY += 1;
    if( cursorY >= gameMap.mapHeight )
      cursorY = gameMap.mapHeight - 1;
    //		System.out.println("moveCursorDown");
  }
  public void moveCursorLeft()
  {
    cursorX -= 1;
    if( cursorX < 0 )
      cursorX = 0;
    //		System.out.println("moveCursorLeft");
  }
  public void moveCursorRight()
  {
    cursorX += 1;
    if( cursorX >= gameMap.mapWidth )
      cursorX = gameMap.mapWidth - 1;
    //		System.out.println("moveCursorRight");
  }

  /**
   * Activates the turn for the next available CO, validating the passfile if needed.
   * @param events
   * @return true if the turn changed, false if the auth-check failed.
   */
  public boolean turn(GameEventQueue events)
  {
    // Store the cursor location for the current CO.
    playerCursors.put(activeCoNum, new XYCoord(cursorX, cursorY));
    // Clean up any still-active unit turns
    if( null != activeArmy )
      for( Unit u : activeArmy.getUnits() )
        u.isTurnOver = true;

    int coTurns = 0;
    // Find the next non-defeated CO.
    do
    {
      coTurns++;
      activeCoNum++;
      if( activeCoNum > armies.length - 1 )
      {
        currentDay++;
        activeCoNum = 0;
      }
      activeArmy = armies[activeCoNum];
    } while (activeArmy.isDefeated);

    // If security is enabled, verify this player is cleared to play.
    boolean passCheckOK = !isSecurityEnforced() || PasswordManager.validateAccess(activeArmy);
    if( !passCheckOK )
    {
      // Display "It's not your turn" message.
      boolean hideMap = true;
      events.add(new TurnInitEvent(activeArmy, currentDay, hideMap, "It's not your turn"));
      return false; // auth failed.
    }

    // Set weather conditions based on forecast
    ArrayList<MapChangeEvent.EnvironmentAssignment> weatherChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
    for( int i = 0; i < gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < gameMap.mapHeight; j++ )
      {
        MapLocation loc = gameMap.getLocation(i, j);
        if( loc.forecast.isEmpty() )
        {
          if( loc.getEnvironment().weatherType != defaultWeather )
          {
            weatherChanges.add(new MapChangeEvent.EnvironmentAssignment(loc.getCoordinates(), Environment.getTile(loc.getEnvironment().terrainType, defaultWeather)));
          }
        }
        else
        {
          Weathers weather = loc.getEnvironment().weatherType;
          for( int turns = 0; turns < coTurns; turns++ )
          {
            weather = loc.forecast.poll();
          }
          if( null == weather ) weather = defaultWeather;
          weatherChanges.add(new MapChangeEvent.EnvironmentAssignment(loc.getCoordinates(), Environment.getTile(loc.getEnvironment().terrainType, weather)));
        }
      }
    }

    events.add(new TurnInitEvent(activeArmy, currentDay, isFogEnabled() || isSecurityEnabled));

    if( !weatherChanges.isEmpty() )
    {
      events.add(new MapChangeEvent(weatherChanges));
    }

    // Set the cursor to the new CO's last known cursor position.
    setCursorLocation(playerCursors.get(activeCoNum).x, playerCursors.get(activeCoNum).y);

    // Handle income and any other scenario-specific events.
    events.addAll(gameScenario.initTurn(gameMap));

    // Handle any CO-specific turn events.
    events.addAll(activeArmy.initTurn(gameMap));

    for( Army army : armies )
    {
      army.myView.resetFog();
    }

    // Initialize the next turn, recording any events that will occur.
    return true; // Turn init successful.
  }

  /** Return the current turn number. */
  public int getCurrentTurn()
  {
    return currentDay;
  }

  /**
   * Does any needed cleanup at the end of the game
   */
  public void endGame()
  {
    for( Army army : armies )
    {
      army.deInitForGame(this);
    }
  }

  /**
   * Gives you a random number between 0 (inclusive) and the input (exclusive)
   * <p>Call this in GameAction.getEvents()/event constructors and nowhere else
   */
  public int getRN(int upperBound)
  {
    upperBound = Math.abs(upperBound);
    ++rngNumbersGenerated;
    return rng.nextInt(upperBound);
  }

  /**
   * Just concatenates the names of all the COs involved
   * TODO: get fancy and actually split the teams out
   */
  private String getSaveName()
  {
    StringBuilder sb = new StringBuilder();
    for( Army army : armies )
      for( Commander co : army.cos )
      {
        sb.append(co.coInfo.name).append("_");
      }
    sb.setLength(sb.length()-1);
    sb.append(".svp"); // "svp" for "SaVe Peace"
    return sb.toString();
  }
  
  public static String getSaveWarnings(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    GameVersion verInfo;
    StringBuilder prepends = new StringBuilder();

    // Check that the save file has a matching version.
    verInfo = (GameVersion) in.readObject();
    if( new GameVersion().isEqual(verInfo) )
    {
      // If so, make sure we aren't trying to take someone else's turn.
      GameInstance gi = (GameInstance) in.readObject();
      if( !gi.turn(new GameEventQueue()) )
      {
        prepends.append('~');
        System.out.println(String.format(
            String.format("Save is for another player's turn (%s).",
                (null != gi.activeArmy) ? gi.getActiveCOIndex() : "null")));
      }
    }
    else
    {
      prepends.append('!');
      System.out.println(String.format("Save is incompatible version: %s",
          (null == verInfo) ? "unknown" : verInfo.toString()));
    }

    return prepends.toString();
  }

  public void writeSave(ObjectOutputStream out, boolean endCurrentTurn) throws IOException
  {
    boolean temp = currentTurnEnded;
    currentTurnEnded = endCurrentTurn;
    // Method for serialization of object
    out.writeObject(new GameVersion());
    out.writeObject(this);
    currentTurnEnded = temp;
  }

  /**
   * Same signature as in Serializable interface
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    stream.defaultWriteObject();

    // save any serializable listeners
    Set<GameEventListener> saveableListeners = new HashSet<GameEventListener>();
    for( GameEventListener listener : eventListeners)
    {
      if( listener.shouldSerialize() )
        saveableListeners.add(listener);
    }
    stream.writeObject(saveableListeners);
  }

  /**
   * Same signature as in Serializable interface
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();

    rng = new SplittableRandom(rngSeed);
    for( int i = 0; i < rngNumbersGenerated; ++i )
    {
      rng.nextInt();
    }

    // restore any serializable listeners
    eventListeners = (Set<GameEventListener>) stream.readObject();
  }

  public boolean isSecurityEnforced()
  {
    // Little reason to secure on Day 1; folks often have one player build
    // infantry for everyone for the first round, so we'll create passwords
    // after the second turn and enforce them thereafter.
    return currentDay > 1 && isSecurityEnabled && !activeArmy.isAI();
  }

  public boolean requireInitOnLoad()
  {
    return currentTurnEnded;
  }
}
