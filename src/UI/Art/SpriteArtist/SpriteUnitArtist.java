package UI.Art.SpriteArtist;

import java.awt.Graphics;

import Engine.GameInstance;
import Terrain.GameMap;
import Terrain.Location;
import UI.MapView;
import UI.Art.UnitArtist;
import Units.Unit;

public class SpriteUnitArtist implements UnitArtist
{
	private GameInstance myGame;
	private SpriteMapView myView;
	int drawScale;
	
	// Variables for controlling unit map animations.
	private int currentAnimIndex = 0;
	private long lastIndexUpdateTime = 0;
	private final double indexUpdateTime = 250;

	public SpriteUnitArtist( GameInstance game )
	{
		myGame = game;
	}
	
	@Override
	public void setView(MapView view)
	{
		myView = (SpriteMapView)view;
		// Figure out how to scale the sprites we draw.
		drawScale = view.getTileSize() / SpriteLibrary.baseSpriteSize;
	}

	@Override
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
						Unit unit = locus.getResident();
						int drawX = myView.getTileSize() * w;
						int drawY = myView.getTileSize() * h;
						SpriteLibrary.getUnitMapSpriteSet(unit).drawUnit(g, myGame.activeCO, unit, /*currentAction,*/
								currentAnimIndex, drawX, drawY, drawScale, myView.getFlipUnitFacing(unit.CO));
					}
				}
			}
		}
	}
	
	private int updateSpriteIndex()
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
