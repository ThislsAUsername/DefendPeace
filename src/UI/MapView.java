package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import Terrain.Environment;
import UI.Art.FillRectMapArtist;
import UI.Art.FillRectMenuArtist;
import UI.Art.FillRectUnitArtist;
import UI.Art.MapArtist;
import UI.Art.MenuArtist;
import UI.Art.UnitArtist;
import Units.Unit;
import Units.UnitModel.UnitEnum;

import Engine.GameInstance;

public class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private GameInstance myGame;
	
	private MapArtist mapArtist;
	private UnitArtist unitArtist;
	private MenuArtist menuArtist;
	
	public static final int tileSizePx = 32;
	public static int mapViewWidth = tileSizePx * 15;
	public static int mapViewHeight = tileSizePx * 10;

	public MapView(GameInstance game)
	{
		myGame = game;
		setPreferredSize(new Dimension(mapViewWidth, mapViewHeight));

		unitArtist = new FillRectUnitArtist(myGame);
		mapArtist = new FillRectMapArtist(myGame);
		menuArtist = new FillRectMenuArtist(myGame);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		mapArtist.drawMap(g);
		unitArtist.drawUnits(g);
		mapArtist.drawHighlights(g);
		
		// Draw the user attention indicator.
		if (myGame.currentMenu == null)
		{
			mapArtist.drawCursor(g);
		}
		else
		{
			menuArtist.drawMenu(g);
		}
	}
}
