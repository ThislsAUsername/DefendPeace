package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Terrain.Environment;
import Terrain.GameMap;
import UI.CO_InfoMenu;
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
  private int animIndex = 0;
  private long animIndexUpdateTime = 0;
  private final int animIndexUpdateInterval = 250;

  // Separate animation speed for "active" things (e.g. units moving).
  private int fastAnimIndex = 0;
  private long fastAnimIndexUpdateTime = 0;
  private final int fastAnimIndexUpdateInterval = 125;

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
    // If we are in the CO_INFO menu, don't draw the map, etc.
    if( mapController.isInCoInfoMenu )
    {
      // Get the CO info menu.
      CO_InfoMenu menu = mapController.getCoInfoMenu();

      // Get the current menu selections.
      int co = menu.getCoSelection();
      int page = menu.getPageSelection();

      // TODO: Create the other CO info pages (powers, stats, etc).

      // Draw the background.

      // Draw the commander art.
      g.drawImage(SpriteLibrary.getCommanderSprites(myGame.commanders[co].coInfo.cmdrEnum).body,
          0, 0, mapViewWidth, mapViewHeight, null);
    }
    else
    { // No overlay is being shown - draw the map, units, etc.
      // Draw base terrain
      mapArtist.drawBaseTerrain(g);

      // Update the central sprite indices so animations happen in sync.
      updateAnimationIndices();

      // Draw units, buildings, trees, etc.
      drawUnitsAndMapObjects(g);

      // Apply any relevant map highlight.
      mapArtist.drawHighlights(g);

      // Draw Unit icons on top of everything, to make sure they are seen clearly.
      drawUnitIcons(g);

      // TODO: Consider moving the contemplated move inside of the action (in MapController)
      //       to make the interface more consistent?
      // Draw the movement arrow if the user is contemplating a move.
      if( mapController.getContemplatedMove() != null )
      {
        mapArtist.drawMovePath(g, mapController.getContemplatedMove());
      }
      // Draw the movement arrow if the user is contemplating an action (but not once the action commences).
      else if( null != currentAction && null != currentAction.getMovePath() && null == currentAnimation )
      {
        mapArtist.drawMovePath(g, currentAction.getMovePath());
      }

      // Draw the currently-acting unit so it's on top of everything.
      if( null != currentAction )
      {
        Unit u = currentAction.getActor();
        unitArtist.drawUnit(g, u, u.x, u.y, fastAnimIndex);
        unitArtist.drawUnitIcons(g, u, u.x, u.y);
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
      else if( getCurrentGameMenu() == null )
      {
        mapArtist.drawCursor(g);
      }
      else
      {
        menuArtist.drawMenu(g);
      }

      // Draw the Commander overlay with available funds.
      drawCommanderOverlay(g);
    } // End of case for no overlay menu.
  }

  /**
   * Draws all units and map objects in order from left to right, top to bottom,
   * to ensure that they are layered correctly (near things are drawn on top of far
   * things, and units are drawn on top of terrain objects).
   * NOTE: Does not draw the currently-active unit, if one exists; that will
   * be drawn later so it is more visible, and so it can be animated.
   */
  private void drawUnitsAndMapObjects(Graphics g)
  {
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
            unitArtist.drawUnit(g, u, u.x, u.y, animIndex);
          }
        }
      }
    }
  }

  /**
   * Draws unit icons (HP, transport, etc) on top of units.
   * NOTE: Does not draw the unit icon for the currently-active unit, if
   * one is selected; this must be done separately.
   */
  public void drawUnitIcons(Graphics g)
  {
    // Get an easy reference to the map.
    GameMap gameMap = myGame.gameMap;

    for( int y = 0; y < gameMap.mapHeight; ++y )
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        if( !gameMap.isLocationEmpty(x, y) )
        {
          Unit u = myGame.gameMap.getLocation(x, y).getResident();
          // If an action is being considered, draw the active unit later, not now.
          if( (null == currentAction) || (u != currentAction.getActor()) )
          {
            unitArtist.drawUnitIcons(g, u, u.x, u.y);
          }
        }
      }
    }
  }

  /**
   * Updates the index which determines the frame that is drawn for map animations.
   */
  private void updateAnimationIndices()
  {
    // Calculate the sprite index to use.
    long thisTime = System.currentTimeMillis();
    long animTimeDiff = thisTime - animIndexUpdateTime;
    long fastAnimTimeDiff = thisTime - fastAnimIndexUpdateTime;

    // If it's time to update the sprite index... update the sprite index.
    if( animTimeDiff > animIndexUpdateInterval )
    {
      animIndex++;
      animIndexUpdateTime = thisTime;
    }

    // If it's time to update the fast sprite index... you know what to do.
    if( fastAnimTimeDiff > fastAnimIndexUpdateInterval )
    {
      fastAnimIndex++;
      fastAnimIndexUpdateTime = thisTime;
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
      int screenWidth = SpriteOptions.getScreenDimensions().width;
      int xPos = screenWidth - overlayImage.getWidth() * drawScale;
      int coNameXPos = screenWidth - spriteA.getWidth() * drawScale * coString.length() - xTextOffset;
      int fundsXPos = screenWidth - spriteA.getWidth() * drawScale * overlayFundsString.length() - xTextOffset;
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

  /**
   * To be called once all but one faction has been eliminated.
   * Animates the victory/defeat overlay.
   */
  public void gameIsOver()
  {
    if( currentAnimation != null )
    {
      // Delete the previous animation if one exists (which it shouldn't).
      currentAnimation.cancel();
      currentAnimation = null;
    }

    // Create a new animation to show the game results.
    currentAnimation = new SpriteGameEndAnimation(myGame.commanders);
  }
}
