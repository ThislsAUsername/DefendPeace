package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class MoveType {
	protected int[][] moveCosts; // format is [weather][terrain]

	public int getMoveCost(Weathers weather, Terrains terrain){
		return moveCosts[weather.ordinal()][terrain.ordinal()];
	}
}
