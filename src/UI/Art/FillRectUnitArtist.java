package UI.Art;

import java.awt.Color;
import java.awt.Graphics;

import Engine.GameAction;
import Engine.GameInstance;
import Engine.Path;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.Location;
import UI.MapView;
import Units.Unit;

public class FillRectUnitArtist implements UnitArtist
{
	private int tileSizePx = MapView.tileSizePx;
	
	private GameInstance myGame = null;
	private GameMap gameMap = null;
	private MapView myView = null;
	
	public static final Color COLOR_TIRED = new Color(128,128,128,160);
	
	public FillRectUnitArtist(GameInstance game, MapView view)
	{
		myGame = game;
		gameMap = myGame.gameMap;
		myView = view;
	}
	
	public void drawUnits(Graphics g)
	{
		// Get the currently-focused Unit, if there is one.
		Unit currentActor = null;
		if(null != myView.currentAction)
		{
			currentActor = myView.currentAction.getActor();
		}

		// Draw all the units except for the one with focus.
		for(int w = 0; w < gameMap.mapWidth; ++w)
		{
			for(int h = 0; h < gameMap.mapHeight; ++h)
			{
				if(gameMap.getLocation(w,h) != null)
				{
					Location locus = gameMap.getLocation(w,h);
					// Draw all units except for the currently-selected one, if there is one.
					if(locus.getResident() != null && locus.getResident() != currentActor)
					{
						drawUnit(g, locus.getResident());
					}
				}
			}
		}

		// Figure out where to draw the focused unit.
		if(null != currentActor)
		{
			Path actorPath = myView.currentMovePath;

			if(actorPath == null)
			{
				// No move command under consideration. Draw unit at current location.
				drawUnit(g, currentActor);
			}
			else
			{
				XYCoord pos = actorPath.getPosition();
				drawUnit(g, currentActor, pos.xCoord, pos.yCoord);
			}
		}
	}

	private void drawUnit(Graphics g, Unit unit)
	{
		drawUnit(g, unit, unit.x, unit.y);
	}

	private void drawUnit(Graphics g, Unit unit, double x, double y)
	{
		Integer health = unit.getHP();
		int offset = (int)(tileSizePx * 0.25);
		int length = tileSizePx - offset;
		g.setColor(Color.BLACK);
		g.fillRect((int)(x * tileSizePx + offset/2), (int)(y * tileSizePx + offset/2), length, length);
		g.setColor(unit.CO.myColor);
		g.fillRect((int)(x * tileSizePx + (offset/2)+1), (int)(y * tileSizePx + (offset/2)+1), length-2, length-2);
		if(unit.isTurnOver && unit.CO == myGame.activeCO)
		{
			g.setColor(COLOR_TIRED);
			g.fillRect((int)(x * tileSizePx + (offset/2)+1), (int)(y * tileSizePx + (offset/2)+1), length-2, length-2);
		}
		g.setColor(Color.BLACK);
		g.drawChars(health.toString().toCharArray(), 0, health.toString().length(), (int)(x * tileSizePx + 8), (int)(y*tileSizePx + tileSizePx*0.66));
	}
}
