package Engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import CommandingOfficers.Commander;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.TurnInitEvent;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.MapWindow;

public class GameInstance implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public String saveFile;
  public Terrain.MapMaster gameMap;
  public Commander[] commanders;
  private int activeCoNum;
  public Commander activeCO = null;
  private int cursorX = 0;
  private int cursorY = 0;

  HashMap<Integer, XYCoord> playerCursors = null;

  private Weathers defaultWeather;

  private GameScenario gameScenario;
  boolean isSecurityEnabled;

  private int currentTurn;
  private boolean currentTurnEnded = true; // Set to false when saving a game mid-turn.

  public GameInstance(MapMaster map)
  {
    this(map, Weathers.CLEAR, new GameScenario(), false);
  }

  public GameInstance(MapMaster map, Weathers weather, GameScenario scenario, boolean useSecurity)
  {
    if( map.commanders.length < 2 )
    {
      System.out.println("WARNING! Creating a game with fewer than two commanders.");
    }
    gameScenario = scenario;
    isSecurityEnabled = useSecurity;

    currentTurn = 1;

    gameMap = map;
    defaultWeather = weather;

    commanders = map.commanders;
    activeCoNum = -1; // No commander is active yet.

    // Set the initial cursor locations for each player.
    playerCursors = new HashMap<Integer, XYCoord>();
    for( int i = 0; i < commanders.length; ++i )
    {
      commanders[i].money = gameScenario.rules.startingFunds;
      if( commanders[i].HQLocation != null )
      {
        commanders[i].myView = new MapWindow(map, commanders[i]);
        commanders[i].myView.resetFog();
        playerCursors.put(i, commanders[i].HQLocation);
      }
      else
      {
        System.out.println("Warning! Commander " + commanders[i].coInfo.name + " does not have an HQ location!");
        playerCursors.put(i, new XYCoord(1, 1));
      }
      GameEventListener.registerEventListener(commanders[i], this);
    }
    
    saveFile = getSaveName();
  }

  public boolean isFogEnabled()
  {
    return gameScenario.rules.isFogEnabled;
  }

  // WeakHashMap isn't serializable, so we can't use Collections.newSetFromMap(new WeakHashMap<GameEventListener, Boolean>());
  public transient Set<GameEventListener> eventListeners = new HashSet<GameEventListener>();

  public int getActiveCOIndex()
  {
    return getCOIndex(activeCO);
  }
  public int getCOIndex(Commander co)
  {
    for( int i = 0; i < commanders.length; ++i )
    {
      if( co == commanders[i] )
        return i;
    }
    return -1;
  }

  public void setCursorLocation(XYCoord loc)
  {
    setCursorLocation(loc.xCoord, loc.yCoord);
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
  public Location getCursorLocation()
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
    int coTurns = 0;

    if( null != activeCO) activeCO.endTurn();

    // Find the next non-defeated CO.
    do
    {
      coTurns++;
      activeCoNum++;
      if( activeCoNum > commanders.length - 1 )
      {
        currentTurn++;
        activeCoNum = 0;
      }
      activeCO = commanders[activeCoNum];
    } while (activeCO.isDefeated);

    // If security is enabled, verify this player is cleared to play.
    boolean passCheckOK = !isSecurityEnforced() || PasswordManager.validateAccess(activeCO);
    if( !passCheckOK )
    {
      // Display "It's not your turn" message.
      boolean hideMap = true;
      events.add(new TurnInitEvent(activeCO, currentTurn, hideMap, "It's not your turn"));
      return false; // auth failed.
    }

    // Set weather conditions based on forecast
    ArrayList<MapChangeEvent.EnvironmentAssignment> weatherChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
    for( int i = 0; i < gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < gameMap.mapHeight; j++ )
      {
        Location loc = gameMap.getLocation(i, j);
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

    events.add(new TurnInitEvent(activeCO, currentTurn, isFogEnabled() || isSecurityEnabled));

    if( !weatherChanges.isEmpty() )
    {
      events.add(new MapChangeEvent(weatherChanges));
    }

    // Set the cursor to the new CO's last known cursor position.
    setCursorLocation(playerCursors.get(activeCoNum).xCoord, playerCursors.get(activeCoNum).yCoord);

    // Handle income and any other scenario-specific events.
    events.addAll(gameScenario.initTurn(gameMap));

    // Handle any CO-specific turn events.
    events.addAll(activeCO.initTurn(gameMap));
    
    // Initialize the next turn, recording any events that will occur.
    return true; // Turn init successful.
  }

  /** Return the current turn number. */
  public int getCurrentTurn()
  {
    return currentTurn;
  }

  /**
   * Does any needed cleanup at the end of the game
   */
  public void endGame()
  {
    for( Commander co : commanders )
    {
      GameEventListener.unregisterEventListener(co, this);
    }
  }
  
  /**
   * Just concatenates the names of all the COs involved
   * TODO: get fancy and actually split the teams out
   */
  private String getSaveName()
  {
    StringBuilder sb = new StringBuilder();
    for( Commander co : commanders )
    {
      sb.append(co.coInfo.name).append("_");
    }
    sb.setLength(sb.length()-1);
    sb.append(".svp"); // "svp" for "SaVe Peace"
    return sb.toString();
  }
  
  public static String getSaveWarnings(String filename)
  {
    System.out.println(String.format("Checking compatibility of save %s", filename));

    StringBuilder prepends = new StringBuilder();
    GameVersion verInfo = null;
    try (FileInputStream file = new FileInputStream(filename); ObjectInputStream in = new ObjectInputStream(file);)
    {
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
                  (null != gi.activeCO) ? gi.activeCO.coInfo.name : "null")));
        }
      }
      else
      {
        prepends.append('!');
        System.out.println(String.format("Save is incompatible version: %s",
            (null == verInfo) ? "unknown" : verInfo.toString()));
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return prepends.toString();
  }
  
  public static GameInstance loadSave(String filename)
  {
    System.out.println(String.format("Deserializing game data from %s", filename));
    
    GameInstance load = null;
    try (FileInputStream file = new FileInputStream(filename); ObjectInputStream in = new ObjectInputStream(file);)
    {
      in.readObject(); // Pull out and discard our version info

      load = (GameInstance) in.readObject();
    }
    catch (Exception ex)
    {
      System.out.println(ex.toString());
    }

    return load;
  }
  
  public String writeSave(boolean endCurrentTurn)
  {
    currentTurnEnded = endCurrentTurn;
    String filename = "save/" + saveFile; // "svp" for "SaVe Peace"
    new File("save/").mkdirs(); // make sure we don't freak out if the directory's not there

    System.out.println(String.format("Now saving to %s", filename));
    try (FileOutputStream file = new FileOutputStream(filename); ObjectOutputStream out = new ObjectOutputStream(file);)
    {
      // Method for serialization of object
      out.writeObject(new GameVersion());
      out.writeObject(this);
    }
    catch (IOException ex)
    {
      System.out.println(ex.toString());
    }

    return filename;
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

    // restore any serializable listeners
    eventListeners = (Set<GameEventListener>) stream.readObject();
  }

  public boolean isSecurityEnforced()
  {
    // Little reason to secure at turn 0; folks often have one player build
    // infantry for everyone for the first round, so we'll create passwords
    // after the second turn and enforce them thereafter.
    return currentTurn > 1 && isSecurityEnabled && !activeCO.isAI();
  }

  public boolean requireInitOnLoad()
  {
    return currentTurnEnded;
  }
}
