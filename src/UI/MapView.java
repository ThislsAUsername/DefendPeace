package UI;

import java.awt.Color;
import java.awt.Graphics;

import Terrain.Tile;

import Engine.GameInstance;

public class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private GameInstance myGame;
	private int cursorX = 0;
	private int cursorY = 0;
	
	public static final int tileSizePx = 32;

	public MapView(GameInstance game)
	{
		myGame = game;
	}
	
	public void handleAction(InputHandler.InputAction action)
	{
		switch (action)
		{
		case UP:
			cursorY -= 1;
			if(cursorY < 0) cursorY = 0;
			break;
		case DOWN:
			cursorY +=1;
			if(cursorY >= myGame.gameMap.mapHeight) cursorY = myGame.gameMap.mapHeight - 1;
			break;
		case LEFT:
			cursorX -= 1;
			if(cursorX < 0) cursorX = 0;
			break;
		case RIGHT:
			cursorX += 1;
			if(cursorX >= myGame.gameMap.mapWidth) cursorX = myGame.gameMap.mapWidth - 1;
			break;
		case ENTER:
			break;
			default:
				System.out.println("WARNING! MapView was given invalid action enum (" + action + ")");
		}
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
					drawTile(g, myGame.gameMap.map[w][h], w*tileSizePx, h*tileSizePx);
				}
				else
				{
					System.out.println("Error! Tile is null! (" + w + ", " + h + ")");
				}
			}
		}

		// Draw the current cursor location.
		g.setColor(new Color(253,171,77));
		g.fillRect(cursorX*tileSizePx, cursorY*tileSizePx, tileSizePx, tileSizePx);
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
