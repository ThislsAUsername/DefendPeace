package Terrain;

public class Tile {
	public enum Terrains{PLAIN, FOREST, ROAD, CITY, FACTORY, SHOAL, WATER, REEF};
	public enum Weathers{CLEAR, RAIN, SNOW, SANDSTORM};
	
	public Terrains terrainType;
	public Weathers weatherType;
}
