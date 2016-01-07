package UI.Art;

import java.awt.Color;
import java.awt.Graphics;

import Engine.GameInstance;
import Terrain.GameMap;
import Terrain.Location;
import UI.MapView;
import Units.Unit;

public class FillRectUnitArtist implements UnitArtist
{
	private int tileSizePx = MapView.tileSizePx;
	
	GameInstance myGame;
	GameMap gameMap;
	
	public static final Color COLOR_TIRED = new Color(128,128,128,160);
	
	public FillRectUnitArtist(GameInstance game)
	{
		myGame = game;
		gameMap = myGame.gameMap;
	}
	
	public void drawUnits(Graphics g)
	{
		// TODO: It would be pretty swell if we could just ask the map for a list of units.
		for(int w = 0; w < gameMap.mapWidth; ++w)
		{
			for(int h = 0; h < gameMap.mapHeight; ++h)
			{
				if(gameMap.getLocation(w,h) != null)
				{
					Location locus = gameMap.getLocation(w,h);
					if(locus.getResident() != null)
					{
						drawUnit(g, locus.getResident());
					}
				}
			}
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
		if(unit.isTurnOver && unit.CO == myGame.activeCO)
		{
			g.setColor(COLOR_TIRED);
			g.fillRect(unit.x * tileSizePx + (offset/2)+1, unit.y * tileSizePx + (offset/2)+1, length-2, length-2);
		}
		g.setColor(Color.BLACK);
		g.drawChars(health.toString().toCharArray(), 0, health.toString().length(), unit.x * tileSizePx + 8, unit.y*tileSizePx + (int)(tileSizePx*0.66));
	}
}
