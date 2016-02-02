package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.ArrayList;

import Terrain.Environment;
import Terrain.GameMap;

/**
 * Responsible for storing and organizing all image data associated with a specific map tile type.
 */
public class TerrainSpriteSet
{
	ArrayList<Sprite> terrainSprites;
	//ArrayList<TileTransition> tileTransitions;
	public final Environment.Terrains myTerrainType = Environment.Terrains.GRASS;

    public static final short NORTH = 0x1;
    public static final short EAST = 0x2;
    public static final short SOUTH = 0x4;
    public static final short WEST = 0x8;
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
    
    public TerrainSpriteSet(BufferedImage spriteSheet, int spriteWidth, int spriteHeight)
    {
    	terrainSprites = new ArrayList<Sprite>();
    	
    	if(spriteSheet == null)
    	{
    		// Just make a single frame of the specified size.
    		createDefaultBlankSprite(spriteWidth, spriteHeight);
    	}
    	else
    	{
    		// Cut the sprite-sheet up and populate terrainSprites.
    		int xOffset = 0;
    		int yOffset = 0;
    		int spriteNum = 0;
    		int maxSpriteNum = NORTH | EAST | SOUTH | WEST; // 16 possible sprites per terrain type (0-15).
    		
			// Create the initial sprites    		
    		try
    		{
    			// Loop until we get as many sprites as we expect or run out of runway.
    			while(spriteNum <= maxSpriteNum && ( (spriteNum+1)*spriteWidth <= spriteSheet.getWidth() ) )
    			{
    				terrainSprites.add(new Sprite(spriteSheet.getSubimage(xOffset, yOffset, spriteWidth, spriteHeight)));
    				xOffset += spriteWidth;
    				spriteNum++;
    			}
    			
    			if(spriteNum != 1 && spriteNum != 15)
    			{
    				System.out.println("WARNING! TerrainSpriteSet detected a malformed sprite sheet!");
    				System.out.println("WARNING!   Found " + spriteNum + " " + spriteWidth + "x" + spriteHeight + " sprites in a "
    						+ spriteSheet.getWidth() + "x" + spriteSheet.getHeight() + " spritesheet");
    				System.out.println("WARNING!   (There should be 1 sprite or 16 in a terrain sprite sheet)");
    			}

    			maxSpriteNum = spriteNum; // However many sprites we found, we won't find more than that on a second horizontal pass.
    			
				// If this sprite has more vertical space, pull in alternate versions of the existing terrain tiles.
    			while(yOffset + spriteHeight <= spriteSheet.getHeight())
    			{
        			xOffset = 0;
        			spriteNum = 0;
        			
        			while(spriteNum <= maxSpriteNum && ( (spriteNum+1)*spriteWidth <= spriteSheet.getWidth() ) )
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
		
		short dirIndex = 0; // For non-directional sprites, the first one is the only one.
		if(terrainSprites.size() > 1) // If there is more than one image, we expect a full complement.
		{
			// Figure out which neighbors tiles have the same terrain type as this one.
			// NOTE: Simple directional tiles don't care about diagonal adjacency - To define
			//   sprites based on diagonally-adjacent tiles, define a TileTransition for that terrain type.
			dirIndex |= checkTileType(map, myTerrainType, x, y-1, NORTH);
			dirIndex |= checkTileType(map, myTerrainType, x+1, y, EAST);
			dirIndex |= checkTileType(map, myTerrainType, x, y+1, SOUTH);
			dirIndex |= checkTileType(map, myTerrainType, x-1, y, WEST);
		}
		
		// Normalize the index value just in case.
		if(dirIndex >= terrainSprites.size())
		{
			dirIndex = (short)(dirIndex % terrainSprites.size());
		}

		//g.drawImage(terrainSprites.get(dirIndex).getFrame(variation), x, y, null);
		BufferedImage frame = terrainSprites.get(dirIndex).getFrame(variation);
		g.drawImage(frame, x, y, frame.getWidth()*scale, frame.getHeight()*scale, null);
		
		// TODO - make tile transitions happen.
		// for(TileTransition tt : tileTransitions)
		//{
			// Draw any transition layers required.
		//}
	}
    
	/**
	 * If position (x, y) in map has TerrainType terrain, return value. Else return 0;
	 * 
	 * If position (x, y) is not a valid location (out of bounds) also return value. This has the effect
	 *   of allowing us to assume that tiles out of sight are whatever terrain we prefer - enabling us to
	 *   draw roads that go off the map, etc.
	 */
    private short checkTileType(GameMap map, Environment.Terrains terrain, int x, int y, short value)
    {
    	if(map.isLocationValid(x, y) && (map.getEnvironment(x, y).terrainType != myTerrainType) )
    	{
    		value = 0;
    	}
    	return value;
    }
}
