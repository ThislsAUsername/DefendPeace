package Engine;

import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.Location;

public class GameInstance
{
  public Terrain.GameMap gameMap;
  public CommandingOfficers.Commander[] commanders;
  private int nextActiveCoNum;
  public CommandingOfficers.Commander activeCO = null;
  private int cursorX = 0;
  private int cursorY = 0;

  public GameInstance(GameMap map, CommandingOfficers.Commander[] cos)
  {
    if( cos.length < 2 )
    {
      System.out.println("WARNING! Creating a game with fewer than two commanders.");
    }

    gameMap = map;
    commanders = cos;
    nextActiveCoNum = 0;
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

  public void turn(GameEventQueue events)
  {
    // Find the next non-defeated CO.
    do
    {
      activeCO = commanders[nextActiveCoNum];
      nextActiveCoNum++;
      if( nextActiveCoNum > commanders.length - 1 )
      {
        nextActiveCoNum = 0;
      }
    } while( activeCO.isDefeated );

    // TODO: Add a turn-init event at the front.

    // Initialize the next turn, recording any events that will occur.
    activeCO.initTurn(gameMap, events);
  }
}
