package UI;

import java.awt.Color;
import java.awt.Graphics;

import Terrain.Environment;
import Units.Unit;

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
					drawLocation(g, myGame.gameMap.getLocation(w,h), w*tileSizePx, h*tileSizePx);
				}
				else
				{
					System.out.println("Error! Tile is null! (" + w + ", " + h + ")");
				}
			}
		}

		if (myGame.currentMenu == null) {
			// Draw the current cursor location.
			g.setColor(new Color(253,171,77));
			g.fillRect(myGame.getCursorX()*tileSizePx, myGame.getCursorY()*tileSizePx, tileSizePx, tileSizePx);
		} else {
			g.setColor(Color.black); // outer border
			g.fillRect(myGame.gameMap.mapWidth*tileSizePx/4, myGame.gameMap.mapHeight*tileSizePx/4, myGame.gameMap.mapWidth*tileSizePx/2, myGame.gameMap.mapHeight*tileSizePx/2);
			g.setColor(Color.cyan); // inner fill
			g.fillRect(myGame.gameMap.mapWidth*tileSizePx/4+1, myGame.gameMap.mapHeight*tileSizePx/4+1, myGame.gameMap.mapWidth*tileSizePx/2-2, myGame.gameMap.mapHeight*tileSizePx/2-2);
			g.setColor(new Color(253,171,77)); // selection
			g.fillRect(myGame.gameMap.mapWidth*tileSizePx/4+1, (myGame.currentMenu.selectedOption+1)*tileSizePx/2+myGame.gameMap.mapHeight*tileSizePx/4+4, myGame.gameMap.mapWidth*tileSizePx/2-2, tileSizePx/2);
			g.setColor(Color.MAGENTA);
			String str = new String("Money: " + myGame.activeCO.money);
			g.drawChars(str.toCharArray(), 0, str.length(), myGame.gameMap.mapWidth*tileSizePx/4+4, tileSizePx/2+myGame.gameMap.mapHeight*tileSizePx/4);
			g.setColor(Color.black);
			for (int i = 0; i < myGame.currentMenu.labels.length; i++) {
				g.drawChars(myGame.currentMenu.labels[i].toCharArray(), 0, myGame.currentMenu.labels[i].length(), myGame.gameMap.mapWidth*tileSizePx/4+4, (i+2)*tileSizePx/2+myGame.gameMap.mapHeight*tileSizePx/4);
			}
		}
	}
	
	private void drawLocation(Graphics g, Terrain.Location locus, int x, int y)
	{
		Environment tile = locus.getEnvironment();
		Color tileColor = Color.black; // TODO: This will be a sprite eventually.
		
		switch(tile.terrainType)
		{
		case PLAIN:
			tileColor = new Color(186,255,124);
			break;
		case CITY:
			if (locus.getOwner() != null) {
				tileColor = locus.getOwner().myColor;
			} else {
				tileColor = Color.GRAY;
			}
			break;
		case FACTORY:
			if (locus.getOwner() != null) {
				tileColor = locus.getOwner().myColor;
			} else {
				tileColor = Color.DARK_GRAY;
			}
//			tileColor = Color.DARK_GRAY; // TODO: Make this team color, if owned.
			break;
		case FOREST:
			tileColor = Color.GREEN;
			break;
		case WATER:
			tileColor = Color.BLUE;
			break;
		case HQ:
			if (locus.getOwner() != null) {
				tileColor = locus.getOwner().myColor;
			} else {
				tileColor = Color.RED;
			}
//			tileColor = Color.RED; // TODO: Make this team color.
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
		
		if(locus.getResident() != null)
		{
			drawUnit(g, locus.getResident());
			
		}
	}
	
	private void drawUnit(Graphics g, Unit unit)
	{
		int health = (int)unit.HP;
		int offset = (int)(tileSizePx * 0.25);
		int length = tileSizePx - offset;
		g.setColor(Color.BLACK);
		g.fillRect(unit.x * tileSizePx + offset/2, unit.y * tileSizePx + offset/2, length, length);
		g.setColor(unit.CO.myColor);
		g.fillRect(unit.x * tileSizePx + (offset/2)+1, unit.y * tileSizePx + (offset/2)+1, length-2, length-2);
	}
}
