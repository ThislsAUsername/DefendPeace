package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.image.BufferedImage;
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
	
	private static Color[] defaultMapColors = {
		new Color(110, 110, 110),
		new Color(160, 160, 160),
		new Color(200, 200, 200),
		new Color(245, 245, 245),
		new Color(70, 70, 70) };
	private static Color[] pinkMapBuildingColors = {
		new Color(190, 90, 90),
		new Color(240, 140, 140),
		new Color(250, 190, 190),
		new Color(255, 245, 245),
		new Color(255, 219, 74)	};
	private static Color[] cyanMapBuildingColors = {
		new Color(77, 157, 157),
		new Color(130, 200, 200),
		new Color(200, 230, 230),
		new Color(245, 255, 255),
		new Color(255, 219, 74) };

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
			// Don't have it? Create it.
			createTerrainSpriteSet(spriteKey);
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
			// Don't have it? Create it.
			createTerrainSpriteSet(spriteKey);
		}
		// Now we must have an entry for that terrain type.
		return spriteSetMap.get(spriteKey);
	}
	
	/**
	 * Loads the images and builds the TerrainSpriteSet for the terrain type passed in.
	 * If we are unable to load the correct images for any reason, make a blank TerrainSpriteSet.
	 * @param terrainType
	 */
	private static void createTerrainSpriteSet(SpriteSetKey spriteKey)
	{
		Environment.Terrains terrainType = spriteKey.terrainKey;

		TerrainSpriteSet ss = null;
		int w = baseSpriteSize;
		int h = baseSpriteSize;
		switch(terrainType)
		{
		case CITY:
			ss = buildTerrainSpriteSet("res/tileset/city_clear.png", spriteKey, w*2, h*2);
			break;
		case DUNES:
			break;
		case FACTORY:
			ss = buildTerrainSpriteSet("res/tileset/factory_clear.png", spriteKey, w*2, h*2);
			break;
		case FOREST:
			ss = buildTerrainSpriteSet("res/tileset/forest_clear.png", spriteKey, w*2, h*2);
			break;
		case GRASS:
			ss = buildTerrainSpriteSet("res/tileset/grass_clear.png", spriteKey, w, h);
			break;
		case HQ:
			ss = buildTerrainSpriteSet("res/tileset/hq_clear.png", spriteKey, w*2, h*2);
			break;
		case MOUNTAIN:
			ss = buildTerrainSpriteSet("res/tileset/mountain_clear.png", spriteKey, w*2, h*2);
			break;
		case SEA:
			ss = buildTerrainSpriteSet("res/tileset/sea_clear.png", spriteKey, w, h);
			ss.addTileTransition(Environment.Terrains.GRASS, loadSpriteSheetFile("res/tileset/sea_grass_clear.png"), w, h);
			break;
		case REEF:
			ss = buildTerrainSpriteSet("res/tileset/reef_clear.png", spriteKey, w, h);
			ss.addTileTransition(Environment.Terrains.SEA, loadSpriteSheetFile("res/tileset/reef_clear.png"), w, h);
			break;
		case ROAD:
			ss = buildTerrainSpriteSet("res/tileset/road_clear.png", spriteKey, w, h);
			break;
		case SHOAL:
			ss = buildTerrainSpriteSet("res/tileset/shoal_clear.png", spriteKey, w, h);
			ss.addTileTransition(Environment.Terrains.SEA, loadSpriteSheetFile("res/tileset/shoal_sea_clear.png"), w, h);
			ss.addTileTransition(Environment.Terrains.GRASS, loadSpriteSheetFile("res/tileset/shoal_grass_clear.png"), w, h);
			break;
			default:
			System.out.println("ERROR! [SpriteLibrary.loadTerrainSpriteSet] Unknown terrain type " + terrainType);
		}

		spriteSetMap.put(spriteKey, ss);
	}

	private static TerrainSpriteSet buildTerrainSpriteSet(String spriteFile, SpriteSetKey spriteKey, int w, int h)
	{
		TerrainSpriteSet ss = null;

		System.out.println("INFO: Loading terrain sprites for " + spriteKey.terrainKey);
		ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile(spriteFile), w, h);

		if(spriteKey.commanderKey != null)
		{
			ss.colorize(defaultMapColors, getColorPalette(spriteKey.commanderKey.myColor).mapBuildingColors);
		}

		return ss;
	}

	private static BufferedImage loadSpriteSheetFile(String filename)
	{
		BufferedImage bi = null;
		try
		{
			bi = ImageIO.read(new File(filename));
		}
		catch(IOException ioex)
		{
			System.out.println("WARNING! Exception loading resource file " + filename);
			bi = null;
		}
		return bi;
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
