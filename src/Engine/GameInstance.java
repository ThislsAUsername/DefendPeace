package Engine;

import Terrain.GameMap;

public class GameInstance {
	public Terrain.GameMap gameMap;
	public CommandingOfficers.Commander[] commanders;
	public int activeCO = 0;

	public GameInstance(GameMap map, CommandingOfficers.Commander[] cos)
	{
		if(cos.length < 2)
		{
			System.out.println("WARNING! Creating a game with fewer than two commanders.");
		}

		gameMap = map;
		commanders = cos;
	}
}
