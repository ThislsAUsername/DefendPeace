package UI.Art.SpriteArtist;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
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

	private GameInstance myGame;

	// Overlay management variables.
	private boolean overlayIsLeft = true;
	private String overlayFundsString = "FUNDS     0";
	private int overlayPreviousFunds = 0;

	public SpriteMapView(GameInstance game)
	{
		super(new SpriteMapArtist(game), new SpriteUnitArtist(game), new SpriteMenuArtist(game));

		myGame = game;
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
		for(int y = 0; y < myGame.gameMap.mapHeight; ++y)
		{
			for(int x = 0; x < myGame.gameMap.mapWidth; ++x)
			{
				// Draw any terrain object here, followed by any unit present.
				sMapArtist.drawTerrainObject(g, x, y);
				if(!myGame.gameMap.isLocationEmpty(x, y))
				{
					Unit u = myGame.gameMap.getLocation(x, y).getResident();
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

		// Draw the Commander overlay with available funds.
		drawCommanderOverlay(g);
	}

	/**
	 * Draws the commander overlay, with the commander name and available funds.
	 * @param g
	 */
	private void drawCommanderOverlay(Graphics g)
	{
		// TODO: Switch overlay location based on cursor location
		// on screen, rather than cursor location on map.
		if( !overlayIsLeft && myGame.getCursorX() > (myGame.gameMap.mapWidth-1)*3/5 )
		{
			overlayIsLeft = true;
		}
		if( overlayIsLeft && myGame.getCursorX() < myGame.gameMap.mapWidth*2/5 )
		{
			overlayIsLeft = false;
		}

		int drawScale = getDrawScale();
		int xTextOffset = 4*drawScale; // Distance from the side of the view to the CO overlay text.
		int yTextOffset = 3*drawScale; // Distance from the top of the view to the CO overlay text.
		BufferedImage spriteA = SpriteLibrary.getMenuLetters().getFrame(0); // Convenient reference so we can check dimensions.
		int textHeight = spriteA.getHeight()*drawScale;

		// Rebuild the funds string to draw if it has changed.
		if( overlayPreviousFunds != myGame.activeCO.money )
		{
			overlayPreviousFunds = myGame.activeCO.money;
			overlayFundsString = buildFundsString( overlayPreviousFunds );
		}

		String coString = "Cmdr Name"; // myGame.activeCO.toString();

		// Choose left or right overlay image to draw.
		BufferedImage overlayImage = SpriteLibrary.getCoOverlay(myGame.activeCO, overlayIsLeft);

		if( overlayIsLeft )
		{ // Draw the overlay on the left side.
			g.drawImage(overlayImage, 0, 0, overlayImage.getWidth()*drawScale, overlayImage.getHeight()*drawScale, null);
			SpriteLibrary.drawMenuText(g, coString, xTextOffset, yTextOffset, drawScale); // CO name
			SpriteLibrary.drawMenuText(g, overlayFundsString, xTextOffset, textHeight+drawScale+yTextOffset, drawScale); // Funds
		}
		else
		{ // Draw the overlay on the right side.
			int xPos = getViewWidth() - overlayImage.getWidth()*drawScale;
			int coNameXPos = getViewWidth() - spriteA.getWidth()*drawScale*coString.length() - xTextOffset;
			int fundsXPos = getViewWidth() - spriteA.getWidth()*drawScale*overlayFundsString.length() - xTextOffset;
			g.drawImage(overlayImage, xPos, 0, overlayImage.getWidth()*drawScale, overlayImage.getHeight()*drawScale, null);
			SpriteLibrary.drawMenuText(g, coString, coNameXPos, yTextOffset, drawScale); // CO name
			SpriteLibrary.drawMenuText(g, overlayFundsString, fundsXPos, textHeight+drawScale+yTextOffset, drawScale); // Funds
		}
	}

	/**
	 * Constructs a fixed-width (padded as needed) 11-character string to be drawn in the commander overlay.
	 * @param funds The number to convert to an HUD overlay funds string.
	 * @return A string of the form "FUNDS XXXXX" where X is either a space or a digit.
	 */
	private String buildFundsString( int funds )
	{
		StringBuilder sb = new StringBuilder("FUNDS ");
		if( myGame.activeCO.money < 10000 ) // Fewer than 5 digits
		{
			sb.append(" ");
		}
		if( myGame.activeCO.money < 1000 ) // Fewer than 4 digits
		{
			sb.append(" ");
		}
		if( myGame.activeCO.money < 100 ) // Fewer than 3 digits
		{
			sb.append(" ");
		}
		if( myGame.activeCO.money < 10 ) // Fewer than 2 digits. You poor.
		{
			sb.append(" ");
		}
		sb.append(Integer.toString(myGame.activeCO.money));

		return sb.toString();
	}
}
