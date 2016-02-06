package UI.Art.SpriteArtist;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import CommandingOfficers.Commander;
import Terrain.Environment;
import Terrain.Location;
import UI.MapView;

/**
 * Responsible for loading all game images from disk. All methods are static, and resources are loaded the first time they are needed.
 */
public class SpriteLibrary
{
	public static final int baseSpriteSize = 16; // TODO: no reason to define this here and in MapView (as tileSizePx).
	public static int drawScale = MapView.getTileSize() / baseSpriteSize; // TODO: Should artists be initialized with this?
	
	private static Color[] defaultMapColors = { new Color(245, 245, 245),
		new Color(200, 200, 200), new Color(160, 160, 160),
		new Color(110, 110, 110), new Color(70, 70, 70)};
	private static Color[] pinkMapBuildingColors = { new Color(255, 245, 245),
		new Color(255, 210, 210), new Color(240, 160, 160),
		new Color(155, 70, 70), new Color(255, 219, 74)	};
	private static Color[] cyanMapBuildingColors = { new Color(237, 255, 255),
		new Color(183, 255, 255), new Color(130, 216, 216),
		new Color(77, 157, 157), new Color(255, 219, 74) };

	private static HashMap<Color, ColorPalette> colorPalettes = new HashMap<Color, ColorPalette>() {
		private static final long serialVersionUID = 1L;
	{
		// Create a mapping of game colors to the fine-tuned colors that will be used for map sprites.
		put(Color.PINK, new ColorPalette(pinkMapBuildingColors));
		put(Color.CYAN, new ColorPalette(cyanMapBuildingColors));
	}};

	// TODO: Account for weather?
	private static HashMap<SpriteSetKey, TerrainSpriteSet> spriteSetMap = new HashMap<SpriteSetKey, TerrainSpriteSet>();

	/**
	 * Retrieve (loading if needed) the sprites associated with the given terrain type. For ownable terrain types
	 * (e.g. cities), the unowned variant of the sprite will be returned.
	 */
	public static TerrainSpriteSet getTerrainSpriteSet(Environment.Terrains terrain)
	{
		SpriteSetKey spriteKey = SpriteSetKey.instance(terrain, null);
		if(!spriteSetMap.containsKey(spriteKey))
		{
			// Go load it.
			loadTerrainSpriteSet(spriteKey);
		}
		// Now we must have an entry for that terrain type.
		return spriteSetMap.get(spriteKey);
	}

	/**
	 * Retrieve (loading if needed) the sprites associated with the terrain type at the specified location.
	 * This function returns the owner-appropriate version of the tile for ownable terrain types.
	 */
	public static TerrainSpriteSet getTerrainSpriteSet(Location loc)
	{
		SpriteSetKey spriteKey = SpriteSetKey.instance(loc.getEnvironment().terrainType, loc.getOwner());
		if(!spriteSetMap.containsKey(spriteKey))
		{
			// Go load it.
			loadTerrainSpriteSet(spriteKey);
		}
		// Now we must have an entry for that terrain type.
		return spriteSetMap.get(spriteKey);
	}
	
	/**
	 * Loads the images and builds the TerrainSpriteSet for the terrain type passed in.
	 * If we are unable to load the correct images for any reason, make a blank TerrainSpriteSet.
	 * @param terrainType
	 */
	private static void loadTerrainSpriteSet(SpriteSetKey spriteKey)
	{
		Environment.Terrains terrainType = spriteKey.terrainKey;

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

			if(spriteKey.commanderKey != null)
			{
				ss.colorize(defaultMapColors, getColorPalette(spriteKey.commanderKey.myColor).mapBuildingColors);
			}
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
		
		spriteSetMap.put(spriteKey, ss);
	}

	private static ColorPalette getColorPalette(Color colorKey)
	{
		return colorPalettes.get(colorKey);
	}

	private static class SpriteSetKey
	{
		public final Environment.Terrains terrainKey;
		public final Commander commanderKey;
		private static ArrayList<SpriteSetKey> instances = new ArrayList<SpriteSetKey>();

		private SpriteSetKey(Environment.Terrains terrain, Commander co)
		{
			terrainKey = terrain;
			commanderKey = co;
		}

		public static SpriteSetKey instance(Environment.Terrains terrain, Commander co)
		{
			SpriteSetKey key = null;
			for(int i = 0; i < instances.size(); ++i)
			{
				if(instances.get(i).terrainKey == terrain && instances.get(i).commanderKey == co)
				{
					key = instances.get(i);
					break;
				}
			}
			if(key == null)
			{
				key = new SpriteSetKey(terrain, co);
				instances.add(key);
			}
			return key;
		}
	}
}
