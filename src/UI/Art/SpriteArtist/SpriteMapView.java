package UI.Art.SpriteArtist;

import java.awt.Graphics;
import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Terrain.Environment;
import Terrain.GameMap;
import UI.MapView;
import Units.Unit;

public class SpriteMapView extends MapView
{
	private static final long serialVersionUID = 1L;

	private HashMap<Commander, Boolean> unitFacings;

	private GameMap gameMap;

	public SpriteMapView(GameInstance game)
	{
		super(new SpriteMapArtist(game), new SpriteUnitArtist(game), new SpriteMenuArtist(game));

		gameMap = game.gameMap;
		unitFacings = new HashMap<Commander, Boolean>();

		// Locally store which direction each CO should be facing.
		for(CommandingOfficers.Commander co : game.commanders)
		{
			setCommanderUnitFacing(co, game.gameMap);
		}
	}

	/** Returns whether the commander's map units should be flipped horizontally when drawn. */
	public boolean getFlipUnitFacing( Commander co )
	{
		return unitFacings.get(co);
	}

	/**
	 * Set the facing direction of the CO based on the location of the HQ. If the
	 * HQ is on the left side of the map, the units should face right, and vice versa.
	 * @param co
	 * @param map
	 */
	private void setCommanderUnitFacing(CommandingOfficers.Commander co, GameMap map)
	{
		for(int x = 0; x < map.mapWidth; ++x)
		{
			for(int y = 0; y < map.mapHeight; ++y)
			{
				if(map.getEnvironment(x, y).terrainType == Environment.Terrains.HQ &&
						map.getLocation(x, y).getOwner() == co)
				{
					unitFacings.put(co, x >= map.mapWidth/2);
				}
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// Cast the MapArtist to the SpriteMapArtist it should be.
		SpriteMapArtist sMapArtist = (SpriteMapArtist)mapArtist;

		// Draw base terrain
		sMapArtist.drawMap(g);

		// Draw terrain objects and units in order so they overlap correctly.
		for(int y = 0; y < gameMap.mapHeight; ++y)
		{
			for(int x = 0; x < gameMap.mapWidth; ++x)
			{
				// Draw any terrain object here, followed by any unit present.
				sMapArtist.drawTerrainObject(g, x, y);
				if(!gameMap.isLocationEmpty(x, y))
				{
					Unit u = gameMap.getLocation(x, y).getResident();
					unitArtist.drawUnit(g, u, u.x, u.y);
				}
			}
		}
		// Draw Unit HP icons on top of everything, to make sure they are seen clearly.
		((SpriteUnitArtist)unitArtist).drawUnitHPIcons(g);

		// Apply any relevant map highlight.
		// TODO: Move highlight underneath units? OR, change highlight to not matter.
		sMapArtist.drawHighlights(g);

		if(mapController.getContemplatedMove() != null)
		{
			sMapArtist.drawMovePath(g, mapController.getContemplatedMove());
		}

		if(currentAnimation != null)
		{
			// Animate until it tells you it's done.
			if(currentAnimation.animate(g))
			{
				currentAction = null;
				currentAnimation = null;
				mapController.animationEnded();
			}
		}
		else if (currentMenu == null)
		{
			sMapArtist.drawCursor(g);
		}
		else
		{
			menuArtist.drawMenu(g);
		}
	}
}
