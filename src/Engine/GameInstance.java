package Engine;

import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.MapWindow;

public class GameInstance
{
  public Terrain.MapMaster gameMap;
  public Commander[] commanders;
  private int activeCoNum;
  public Commander activeCO = null;
  private int cursorX = 0;
  private int cursorY = 0;

  HashMap<Integer, XYCoord> playerCursors = null;

  private boolean isFogEnabled;

  public GameInstance(MapMaster map)
  {
    if( map.commanders.length < 2 )
    {
      System.out.println("WARNING! Creating a game with fewer than two commanders.");
    }

    gameMap = map;
    commanders = map.commanders;
    activeCoNum = -1; // No commander is active yet.

    // Set the initial cursor locations for each player.
    playerCursors = new HashMap<Integer, XYCoord>();
    for( int i = 0; i < commanders.length; ++i )
    {
      if( commanders[i].HQLocation != null )
      {
        commanders[i].myView = new MapWindow(map, commanders[i], isFogEnabled);
        commanders[i].myView.resetFog();
        playerCursors.put(i, commanders[i].HQLocation);
      }
      else
      {
        System.out.println("Warning! Commander " + commanders[i].coInfo.name + " does not have an HQ location!");
        playerCursors.put(i, new XYCoord(1, 1));
      }
      GameEventListener.registerEventListener(commanders[i]);
    }
  }

  public boolean isFogEnabled()
  {
    return isFogEnabled;
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
   * Activates the turn for the next available CO.
   * @param events
   */
  public GameEventQueue turn()
  {
    GameEventQueue events = new GameEventQueue();
    
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
        activeCoNum = 0;
      }
      activeCO = commanders[activeCoNum];
    } while (activeCO.isDefeated);

    // Set weather conditions based on forecast
    ArrayList<MapChangeEvent.EnvironmentAssignment> weatherChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
    for( int i = 0; i < gameMap.mapWidth; i++ )
    {
      for( int j = 0; j < gameMap.mapHeight; j++ )
      {
        Location loc = gameMap.getLocation(i, j);
        if( loc.forecast.isEmpty() )
        {
          if( loc.getEnvironment().weatherType != Weathers.CLEAR )
          {
            weatherChanges.add(new MapChangeEvent.EnvironmentAssignment(loc.getCoordinates(), Environment.getTile(loc.getEnvironment().terrainType, Weathers.CLEAR)));
          }
        }
        else
        {
          Weathers weather = loc.getEnvironment().weatherType;
          for( int turns = 0; turns < coTurns; turns++ )
          {
            weather = loc.forecast.poll();
          }
          if( null == weather ) weather = Weathers.CLEAR;
          weatherChanges.add(new MapChangeEvent.EnvironmentAssignment(loc.getCoordinates(), Environment.getTile(loc.getEnvironment().terrainType, weather)));
        }
      }
    }
    if( !weatherChanges.isEmpty() )
    {
      events.add(new MapChangeEvent(weatherChanges));
    }

    // Set the cursor to the new CO's last known cursor position.
    setCursorLocation(playerCursors.get(activeCoNum).xCoord, playerCursors.get(activeCoNum).yCoord);
    
    events.addAll(activeCO.initTurn(gameMap));
    
    // Initialize the next turn, recording any events that will occur.
    return events;
  }

  /**
   * Does any needed cleanup at the end of the game
   */
  public void endGame()
  {
    for( Commander co : commanders )
    {
      GameEventListener.unregisterEventListener(co);
    }
  }
}
