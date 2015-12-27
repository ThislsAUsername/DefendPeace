package Engine;

import Terrain.GameMap;
import UI.GameMenu;

public class GameInstance {
	public Terrain.GameMap gameMap;
	public CommandingOfficers.Commander[] commanders;
	public CommandingOfficers.Commander activeCO = null;
	private int cursorX = 0;
	private int cursorY = 0;
	
	// TODO this is a workaround to allow MapView to access MapController's menu. Probably should be better-implemented.
	public GameMenu currentMenu;

	public GameInstance(GameMap map, CommandingOfficers.Commander[] cos)
	{
		if(cos.length < 2)
		{
			System.out.println("WARNING! Creating a game with fewer than two commanders.");
		}

		gameMap = map;
		commanders = cos;
		activeCO = commanders[0];
	}
	
	public int getCursorX()
	{
		return cursorX;
	}
	public int getCursorY()
	{
		return cursorY;
	}
	public void moveCursorUp()
	{
		cursorY -= 1;
		if(cursorY < 0) cursorY = 0;
	}
	public void moveCursorDown()
	{
		cursorY +=1;
		if(cursorY >= gameMap.mapHeight) cursorY = gameMap.mapHeight - 1;
	}
	public void moveCursorLeft()
	{
		cursorX -= 1;
		if(cursorX < 0) cursorX = 0;
	}
	public void moveCursorRight()
	{
		cursorX += 1;
		if(cursorX >= gameMap.mapWidth) cursorX = gameMap.mapWidth - 1;
	}
}
