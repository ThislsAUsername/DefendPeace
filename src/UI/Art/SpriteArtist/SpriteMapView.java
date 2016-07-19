package UI.Art.SpriteArtist;

import java.awt.Dimension;
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
  private HashMap<Commander, Boolean> unitFacings;

  private GameInstance myGame;

  private SpriteMapArtist mapArtist;
  private SpriteUnitArtist unitArtist;
  private SpriteMenuArtist menuArtist;

  // Overlay management variables.
  private boolean overlayIsLeft = true;
  private String overlayFundsString = "FUNDS     0";
  private int overlayPreviousFunds = 0;

  // Variables for controlling map animations.
  private int currentAnimIndex = 0;
  private long lastAnimIndexUpdateTime = 0;
  private final int animIndexUpdateInterval = 250;

  private int mapViewWidth;
  private int mapViewHeight;

  public SpriteMapView(GameInstance game)
  {
    mapArtist = new SpriteMapArtist(game, this);
    unitArtist = new SpriteUnitArtist(game, this);
    menuArtist = new SpriteMenuArtist(game, this);

    myGame = game;
    unitFacings = new HashMap<Commander, Boolean>();

    // Locally store which direction each CO should be facing.
    for( CommandingOfficers.Commander co : game.commanders )
    {
      setCommanderUnitFacing(co, game.gameMap);
    }

    // By default, we will show a 15x10 chunk of the map.
    mapViewWidth = SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale() * 15;
    mapViewHeight = SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale() * 10;
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return new Dimension(mapViewWidth, mapViewHeight);
  }

  @Override
  public int getTileSize()
  {
    return SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale();
  }

  @Override
  public int getViewWidth()
  {
    return mapViewWidth;
  }

  @Override
  public int getViewHeight()
  {
    return mapViewHeight;
  }

  /** Returns whether the commander's map units should be flipped horizontally when drawn. */
  public boolean getFlipUnitFacing(Commander co)
  {
    boolean flip = false;
    if(unitFacings.containsKey(co)) // Make sure we don't try to assign null to a boolean.
    {
      flip = unitFacings.get(co);
    }
    return flip;
  }

  /**
   * Set the facing direction of the CO based on the location of the HQ. If the
   * HQ is on the left side of the map, the units should face right, and vice versa.
   * @param co
   * @param map
   */
  private void setCommanderUnitFacing(CommandingOfficers.Commander co, GameMap map)
  {
    for( int x = 0; x < map.mapWidth; ++x )
    {
      for( int y = 0; y < map.mapHeight; ++y )
      {
        if( map.getEnvironment(x, y).terrainType == Environment.Terrains.HQ && map.getLocation(x, y).getOwner() == co )
        {
          unitFacings.put(co, x >= map.mapWidth / 2);
        }
      }
    }
  }

  @Override
  public void render(Graphics g)
  {
    // Draw base terrain
    mapArtist.drawBaseTerrain(g);

    // Draw units, buildings, trees, etc.
    drawUnitsAndMapObjects(g);

    // Draw Unit HP icons on top of everything, to make sure they are seen clearly.
    unitArtist.drawUnitHPIcons(g);

    // Apply any relevant map highlight.
    mapArtist.drawHighlights(g);

    // TODO: Consider moving the contemplated move inside of the action (in MapController)
    //       to make the interface more consistent?
    // Draw the movement arrow if the user is contemplating a move.
    if( mapController.getContemplatedMove() != null )
    {
      mapArtist.drawMovePath(g, mapController.getContemplatedMove());
    }
    // Draw the movement arrow if the user is contemplating an action (but not once the action commences).
    if( null != currentAction && null != currentAction.getMovePath() && null == currentAnimation )
    {
      mapArtist.drawMovePath(g, currentAction.getMovePath());
    }
    // Draw the acting unit so it's on top of everything.
    if( null != currentAction ) // && currentAnimation == null) // If the unit should animate when acting.
    {
      Unit u = currentAction.getActor();
      unitArtist.drawUnit(g, u, u.x, u.y, currentAnimIndex);
    }

    if( currentAnimation != null )
    {
      // Animate until it tells you it's done.
      if( currentAnimation.animate(g) )
      {
        currentAction = null;
        currentAnimation = null;
        mapController.animationEnded();
      }
    }
    else if( currentMenu == null )
    {
      mapArtist.drawCursor(g);
    }
    else
    {
      menuArtist.drawMenu(g);
    }

    // Draw the Commander overlay with available funds.
    drawCommanderOverlay(g);
  }

  /**
   * Increments the current map animation index, and draws all units and map objects in order
   *   from left to right, top to bottom, ensuring that they are layered correctly (near things
   *   are always drawn on top of far things).
   */
  private void drawUnitsAndMapObjects(Graphics g)
  {
    // Update the central sprite index so animations happen in sync.
    updateSpriteIndex();

    // Draw terrain objects and units in order so they overlap correctly.
    for( int y = 0; y < myGame.gameMap.mapHeight; ++y )
    {
      for( int x = 0; x < myGame.gameMap.mapWidth; ++x )
      {
        // Draw any terrain object here, followed by any unit present.
        mapArtist.drawTerrainObject(g, x, y);
        if( !myGame.gameMap.isLocationEmpty(x, y) )
        {
          Unit u = myGame.gameMap.getLocation(x, y).getResident();
          // If an action is being considered, draw the active unit later, not now.
          if( (null == currentAction) || (u != currentAction.getActor()) )
          {
            unitArtist.drawUnit(g, u, u.x, u.y, currentAnimIndex);
          }
        }
      }
    }
  }

  /**
   * Updates the index which determines the frame that is drawn for map animations.
   */
  private void updateSpriteIndex()
  {
    // Calculate the sprite index to use.
    long thisTime = System.currentTimeMillis();
    long timeDiff = thisTime - lastAnimIndexUpdateTime;

    // If it's time to update the sprite index... update the sprite index.
    if( timeDiff > animIndexUpdateInterval )
    {
      currentAnimIndex++;
      lastAnimIndexUpdateTime = thisTime;
    }
  }

  /**
   * Draws the commander overlay, with the commander name and available funds.
   * @param g
   */
  private void drawCommanderOverlay(Graphics g)
  {
    // TODO: Move CO overlay based on the cursor location on the screen,
    // rather than the cursor location on the map.
    if( !overlayIsLeft && myGame.getCursorX() > (myGame.gameMap.mapWidth - 1) * 3 / 5 )
    {
      overlayIsLeft = true;
    }
    if( overlayIsLeft && myGame.getCursorX() < myGame.gameMap.mapWidth * 2 / 5 )
    {
      overlayIsLeft = false;
    }

    int drawScale = SpriteOptions.getDrawScale();
    int coEyesWidth = 25;
    int xTextOffset = (4+coEyesWidth) * drawScale; // Distance from the side of the view to the CO overlay text.
    int yTextOffset = 3 * drawScale; // Distance from the top of the view to the CO overlay text.
    BufferedImage spriteA = SpriteLibrary.getLettersSmallCaps().getFrame(0); // Convenient reference so we can check dimensions.
    int textHeight = spriteA.getHeight() * drawScale;

    // Rebuild the funds string to draw if it has changed.
    if( overlayPreviousFunds != myGame.activeCO.money )
    {
      overlayPreviousFunds = myGame.activeCO.money;
      overlayFundsString = buildFundsString(overlayPreviousFunds);
    }

    String coString = myGame.activeCO.coInfo.name;

    // Choose left or right overlay image to draw.
    BufferedImage overlayImage = SpriteLibrary.getCoOverlay(myGame.activeCO, overlayIsLeft);

    if( overlayIsLeft )
    { // Draw the overlay on the left side.
      g.drawImage(overlayImage, 0, 0, overlayImage.getWidth() * drawScale, overlayImage.getHeight() * drawScale, null);
      SpriteLibrary.drawTextSmallCaps(g, coString, xTextOffset, yTextOffset, drawScale); // CO name
      SpriteLibrary.drawTextSmallCaps(g, overlayFundsString, xTextOffset, textHeight + drawScale + yTextOffset, drawScale); // Funds
    }
    else
    { // Draw the overlay on the right side.
      int xPos = getViewWidth() - overlayImage.getWidth() * drawScale;
      int coNameXPos = getViewWidth() - spriteA.getWidth() * drawScale * coString.length() - xTextOffset;
      int fundsXPos = getViewWidth() - spriteA.getWidth() * drawScale * overlayFundsString.length() - xTextOffset;
      g.drawImage(overlayImage, xPos, 0, overlayImage.getWidth() * drawScale, overlayImage.getHeight() * drawScale, null);
      SpriteLibrary.drawTextSmallCaps(g, coString, coNameXPos, yTextOffset, drawScale); // CO name
      SpriteLibrary.drawTextSmallCaps(g, overlayFundsString, fundsXPos, textHeight + drawScale + yTextOffset, drawScale); // Funds
    }
  }

  /**
   * Constructs a fixed-width (padded as needed) 11-character string to be drawn in the commander overlay.
   * @param funds The number to convert to an HUD overlay funds string.
   * @return A string of the form "FUNDS XXXXX" where X is either a space or a digit.
   */
  private String buildFundsString(int funds)
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
