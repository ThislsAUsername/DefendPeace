package UI.Art.SpriteArtist;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Terrain.GameMap;
import UI.MapView;
import UI.Art.MapArtist;
import UI.Art.FillRectArtist.FillRectMapArtist;

import Engine.GameInstance;
import Engine.Path;
import Engine.Path.PathNode;

public class SpriteMapArtist implements MapArtist
{
	private GameInstance myGame;
	private GameMap gameMap;

	BufferedImage baseMapImage;

	private int drawScale;

	MapArtist backupArtist; // TODO: Make this obsolete.
	
	public SpriteMapArtist(GameInstance game)
	{
		myGame = game;
		gameMap = game.gameMap;
	}

	@Override
	public void setView(MapView view)
	{
		drawScale = view.getTileSize() / SpriteLibrary.baseSpriteSize;

		// TODO: make this obsolete.
		backupArtist = new FillRectMapArtist(myGame);
		backupArtist.setView(view);
		
		baseMapImage = new BufferedImage(gameMap.mapWidth*view.getTileSize(),
				gameMap.mapHeight*view.getTileSize(), BufferedImage.TYPE_INT_RGB);
		
		// Build base map image.
		buildMapImage(gameMap);
	}

	@Override
	public void drawMap(Graphics g)
	{
		// TODO: Change what/where we draw based on camera location.
		g.drawImage(baseMapImage, 0, 0, null);
	}

	@Override
	public void drawCursor(Graphics g)
	{
		backupArtist.drawCursor(g);
	}

	@Override
	public void drawMovePath(Graphics g, Path path)
	{
		int tileSize = SpriteLibrary.baseSpriteSize * drawScale;
		Sprite moveLineSprites = SpriteLibrary.getMoveCursorLineSprite();
		Sprite moveArrowSprites = SpriteLibrary.getMoveCursorArrowSprite();

		for(int i = 0; i < path.getWaypoints().size(); ++i)
		{
			// Figure out which line segment type to draw.
			PathNode lastp = null;
			PathNode thisp = path.getWaypoints().get(i);
			PathNode nextp = null;
			if( i > 0 )
			{
			  lastp = path.getWaypoints().get(i-1);
			}
			if( (i+1) < path.getWaypoints().size() )
			{
			  nextp = path.getWaypoints().get(i+1);
			}

			// Calculate which frame to draw
			short dir1 = getMovePathDirection( thisp, lastp );
			short dir2 = getMovePathDirection( thisp, nextp );
			int index = dir1 | dir2;

			// Choose the specific image; if nextp is null, we know it's the end of the path, so use an arrow;
			BufferedImage movePathSprite = (nextp != null)?
					moveLineSprites.getFrame( index ):
						moveArrowSprites.getFrame( index );

			// Draw it.
			g.drawImage( movePathSprite, thisp.x*tileSize, thisp.y*tileSize, tileSize, tileSize, null);
		}
	}

	/**
	 * Returns a short value representing the direction from 'before' to 'after'
	 * NOTE: It is assumed that 'before' and 'after' share a plane in common; i.e. they
	 *   will be cardinally oriented, no diagonals.
	 */
	private short getMovePathDirection( PathNode before, PathNode after )
	{
	    short NORTH = 0x1;
	    short EAST = 0x2;
	    short SOUTH = 0x4;
	    short WEST = 0x8;

	    short actualDir = 0x0;

		// We need two things for there to be a direction between them.
		if( before != null && after != null )
		{
			// Either x or y can be different. Check x offset.
			if( before.x != after.x )
			{
				actualDir = (before.x < after.x)? EAST : WEST;
			}
			else if( before.y != after.y ) // If not x, maybe y is different.
			{
				actualDir = (before.y < after.y)? SOUTH : NORTH;
			}
		}

		return actualDir;
	}

	@Override
	public void drawHighlights(Graphics g)
	{
		backupArtist.drawHighlights(g);
	}
	
	@Override
	public void alertTileChanged(int x, int y)
	{
		// TODO: Only draw the tiles that need to change, rather than the whole map?
		buildMapImage(gameMap);
	}

	public void buildMapImage(GameMap map)
	{
		Graphics g = baseMapImage.getGraphics();
		
		// Choose and draw all base sprites (grass, water, shallows).
		for(int y = 0; y < map.mapHeight; ++y) // Iterate horizontally to layer terrain correctly.
		{
			for(int x = 0; x < map.mapWidth; ++x)
			{
				TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet( map.getLocation(x, y) );
				
				spriteSet.drawTile(g, gameMap, x, y, drawScale);
			}
		}
	}
}
