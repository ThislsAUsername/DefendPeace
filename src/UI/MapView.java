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

	public static final Color COLOR_PLAIN = new Color(186,255,124);
	public static final Color COLOR_CITY = Color.GRAY;
	public static final Color COLOR_FACTORY = Color.DARK_GRAY;
	public static final Color COLOR_FOREST = Color.GREEN;
	public static final Color COLOR_WATER = Color.BLUE;
	public static final Color COLOR_MOUNTAIN = new Color(101,40,26);
	public static final Color COLOR_REEF = new Color(212,144,56);
	public static final Color COLOR_ROAD = Color.LIGHT_GRAY;
	public static final Color COLOR_SHOAL = new Color(240,209,77);
	public static final Color HIGHLIGHT_MOVE = new Color(255,255,255,160);
	public static final Color HIGHLIGHT_ATTACK = new Color(255,0,0,160);

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
			tileColor = COLOR_PLAIN;
			break;
		case CITY:
			if (locus.getOwner() != null) {
				tileColor = locus.getOwner().myColor;
			} else {
				tileColor = COLOR_CITY;
			}
			break;
		case FACTORY:
			if (locus.getOwner() != null) {
				tileColor = locus.getOwner().myColor;
			} else {
				tileColor = COLOR_FACTORY;
			}
			break;
		case FOREST:
			tileColor = COLOR_FOREST;
			break;
		case WATER:
			tileColor = COLOR_WATER;
			break;
		case HQ:
			if (locus.getOwner() != null) {
				tileColor = locus.getOwner().myColor;
			}
			break;
		case MOUNTAIN:
			tileColor = COLOR_MOUNTAIN;
			break;
		case REEF:
			tileColor = COLOR_REEF;
			break;
		case ROAD:
			tileColor = COLOR_ROAD;
			break;
		case SHOAL:
			tileColor = COLOR_SHOAL;
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

		if(locus.isHighlightSet())
		{
			if(locus.getResident() != null && locus.getResident().CO != myGame.activeCO)
			{
				g.setColor(HIGHLIGHT_ATTACK);
			}
			else
			{
				g.setColor(HIGHLIGHT_MOVE);
			}
			g.fillRect(x, y, tileSizePx, tileSizePx);
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
		case METAACTION:
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
			System.out.println("WARNING! MapView.drawMenu was given an undefined MenuType!");
			label = new String("WARNING! MapView.drawMenu was given an undefined MenuType!");
			g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, tileSizePx/2+menuBorderTop);
			break;
		}
	}
	
	private void drawUnit(Graphics g, Unit unit)
	{
		Integer health = (int)Math.ceil(unit.HP/10);
		int offset = (int)(tileSizePx * 0.25);
		int length = tileSizePx - offset;
		g.setColor(Color.BLACK);
		g.fillRect(unit.x * tileSizePx + offset/2, unit.y * tileSizePx + offset/2, length, length);
		g.setColor(unit.CO.myColor);
		g.fillRect(unit.x * tileSizePx + (offset/2)+1, unit.y * tileSizePx + (offset/2)+1, length-2, length-2);
		g.setColor(Color.BLACK);
		g.drawChars(health.toString().toCharArray(), 0, health.toString().length(), unit.x * tileSizePx + 8, unit.y*tileSizePx + (int)(tileSizePx*0.66));
	}
}
