package UI.Art.SpriteArtist;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import Terrain.Environment;
import UI.MapView;

/**
 * Responsible for loading all game images from disk. All methods are static, and resources are loaded the first time they are needed.
 */
public class SpriteLibrary
{
	public static final int baseSpriteSize = 16; // TODO: no reason to define this here and in MapView (as tileSizePx).
	public static int drawScale = MapView.getTileSize() / baseSpriteSize; // TODO: Should artists be initialized with this?
	
	// TODO: Account for weather?
	private static HashMap<Environment.Terrains, TerrainSpriteSet> spriteSetMap = new HashMap<Environment.Terrains, TerrainSpriteSet>();
	
	public static TerrainSpriteSet getTerrainSpriteSet(Environment.Terrains terrain)
	{
		if(!spriteSetMap.containsKey(terrain))
		{
			// Go load it.
			loadTerrainSpriteSet(terrain);
		}
		// Now we must have an entry for that terrain type.
		return spriteSetMap.get(terrain);
	}
	
	/**
	 * Loads the images and builds the TerrainSpriteSet for the terrain type passed in.
	 * If we are unable to load the correct images for any reason, make a blank TerrainSpriteSet.
	 * @param terrainType
	 */
	private static void loadTerrainSpriteSet(Environment.Terrains terrainType)
	{
		String spriteFile = "res/tileset/";
		TerrainSpriteSet ss = null;
		int w = baseSpriteSize;
		int h = baseSpriteSize;
		switch(terrainType)
		{
		case CITY:
			spriteFile += "city_clear.png";
			w = baseSpriteSize*2;
			h = baseSpriteSize*2;
			break;
		case DUNES:
			break;
		case FACTORY:
			spriteFile += "factory_clear.png";
			w = baseSpriteSize*2;
			h = baseSpriteSize*2;
			break;
		case FOREST:
			spriteFile += "forest_clear.png";
			w = baseSpriteSize*2;
			h = baseSpriteSize*2;
			break;
		case GRASS:
			spriteFile += "grass_clear.png";
			break;
		case HQ:
			break;
		case MOUNTAIN:
			spriteFile += "mountain_clear.png";
			w = baseSpriteSize*2;
			h = baseSpriteSize*2;
			break;
		case OCEAN:
			spriteFile += "sea_clear.png";
			break;
		case REEF:
			spriteFile += "reef_clear.png";
			break;
		case ROAD:
			spriteFile += "road_clear.png";
			break;
		case SHOAL:
			spriteFile += "shoal_clear.png";
			break;
			default:
			System.out.println("ERROR! [SpriteLibrary.loadTerrainSpriteSet] Unknown terrain type " + terrainType);
		}
		
		try
		{
			System.out.println("INFO: Loading terrain sprites for " + terrainType);
			ss = new TerrainSpriteSet(terrainType, ImageIO.read(new File(spriteFile)), w, h);
		}
		catch( IOException ioe )
		{
			System.out.println("WARNING! Exception while loading resource file '" + spriteFile + "'");
			//ioe.printStackTrace();
			ss = null; // Just in case it partially initialized or something.
		}
		
		if(ss == null)
		{
			// Somehow we failed to initialize the sprite set. Create one with default settings
			System.out.println("WARNING! Continuing with placeholders.");
			ss = new TerrainSpriteSet(terrainType, null, w, h);
		}
		
		spriteSetMap.put(terrainType, ss);
	}
}
