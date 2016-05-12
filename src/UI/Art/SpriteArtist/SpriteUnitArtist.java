package UI.Art.SpriteArtist;

import java.awt.Graphics;

import Engine.GameInstance;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;

public class SpriteUnitArtist
{
	private GameInstance myGame;
	private SpriteMapView myView;
	int drawScale;
	
	// Variables for controlling unit map animations.
	private int currentAnimIndex = 0;
	private long lastIndexUpdateTime = 0;
	private final double indexUpdateTime = 250;

	public SpriteUnitArtist( GameInstance game, SpriteMapView view )
	{
		myGame = game;

		myView = view;
		// Get a convenient copy of the view's scaling factor.
		drawScale = view.getDrawScale();
	}

	public void drawUnits(Graphics g)
	{
		// Get an easy reference to the map.
		GameMap gameMap = myGame.gameMap;
		
		updateSpriteIndex(); // Every map unit will be drawn with the same sprite index.
		
		// Draw all current units.
		for(int w = 0; w < gameMap.mapWidth; ++w)
		{
			for(int h = 0; h < gameMap.mapHeight; ++h)
			{
				if(gameMap.getLocation(w,h) != null)
				{
					Location locus = gameMap.getLocation(w,h);
					if(locus.getResident() != null)
					{
						drawUnit(g, locus.getResident(), w, h);
					}
				}
			}
		}
	}

	public void drawUnitHPIcons(Graphics g)
	{
		// Get an easy reference to the map.
		GameMap gameMap = myGame.gameMap;

		for(int w = 0; w < gameMap.mapWidth; ++w)
		{
			for(int h = 0; h < gameMap.mapHeight; ++h)
			{
				if(gameMap.getLocation(w,h) != null)
				{
					Location locus = gameMap.getLocation(w,h);
					if(locus.getResident() != null)
					{
						Unit unit = locus.getResident();
						int drawX = (int)( myView.getTileSize() * w);
						int drawY = (int)( myView.getTileSize() * h);
						SpriteLibrary.getUnitMapSpriteSet(locus.getResident()).drawUnitHP(g, myGame.activeCO,
								unit, drawX, drawY, drawScale);
					}
				}
			}
		}
	}
	
	/**
	 * Allows drawing of a single unit, at a specified real location.
	 * "Real" means that the specified x and y are that of the game's
	 * underlying data model, not of the draw-space.
	 */
	public void drawUnit(Graphics g, Unit unit, double x, double y)
	{
		int drawX = (int)( myView.getTileSize() * x);
		int drawY = (int)( myView.getTileSize() * y);
		SpriteLibrary.getUnitMapSpriteSet(unit).drawUnit(g, myGame.activeCO, unit, /*currentAction,*/
				currentAnimIndex, drawX, drawY, drawScale, myView.getFlipUnitFacing(unit.CO));
	}

	public int updateSpriteIndex()
	{
		// Calculate the sprite index to use.
		long thisTime = System.currentTimeMillis();
		long timeDiff = thisTime - lastIndexUpdateTime;

		// If it's time to update the sprite index... update the sprite index.
		if(timeDiff > indexUpdateTime)
		{
			currentAnimIndex++;
			lastIndexUpdateTime = thisTime;
		}

		return currentAnimIndex;
	}
}
