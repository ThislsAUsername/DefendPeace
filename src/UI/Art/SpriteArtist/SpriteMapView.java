package UI.Art.SpriteArtist;

import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Terrain.Environment;
import Terrain.GameMap;
import UI.MapView;
import UI.Art.SpriteArtist.SpriteMapArtist;
import UI.Art.FillRectArtist.FillRectMenuArtist;
import UI.Art.SpriteArtist.SpriteUnitArtist;

public class SpriteMapView extends MapView
{
	private static final long serialVersionUID = 1L;

	private HashMap<Commander, Boolean> unitFacings;

	public SpriteMapView(GameInstance game)
	{
		super(new SpriteMapArtist(game), new SpriteUnitArtist(game), new FillRectMenuArtist(game));

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
}
