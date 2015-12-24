package Units.MoveTypes;

import Terrain.Tile.Terrains;
import Terrain.Tile.Weathers;

public class MoveType {
	public int[][] moveCosts; // format is [weather][terrain]
	
	public int getMoveCost(Weathers weather, Terrains terrain){
		return moveCosts[weather.ordinal()][terrain.ordinal()];
	}
}
