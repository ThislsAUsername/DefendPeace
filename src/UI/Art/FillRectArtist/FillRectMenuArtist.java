package UI.Art.FillRectArtist;

import java.awt.Color;
import java.awt.Graphics;

import Engine.GameInstance;
import UI.MapView;
import UI.Art.MenuArtist;
import Units.UnitModel.UnitEnum;

public class FillRectMenuArtist implements MenuArtist
{
	private int tileSizePx = MapView.tileSizePx;
	private int mapViewWidth = MapView.mapViewWidth;
	private int mapViewHeight = MapView.mapViewHeight;

	GameInstance myGame;
	MapView myView;

	public static final Color COLOR_CURSOR = new Color(253,171,77,200);
	
	public FillRectMenuArtist(GameInstance game, MapView view)
	{
		myGame = game;
		myView = view;
	}
	
	@Override
	public void drawMenu(Graphics g)
	{
		int menuBorderLeft = mapViewWidth/4;
		int menuBorderTop = mapViewHeight/4;
		int menuWidth = mapViewWidth / 2;
		int menuHeight = mapViewHeight / 2;
		
		g.setColor(Color.black); // outer border
		g.fillRect(menuBorderLeft, menuBorderTop, menuWidth, menuHeight);
		g.setColor(Color.cyan); // inner fill
		g.fillRect(menuBorderLeft+1, menuBorderTop+1, menuWidth-2, menuHeight-2);
		String label;
		switch (myView.currentMenu.menuType) {
		case PRODUCTION:
			g.setColor(COLOR_CURSOR);
			g.fillRect(menuBorderLeft+1, (myView.currentMenu.getSelectionNumber()+1)*tileSizePx/2+menuBorderTop+4, menuWidth-2, tileSizePx/2);
			g.setColor(Color.MAGENTA);
			label = new String("Money: " + myGame.activeCO.money);
			g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, tileSizePx/2+menuBorderTop);
			g.setColor(Color.black);
			for (int i = 0; i < myView.currentMenu.getNumChoices(); i++) {
				label = myView.currentMenu.getOptions()[i].toString()+ ": " + myGame.activeCO.getUnitModel((UnitEnum) myView.currentMenu.getOptions()[i]).moneyCost;
				g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, (i+2)*tileSizePx/2+menuBorderTop);
			}
			break;
		case ACTION:
			g.setColor(COLOR_CURSOR);
			g.fillRect(menuBorderLeft+1, (myView.currentMenu.getSelectionNumber())*tileSizePx/2+menuBorderTop+4, menuWidth-2, tileSizePx/2);
			g.setColor(Color.black);
			for (int i = 0; i < myView.currentMenu.getNumChoices(); i++) {
				label = myView.currentMenu.getOptions()[i].toString();
				g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, (i+1)*tileSizePx/2+menuBorderTop);
			}
			break;
		case METAACTION:
			g.setColor(COLOR_CURSOR);
			g.fillRect(menuBorderLeft+1, (myView.currentMenu.getSelectionNumber())*tileSizePx/2+menuBorderTop+4, menuWidth-2, tileSizePx/2);
			g.setColor(Color.black);
			for (int i = 0; i < myView.currentMenu.getNumChoices(); i++) {
				label = myView.currentMenu.getOptions()[i].toString();
				g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, (i+1)*tileSizePx/2+menuBorderTop);
			}
			break;
		default:
			g.setColor(Color.black);
			System.out.println("WARNING! FillRectMenuArtist was given an undefined MenuType!");
			label = new String("Undefined MenuType!");
			g.drawChars(label.toCharArray(), 0, label.length(), menuBorderLeft+4, tileSizePx/2+menuBorderTop);
			break;
		}
	}
}
