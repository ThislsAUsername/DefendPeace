package UI;

import java.awt.Color;
import java.awt.Graphics;

import javax.tools.JavaFileManager.Location;

import Terrain.Environment;

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
				if(myGame.gameMap.getLocation(w,h) != null)
				{
					drawTile(g, myGame.gameMap.getLocation(w,h), w*tileSizePx, h*tileSizePx);
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
	
	private void drawTile(Graphics g, Terrain.Location locus, int x, int y)
	{
		Environment tile = locus.getEnvironment();
		Color tileColor = Color.black; // TODO: This will be a sprite eventually.
		
		switch(tile.terrainType)
		{
		case PLAIN:
			tileColor = new Color(186,255,124);
			break;
		case CITY:
			tileColor = Color.GRAY;
			break;
		case FACTORY:
			tileColor = Color.DARK_GRAY; // TODO: Make this team color, if owned.
			break;
		case FOREST:
			tileColor = Color.GREEN;
			break;
		case WATER:
			tileColor = Color.BLUE;
			break;
		case HQ:
			tileColor = Color.RED; // TODO: Make this team color.
			break;
		case MOUNTAIN:
			tileColor = new Color(101,40,26);
			break;
		case REEF:
			tileColor = new Color(212,144,56);
			break;
		case ROAD:
			tileColor = Color.LIGHT_GRAY;
			break;
		case SHOAL:
			tileColor = new Color(240,209,77);
			break;
			default:
				tileColor = Color.BLACK;
		}
		
		g.setColor(tileColor);
		g.fillRect(x, y, tileSizePx, tileSizePx);
	}
}
