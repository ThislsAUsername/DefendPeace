package UI;

import java.awt.Color;
import java.awt.Graphics;

import Terrain.Tile;

import Engine.GameInstance;

public class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private GameInstance myGame;
	
	public static final int tileSizePx = 32;

	public MapView(GameInstance game)
	{
		myGame = game;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for(int w = 0; w < myGame.gameMap.mapWidth; ++w)
		{
			for(int h = 0; h < myGame.gameMap.mapHeight; ++h)
			{
				if(myGame.gameMap.getTile(w,h) != null)
				{
					drawTile(g, myGame.gameMap.getTile(w,h), w*tileSizePx, h*tileSizePx);
				}
				else
				{
					System.out.println("Error! Tile is null! (" + w + ", " + h + ")");
				}
			}
		}

		// Draw the current cursor location.
		g.setColor(new Color(253,171,77));
		g.fillRect(myGame.getCursorX()*tileSizePx, myGame.getCursorY()*tileSizePx, tileSizePx, tileSizePx);
	}
	
	private void drawTile(Graphics g, Tile tile, int x, int y)
	{
		Color tileColor = Color.black; // TODO: This will be a sprite eventually.
		
		switch(tile.terrainType)
		{
		case PLAIN:
			tileColor = new Color(186,255,124);
			break;
		case CITY:
			tileColor = Color.LIGHT_GRAY;
			break;
		}
		
		g.setColor(tileColor);
		g.fillRect(x, y, tileSizePx, tileSizePx);
	}
}
