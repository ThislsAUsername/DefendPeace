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
import Units.Unit;
import Units.UnitModel;

/**
 * Responsible for loading all game images from disk. All methods are static, and resources are loaded the first time they are needed.
 */
public class SpriteLibrary
{
	// This is the physical size of a single map square in pixels.
	public static final int baseSpriteSize = 16;
	
	public static final Color[] defaultMapColors = {
		new Color(70, 70, 70),
		new Color(110, 110, 110),
		new Color(160, 160, 160),
		new Color(200, 200, 200),
		new Color(245, 245, 245) };
	private static Color[] pinkMapBuildingColors = {
		new Color(255, 219, 74),
		new Color(190, 90, 90),
		new Color(240, 140, 140),
		new Color(250, 190, 190),
		new Color(255, 245, 245)};
	private static Color[] cyanMapBuildingColors = {
		new Color(255, 219, 74),
		new Color(77, 157, 157),
		new Color(130, 200, 200),
		new Color(200, 230, 230),
		new Color(245, 255, 255) };
	private static Color[] pinkMapUnitColors = {
		new Color(177, 62, 62),
		new Color(255, 100, 100),
		new Color(255, 136, 136),
		new Color(255, 175, 175),
		new Color(255, 230, 230) };
	private static Color[] cyanMapUnitColors = {
		new Color(0, 105, 105),
		new Color(0, 170, 170),
		new Color(0, 215, 215),
		new Color(0, 255, 255),
		new Color(195, 255, 255), };

	private static HashMap<Color, ColorPalette> buildingColorPalettes = new HashMap<Color, ColorPalette>() {
		private static final long serialVersionUID = 1L;
	{
		// Create a mapping of game colors to the fine-tuned colors that will be used for map sprites.
		put(Color.PINK, new ColorPalette(pinkMapBuildingColors));
		put(Color.CYAN, new ColorPalette(cyanMapBuildingColors));
	}};
	private static HashMap<Color, ColorPalette> mapUnitColorPalettes = new HashMap<Color, ColorPalette>() {
		private static final long serialVersionUID = 1L;
	{
		// Create a mapping of game colors to the fine-tuned colors that will be used for map sprites.
		put(Color.PINK, new ColorPalette(pinkMapUnitColors));
		put(Color.CYAN, new ColorPalette(cyanMapUnitColors));
	}};

	// TODO: Account for weather?
	private static HashMap<SpriteSetKey, TerrainSpriteSet> spriteSetMap = new HashMap<SpriteSetKey, TerrainSpriteSet>();
	// TODO: Consider templatizing the key types, and then combining these two maps.
	private static HashMap<UnitSpriteSetKey, UnitSpriteSet> unitMapSpriteSetMap = new HashMap<UnitSpriteSetKey, UnitSpriteSet>();

	// Sprites to hold the images for drawing tentative moves on the map.
	private static Sprite moveCursorLineSprite = null;
	private static Sprite moveCursorArrowSprite = null;
	
	// HP numbers to overlay on map units when damaged.
	private static Sprite mapUnitHPSprites = null;

	// Letters for writing in menus.
	private static Sprite menuLetterSprites = null;
	private static Sprite menuNumberSprites = null;

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
		System.out.println("INFO: Loading terrain sprites for " + terrainType);

		TerrainSpriteSet ss = null;
		int w = baseSpriteSize;
		int h = baseSpriteSize;
		switch(terrainType)
		{
		case CITY:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/city_clear.png"), w*2, h*2);
			break;
		case DUNES:
			break;
		case FACTORY:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/factory_clear.png"), w*2, h*2);
			break;
		case FOREST:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/forest_clear.png"), w*2, h*2);
			break;
		case GRASS:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/grass_clear.png"), w, h);
			break;
		case HQ:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/hq_clear.png"), w*2, h*2);
			break;
		case MOUNTAIN:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/mountain_clear.png"), w*2, h*2);
			break;
		case SEA:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/sea_clear.png"), w, h);
			ss.addTileTransition(Environment.Terrains.GRASS, loadSpriteSheetFile("res/tileset/sea_grass_clear.png"), w, h);
			break;
		case REEF:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/reef_clear.png"), w, h);
			ss.addTileTransition(Environment.Terrains.SEA, loadSpriteSheetFile("res/tileset/reef_clear.png"), w, h);
			break;
		case ROAD:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/road_clear.png"), w, h);
			break;
		case SHOAL:
			ss = new TerrainSpriteSet(spriteKey.terrainKey, loadSpriteSheetFile("res/tileset/shoal_clear.png"), w, h);
			ss.addTileTransition(Environment.Terrains.SEA, loadSpriteSheetFile("res/tileset/shoal_sea_clear.png"), w, h);
			ss.addTileTransition(Environment.Terrains.GRASS, loadSpriteSheetFile("res/tileset/shoal_grass_clear.png"), w, h);
			break;
			default:
			System.out.println("ERROR! [SpriteLibrary.loadTerrainSpriteSet] Unknown terrain type " + terrainType);
		}

		if(spriteKey.commanderKey != null)
		{
			ss.colorize(defaultMapColors, getBuildingColors(spriteKey.commanderKey.myColor).paletteColors);
		}
		spriteSetMap.put(spriteKey, ss);
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

	public static Sprite getMoveCursorLineSprite()
	{
		if( moveCursorLineSprite == null )
		{
			moveCursorLineSprite = new Sprite(loadSpriteSheetFile("res/tileset/moveCursorLine.png"), baseSpriteSize, baseSpriteSize);
		}

		return moveCursorLineSprite;
	}

	public static Sprite getMoveCursorArrowSprite()
	{
		if( moveCursorArrowSprite == null )
		{
			moveCursorArrowSprite = new Sprite(loadSpriteSheetFile("res/tileset/moveCursorArrow.png"), baseSpriteSize, baseSpriteSize);
		}

		return moveCursorArrowSprite;
	}

	private static ColorPalette getBuildingColors(Color colorKey)
	{
		return buildingColorPalettes.get(colorKey);
	}

	private static ColorPalette getMapUnitColors(Color colorKey)
	{
		return mapUnitColorPalettes.get(colorKey);
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

	///////////////////////////////////////////////////////////////////
	//  Below is code for dealing with unit sprites.
	///////////////////////////////////////////////////////////////////

	private static class UnitSpriteSetKey
	{
		public final UnitModel.UnitEnum unitTypeKey;
		public final Commander commanderKey;
		private static ArrayList<UnitSpriteSetKey> instances = new ArrayList<UnitSpriteSetKey>();

		private UnitSpriteSetKey(UnitModel.UnitEnum unitType, Commander co)
		{
			unitTypeKey = unitType;
			commanderKey = co;
		}

		public static UnitSpriteSetKey instance(UnitModel.UnitEnum unitType, Commander co)
		{
			UnitSpriteSetKey key = null;
			for(int i = 0; i < instances.size(); ++i)
			{
				if(instances.get(i).unitTypeKey == unitType && instances.get(i).commanderKey == co)
				{
					key = instances.get(i);
					break;
				}
			}
			if(key == null)
			{
				key = new UnitSpriteSetKey(unitType, co);
				instances.add(key);
			}
			return key;
		}
	}

	public static UnitSpriteSet getUnitMapSpriteSet(Unit unit)
	{
		UnitSpriteSetKey key = UnitSpriteSetKey.instance(unit.model.type, unit.CO);
		if( !unitMapSpriteSetMap.containsKey(key))
		{
			// We don't have it? Go load it.
			createUnitMapSpriteSet(key);
		}
		// We either found it or created it; it had better be there.
		return unitMapSpriteSetMap.get(key);
	}

	private static void createUnitMapSpriteSet(UnitSpriteSetKey key)
	{
		System.out.println("creating " + key.unitTypeKey.toString() + " spriteset for CO " + key.commanderKey.myColor.toString());
		String filestr = getUnitSpriteFilename(key.unitTypeKey);
		UnitSpriteSet spriteSet = new UnitSpriteSet( loadSpriteSheetFile(filestr), baseSpriteSize, baseSpriteSize, getMapUnitColors(key.commanderKey.myColor) );
		unitMapSpriteSetMap.put(key, spriteSet);
	}

	private static String getUnitSpriteFilename( UnitModel.UnitEnum unitType )
	{
		String spriteFile = "";
		switch( unitType )
		{
		case ANTI_AIR:
			break;
		case APC:
			spriteFile = "res/unit/apc_map.png";
			break;
		case ARTILLERY:
		break;
		case B_COPTER:
		break;
		case BATTLESHIP:
		break;
		case BOMBER:
		break;
		case CRUISER:
		break;
		case FIGHTER:
		break;
		case INFANTRY:
			spriteFile = "res/unit/infantry_map.png";
		break;
		case LANDER:
		break;
		case MD_TANK:
		break;
		case MECH:
			spriteFile = "res/unit/mech_map.png";
		break;
		case MISSILES:
		break;
		case RECON:
		break;
		case ROCKETS:
		break;
		case SUB:
		break;
		case T_COPTER:
		break;
		case TANK:
		break;
		}
		return spriteFile;
	}

	public static Sprite getMapUnitHPSprites()
	{
		if( null == mapUnitHPSprites )
		{
			mapUnitHPSprites = new Sprite(loadSpriteSheetFile("res/unit/unit_hp.png"), 8, 8);
		}
		return mapUnitHPSprites;
	}

	public static Sprite getMenuLetters()
	{
		if( null == menuLetterSprites )
		{
			menuLetterSprites = new Sprite(loadSpriteSheetFile("res/tileset/letters.png"), 5, 6);
		}
		return menuLetterSprites;
	}

	public static Sprite getMenuNumbers()
	{
		if( null == menuNumberSprites )
		{
			menuNumberSprites = new Sprite(loadSpriteSheetFile("res/tileset/numbers.png"), 5, 6);
		}
		return menuNumberSprites;
	}
}
