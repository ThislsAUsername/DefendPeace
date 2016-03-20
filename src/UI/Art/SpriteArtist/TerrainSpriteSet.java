package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;

import Terrain.Environment;
import Terrain.GameMap;
import UI.MapView;

/**
 * Responsible for storing and organizing all image data associated with a specific map tile type.
 */
public class TerrainSpriteSet
{
	private ArrayList<Sprite> terrainSprites;
	private ArrayList<TerrainSpriteSet> tileTransitions;
	public final Environment.Terrains myTerrainType;

	int drawOffsetx;
	int drawOffsety;

    // Keys to the sprite array - logical OR the four cardinals to get the corresponding sprite index.
    // The four diagonal directions are just the index for that corner transition.
    private static final short NORTH = 0x1;
    private static final short EAST = 0x2;
    private static final short SOUTH = 0x4;
    private static final short WEST = 0x8;
    private static final short NW = 16;
    private static final short NE = 17;
    private static final short SE = 18;
    private static final short SW = 19;
    // public static final short PLACEHOLDER5 = 0x10;
    // public static final short PLACEHOLDER6 = 0x20;
    // public static final short PLACEHOLDER7 = 0x40;
    // public static final short PLACEHOLDER8 = 0x80;
    // public static final short PLACEHOLDER9 = 0x100;
    // public static final short PLACEHOLDER10 = 0x200;
    // public static final short PLACEHOLDER11 = 0x400;
    // public static final short PLACEHOLDER12 = 0x800;
    // public static final short PLACEHOLDER13 = 0x1000;
    // public static final short PLACEHOLDER14 = 0x2000;
    // public static final short PLACEHOLDER15 = 0x4000;
    
    public TerrainSpriteSet(Environment.Terrains terrainType, BufferedImage spriteSheet, int spriteWidth, int spriteHeight)
    {
        myTerrainType = terrainType;
    	terrainSprites = new ArrayList<Sprite>();
		tileTransitions = new ArrayList<TerrainSpriteSet>();
    	
        // We assume here that all sprites are sized in multiples of the base sprite size.
        drawOffsetx = spriteWidth / SpriteLibrary.baseSpriteSize - 1;
        drawOffsety = spriteHeight / SpriteLibrary.baseSpriteSize - 1;

    	if(spriteSheet == null)
    	{
			System.out.println("WARNING! Continuing with placeholder images.");
    		// Just make a single frame of the specified size.
			drawOffsetx = 0;
			drawOffsety = 0;
			createDefaultBlankSprite(SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize);
    	}
    	else
    	{
    		// Cut the sprite-sheet up and populate terrainSprites.
    		int xOffset = 0;
    		int yOffset = 0;
    		int spriteNum = 0;
    		int maxSpriteIndex = (NORTH | EAST | SOUTH | WEST) + 4; // 20 possible sprites per terrain type (0-15, plus four corners).
    		
			// Create the initial sprites    		
    		try
    		{
    			// Loop until we get as many sprites as we expect or run out of runway.
    			while(spriteNum <= maxSpriteIndex && ( (spriteNum+1)*spriteWidth <= spriteSheet.getWidth() ) )
    			{
    				terrainSprites.add(new Sprite(spriteSheet.getSubimage(xOffset, yOffset, spriteWidth, spriteHeight)));
    				xOffset += spriteWidth;
    				spriteNum++;
    			}
    			
                if(spriteNum != 1 && spriteNum != 16 && spriteNum != 20)
    			{
    				System.out.println("WARNING! TerrainSpriteSet detected a malformed sprite sheet!");
    				System.out.println("WARNING!   Found " + spriteNum + " " + spriteWidth + "x" + spriteHeight + " sprites in a "
    						+ spriteSheet.getWidth() + "x" + spriteSheet.getHeight() + " spritesheet");
    				System.out.println("WARNING!   (There should be 1, 16, or 20 sprites in a terrain sprite sheet)");
    			}

    			maxSpriteIndex = spriteNum-1; // However many sprites we found, we won't find more than that on a second horizontal pass.
    			
				// If this sprite has more vertical space, pull in alternate versions of the existing terrain tiles.
    			while(yOffset + spriteHeight <= spriteSheet.getHeight())
    			{
        			xOffset = 0;
        			spriteNum = 0;
        			
        			while(spriteNum <= maxSpriteIndex && ( (spriteNum+1)*spriteWidth <= spriteSheet.getWidth() ) )
        			{
        				terrainSprites.get(spriteNum).addFrame(spriteSheet.getSubimage(xOffset, yOffset, spriteWidth, spriteHeight));
        				xOffset += spriteWidth;
        				spriteNum++;
        			}

        			yOffset += spriteHeight;
    			}
    		}
    		catch(RasterFormatException RFE) // This occurs if we go off the end of the sprite sheet.
    		{
    			System.out.println("WARNING! RasterFormatException while loading Sprite. Attempting to continue.");
    			//RFE.printStackTrace();
    			
    			terrainSprites.clear(); // Clear this in case of partially-created data.

        		// Make a single frame of the specified size.
    			createDefaultBlankSprite(spriteWidth, spriteHeight);
    		}
    	} // spriteSheet != null
    	System.out.println("INFO: Created TerrainSpriteSheet with " + terrainSprites.size() + " sprites.");
    }
    
    public void addTileTransition(Environment.Terrains otherTerrain, BufferedImage spriteSheet, int spriteWidth, int spriteHeight)
    {
        tileTransitions.add(new TerrainSpriteSet(otherTerrain, spriteSheet, spriteWidth, spriteHeight));
    }

    public void colorize(Color[] oldColors, Color[] newColors)
    {
        for(Sprite s : terrainSprites)
        {
            s.colorize(oldColors, newColors);
        }
    }

    private void createDefaultBlankSprite(int w, int h)
    {
		BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		Graphics big = bi.getGraphics();
		big.setColor(Color.BLACK);
		big.fillRect(0, 0, w, h);
		terrainSprites.add(new Sprite(bi));
    }

	/**
	 * Draws the terrain at the indicated location, accounting for any defined tile transitions.  
	 */
	public void drawTile(Graphics g, GameMap map, int x, int y, int scale)
	{
		int variation = (x+1)*(y)+x; // Used to vary the specific sprite version drawn at each place in a repeatable way.
		
		short dirIndex = 0;
		boolean assumeSameTileType = myTerrainType == map.getEnvironment(x, y).terrainType;
		if(terrainSprites.size() > 1) // We expect the size to be either 1, 16, or 20.
		{
			// Figure out which neighbors tiles have the same terrain type as this one.
			dirIndex |= checkTileType(map, myTerrainType, x, y-1, assumeSameTileType)? NORTH:0;
			dirIndex |= checkTileType(map, myTerrainType, x+1, y, assumeSameTileType)? EAST:0;
			dirIndex |= checkTileType(map, myTerrainType, x, y+1, assumeSameTileType)? SOUTH:0;
			dirIndex |= checkTileType(map, myTerrainType, x-1, y, assumeSameTileType)? WEST:0;
		}

		// Normalize the index value just in case.
		if(dirIndex >= terrainSprites.size())
		{
			// We could print a warning here, but there should have been one when the sprites were loaded.
			dirIndex = (short)(dirIndex % terrainSprites.size());
		}

		// Draw the base tile type, if needed.
		Environment.Terrains baseTerrainType = getBaseTerrainType(myTerrainType);
		if(baseTerrainType != myTerrainType)
		{
			System.out.println("Drawing " + baseTerrainType + " as base of " + myTerrainType);
			TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet( baseTerrainType );
			spriteSet.drawTile(g, map, x, y, scale);
		}

		int tileSize = SpriteLibrary.baseSpriteSize * scale;

		// Draw the current tile
		BufferedImage frame = terrainSprites.get((dirIndex % terrainSprites.size())).getFrame(variation);
		g.drawImage(frame, (x-drawOffsetx)*tileSize, (y-drawOffsety)*tileSize,
				frame.getWidth()*scale, frame.getHeight()*scale, null);
		
		// Handle drawing corner-case tile variations if needed.
		if(terrainSprites.size() == 20)
		{
			// If we didn't have a N or W transition, then look in the NW position
			if((dirIndex & (NORTH | WEST)) == 0 && checkTileType(map, myTerrainType, x-1, y-1, assumeSameTileType))
			{
				g.drawImage(terrainSprites.get(NW).getFrame(variation),	(x-drawOffsetx)*tileSize,
						(y-drawOffsety)*tileSize, frame.getWidth()*scale, frame.getHeight()*scale, null);
			}
			if((dirIndex & (NORTH | EAST)) == 0 && checkTileType(map, myTerrainType, x+1, y-1, assumeSameTileType))
			{
				g.drawImage(terrainSprites.get(NE).getFrame(variation),	(x-drawOffsetx)*tileSize,
						(y-drawOffsety)*tileSize, frame.getWidth()*scale, frame.getHeight()*scale, null);
			}
			if((dirIndex & (SOUTH | EAST)) == 0 && checkTileType(map, myTerrainType, x+1, y+1, assumeSameTileType))
			{
				g.drawImage(terrainSprites.get(SE).getFrame(variation),	(x-drawOffsetx)*tileSize,
						(y-drawOffsety)*tileSize, frame.getWidth()*scale, frame.getHeight()*scale, null);
			}
			if((dirIndex & (SOUTH | WEST)) == 0 && checkTileType(map, myTerrainType, x-1, y+1, assumeSameTileType))
			{
				g.drawImage(terrainSprites.get(SW).getFrame(variation),	(x-drawOffsetx)*tileSize,
						(y-drawOffsety)*tileSize, frame.getWidth()*scale, frame.getHeight()*scale, null);
			}
		}

		// Draw any tile transitions that are needed.
		for(TerrainSpriteSet tt : tileTransitions)
		{
			tt.drawTile(g, map, x, y, scale);
		}
	}
    
	/**
	 * If position (x, y) in map has TerrainType terrain, return true. Else return false;
	 * 
	 * If position (x, y) is not a valid location (out of bounds), then return true IFF assumeTrue. This has the effect
	 *   of allowing us to assume that tiles out of sight are whatever terrain we prefer - enabling us to
	 *   draw roads that go off the map, etc, but keep it from looking like there is always land across the
	 *   water at the edge of the map due to unwanted cliff-face transitions.
	 */
    private boolean checkTileType(GameMap map, Environment.Terrains terrain, int x, int y, boolean assumeTrue)
    {
		return (map.isLocationValid(x, y) &&
				  ((map.getEnvironment(x,y).terrainType == myTerrainType) || // Valid location, terrain types match.
				  (getBaseTerrainType(map.getEnvironment(x,y).terrainType) == myTerrainType)) // Valid location, terrain base matches. 
			    || (!map.isLocationValid(x,y) && assumeTrue) ); // Invalid location, but assuming true for that case.
    }

	/**
	 * Determines the base terrain type for the provided environment terrain type.
	 * For example, FOREST is a tile type, but the trees sit on a plain, so for drawing
	 * purposes (esp. terrain transitions), the base tile type of FOREST is actually PLAIN.
	 */
	private static Environment.Terrains getBaseTerrainType(Environment.Terrains terrainType)
	{
		Environment.Terrains baseTerrain = terrainType;
		switch( baseTerrain )
		{
		case CITY:
		case DUNES:
		case FACTORY:
		case FOREST:
		case HQ:
		case MOUNTAIN:
		case GRASS:
		case ROAD:
			baseTerrain = Environment.Terrains.GRASS;
			break;
		case SHOAL:
			baseTerrain = Environment.Terrains.SHOAL;
			break;
		case REEF:
		case SEA:
			baseTerrain = Environment.Terrains.SEA;
			break;
			default:
				System.out.println("ERROR! [SpriteMapArtist.buildMapImage] Invalid terrain type " + baseTerrain);
		}

		return baseTerrain;
	}
}
