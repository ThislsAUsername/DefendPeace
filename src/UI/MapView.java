package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import Terrain.Environment;
import Units.Unit;

import Engine.DamageChart;
import Engine.DamageChart.UnitEnum;
import Engine.GameInstance;

public class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private GameInstance myGame;
	
	public static final int tileSizePx = 32;
	public static int mapViewWidth = tileSizePx * 15;
	public static int mapViewHeight = tileSizePx * 10;

	public MapView(GameInstance game)
	{
		myGame = game;
		setPreferredSize(new Dimension(mapViewWidth, mapViewHeight));
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
			drawMenu(g);
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
	
	private void drawMenu(Graphics g) {
		int menuBorderLeft = mapViewWidth/4;
		int menuBorderTop = mapViewHeight/4;
		int menuWidth = mapViewWidth / 2;
		int menuHeight = mapViewHeight / 2;
		
		g.setColor(Color.black); // outer border
		g.fillRect(menuBorderLeft, menuBorderTop, menuWidth, menuHeight);
		g.setColor(Color.cyan); // inner fill
		g.fillRect(menuBorderLeft+1, menuBorderTop+1, menuWidth-2, menuHeight-2);
		String label;
//		System.out.println("Current selection is: " + myGame.currentMenu.selectedOption);
		switch (myGame.currentMenu.menuType) {
		case PRODUCTION:
			g.setColor(new Color(253,171,77)); // selection
			g.fillRect(menuBorderLeft+1, (myGame.currentMenu.selectedOption+1)*tileSizePx/2+menuBorderTop+4, menuWidth-2, tileSizePx/2);
			g.setColor(Color.MAGENTA);
			label = new String("Money: " + myGame.activeCO.money);
			g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, tileSizePx/2+menuBorderTop);
			g.setColor(Color.black);
			for (int i = 0; i < myGame.currentMenu.getNumChoices(); i++) {
				label = myGame.currentMenu.getOptions()[i].toString()+ ": " + myGame.activeCO.getUnitModel((UnitEnum) myGame.currentMenu.getOptions()[i]).moneyCost;
				g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, (i+2)*tileSizePx/2+menuBorderTop);
			}
			break;
		case ACTION:
			g.setColor(new Color(253,171,77)); // selection
			g.fillRect(menuBorderLeft+1, (myGame.currentMenu.selectedOption)*tileSizePx/2+menuBorderTop+4, menuWidth-2, tileSizePx/2);
			g.setColor(Color.black);
			for (int i = 0; i < myGame.currentMenu.getNumChoices(); i++) {
				label = myGame.currentMenu.getOptions()[i].toString();
				g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, (i+1)*tileSizePx/2+menuBorderTop);
			}
			break;
		default:
			g.setColor(Color.black);
			label = new String("This is an undefined menu type. Thats... probably a problem.");
			g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, tileSizePx/2+menuBorderTop);
			break;
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
