package Units;

import Terrain.Tile;

public class UnitModel {
	public String name;
	public double maxHP;
	public double atkStr;
	
	public int idleFuelBurn;
	public int movePower;
	public int[][] moveCosts; // this should be of dimensions [weathers][terrains] or [terrains][weathers]
								// Since COs generally care about weather and not terrain, [weathers][terrains] makes more sense at the moment.
}
