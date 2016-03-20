package UI.Art.SpriteArtist;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Terrain.Environment;
import Terrain.GameMap;
import UI.MapView;
import UI.Art.MapArtist;
import UI.Art.FillRectArtist.FillRectMapArtist;

import Engine.GameInstance;
import Engine.Path;

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
	public void drawMovePath(Graphics g, Path p)
	{
		backupArtist.drawMovePath(g, p);
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
		System.out.println("Drawing map!");
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
