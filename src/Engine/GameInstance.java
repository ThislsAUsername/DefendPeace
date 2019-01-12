package Engine;

import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.GameEvents.GameEventListener;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.MapWindow;

public class GameInstance
{
  public Terrain.MapMaster gameMap;
  public GameMap foggedMap;
  public Commander[] commanders;
  private int activeCoNum;
  public Commander activeCO = null;
  private int cursorX = 0;
  private int cursorY = 0;

  HashMap<Integer, XYCoord> playerCursors = null;

  private boolean isFogEnabled;

  public GameInstance(MapMaster map, Commander[] cos)
  {
    if( cos.length < 2 )
    {
      System.out.println("WARNING! Creating a game with fewer than two commanders.");
    }

    gameMap = map;
    foggedMap = new MapWindow(map, null, isFogEnabled);
    foggedMap.resetFog();
    commanders = cos;
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
  public void turn()
  {
    // Store the cursor location for the current CO.
    playerCursors.put(activeCoNum, new XYCoord(cursorX, cursorY));

    if( null != activeCO) activeCO.endTurn();

    // Find the next non-defeated CO.
    do
    {
      activeCoNum++;
      if( activeCoNum > commanders.length - 1 )
      {
        activeCoNum = 0;
      }
      activeCO = commanders[activeCoNum];
    } while (activeCO.isDefeated);

    // Set the cursor to the new CO's last known cursor position.
    setCursorLocation(playerCursors.get(activeCoNum).xCoord, playerCursors.get(activeCoNum).yCoord);

    // Initialize the next turn, recording any events that will occur.
    activeCO.initTurn(gameMap);
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
