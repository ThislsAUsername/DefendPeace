package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Queue;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.Path;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.Environment;
import Terrain.GameMap;
import UI.CO_InfoMenu;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import UI.Art.Animation.NobunagaBattleAnimation;
import Units.Unit;

public class SpriteMapView extends MapView
{
  private HashMap<Commander, Boolean> unitFacings;

  private GameInstance myGame;

  // Local map buffer to simplify drawing for sub-artists. Game assets are drawn
  // onto their absolute locations on this image, and then the relevant portion
  // of this image is drawn to the screen.
  private BufferedImage mapImage = null;

  private SpriteMapArtist mapArtist;
  private SpriteUnitArtist unitArtist;
  private SpriteMenuArtist menuArtist;

  // Overlay management variables.
  private boolean overlayIsLeft = true;
  private String overlayFundsString = "FUNDS     0";
  private int overlayPreviousFunds = 0;

  // Variables for controlling map animations.
  protected Queue<GameEvent> eventsToAnimate = new GameEventQueue();
  private int animIndex = 0;
  private long animIndexUpdateTime = 0;
  private final int animIndexUpdateInterval = 250;

  // Separate animation speed for "active" things (e.g. units moving).
  private int fastAnimIndex = 0;
  private long fastAnimIndexUpdateTime = 0;
  private final int fastAnimIndexUpdateInterval = 125;

  /** Width of the visible space in pixels. */
  private int mapViewWidth;
  /** Height of the visible space in pixels. */
  private int mapViewHeight;

  // The number of map tiles to draw. This corresponds to the size of the game window.
  private int mapTilesToDrawX;
  private int mapTilesToDrawY;
  // Coordinates of the upper-left-most, currently visible map location.
  private int mapViewX;
  private int mapViewY;
  // Coordinates of the draw view, with double precision. Will constantly move towards (mapViewX, mapViewY).
  private double mapViewDrawX;
  private double mapViewDrawY;

  public SpriteMapView(GameInstance game)
  {
    // Create an initial image that can contain the entire map.
    mapImage = SpriteLibrary.createDefaultBlankSprite(
        SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale() * game.gameMap.mapWidth,
        SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale() * game.gameMap.mapHeight);

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
    mapTilesToDrawX = 15;
    mapTilesToDrawY = 10;
    // Start the view at the top-left by default.
    mapViewX = 0;
    mapViewY = 0;
    mapViewDrawX = 0;
    mapViewDrawY = 0;

    mapViewWidth = SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale() * mapTilesToDrawX;
    mapViewHeight = SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale() * mapTilesToDrawY;
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
  public void animate( GameEventQueue newEvents )
  {
    System.out.println("DEBUG: Received " + newEvents.size() + " events to animate.");
    eventsToAnimate.addAll( newEvents );

    // If we aren't currently animating anything, load up the next animation.
    if( null == currentAnimation )
    {
      loadNextEventAnimation();
    }
  }

  /**
   * Utility function to get the animation for the next animatable GameEvent
   * in the GameEvent queue.
   */
  private void loadNextEventAnimation()
  {
    // Keep pulling events off the queue until we get one we can draw.
    while( null == currentAnimation && !eventsToAnimate.isEmpty() )
    {
      GameEvent event = eventsToAnimate.peek();
      if( null != event )
      {
        currentAnimation = event.getEventAnimation( this );
        if( null == currentAnimation )
        {
          // There isn't an animation for this event. Just notify the controller.
          mapController.animationEnded( eventsToAnimate.poll(), eventsToAnimate.isEmpty() );
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
    {
      // We draw in two stages. First, we draw the map/units onto a canvas which is the size
      // of the entire map; then we copy the visible section of that canvas onto the game window.
      // This allows us to avoid extra calculations to place map objects within in the window.
      Graphics mapGraphics = mapImage.getGraphics();

      // No overlay is being shown - draw the map, units, etc.
      // Make sure the view is centered where we want it.
      adjustViewLocation();

      // Draw the portion of the base terrain that is currently in-window.
      int drawMultiplier = SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale();
      int drawX = (int)(mapViewDrawX * drawMultiplier);
      int drawY = (int)(mapViewDrawY * drawMultiplier);
      System.out.println("Drawing at " + drawX + ", " + drawY);
      mapArtist.drawBaseTerrain(mapGraphics, drawX, drawY, mapViewWidth, mapViewHeight);

      // Update the central sprite indices so animations happen in sync.
      updateAnimationIndices();

      // Draw units, buildings, trees, etc.
      drawUnitsAndMapObjects(mapGraphics);

      // Apply any relevant map highlight.
      mapArtist.drawHighlights(mapGraphics);

      // Draw Unit icons on top of everything, to make sure they are seen clearly.
      drawUnitIcons(mapGraphics);

      // Get a reference to the current action being built, if one exists.
      GameAction currentAction = mapController.getContemplatedAction();

      // Draw the movement arrow if the user is contemplating a move.
      if( mapController.getContemplatedMove() != null )
      {
        mapArtist.drawMovePath(mapGraphics, mapController.getContemplatedMove());
      }
      // Draw the movement arrow if the user is contemplating an action (but not once the action commences).
      else if( null != currentAction && null != currentAction.getMovePath() && null == currentAnimation )
      {
        mapArtist.drawMovePath(mapGraphics, currentAction.getMovePath());
      }

      // Draw the currently-acting unit so it's on top of everything.
      if( null != currentAction && currentAnimation == null )
      {
        Unit u = currentAction.getActor();
        unitArtist.drawUnit(mapGraphics, u, u.x, u.y, fastAnimIndex);
        unitArtist.drawUnitIcons(mapGraphics, u, u.x, u.y);
      }

      if( currentAnimation != null )
      {
        // Animate until it tells you it's done.
        if( currentAnimation.animate(mapGraphics) )
        {
          currentAnimation = null;

          // The animation is over; remove the corresponding event and notify the controller.
          mapController.animationEnded( eventsToAnimate.poll(), eventsToAnimate.isEmpty() );

          // Get the next event animation if one exists.
          loadNextEventAnimation();
        }
      }
      else if( getCurrentGameMenu() == null )
      {
        mapArtist.drawCursor(mapGraphics, currentAction, myGame.getCursorX(), myGame.getCursorY());
      }
      else
      {
        menuArtist.drawMenu(mapGraphics, drawX, drawY);
      }

      // Copy the map image into the window's graphics buffer.
      // First four coords are the dest x,y,x2,y2. Next four are the source coords.
      g.drawImage(mapImage, 0, 0, mapViewWidth, mapViewHeight, drawX, drawY, drawX+mapViewWidth, drawY+mapViewHeight, null);

      // Draw the Commander overlay with available funds.
      drawCommanderOverlay(g);
    } // End of case for no overlay menu.
  }

  private void adjustViewLocation()
  {
    int curX = myGame.getCursorX();
    int curY = myGame.getCursorY();
    GameMap gameMap = myGame.gameMap;

    // Maintain a 2-space buffer between the cursor and the edge of the visible map, when possible.
    int buffer = 2; // Note the cursor takes up one space, so we will have to add 1 when checking the right/bottom border.
    if( (mapViewX + mapTilesToDrawX) < (curX + buffer+1) )
    {
      System.out.println("moving view right");
      mapViewX = curX - mapTilesToDrawX + buffer+1; // Move our view to keep the cursor in sight.
      // Make sure we don't try to move the view off the map.
      if( mapViewX + mapTilesToDrawX > gameMap.mapWidth ) mapViewX = gameMap.mapWidth - mapTilesToDrawX;
    }
    else if( (curX - buffer) < mapViewX )
    {
      System.out.println("moving view left");
      mapViewX = curX - buffer;
      if( mapViewX < 0 ) mapViewX = 0;
    }

    System.out.println("===========");
    System.out.println("mvy: " + mapViewY);
    System.out.println("cy : " + curY);
    // Now do the y-axis.
    if( (curY + buffer+1) >= (mapViewY + mapTilesToDrawY) )
    {
      System.out.println("moving view down");
      mapViewY = curY - mapTilesToDrawY + buffer+1;
      if( mapViewY + mapTilesToDrawY > gameMap.mapHeight ) mapViewY = gameMap.mapHeight - mapTilesToDrawY;
    }
    else if( (curY - buffer) < mapViewY )
    {
      System.out.println("moving view up");
      mapViewY = curY - buffer;
      if( mapViewY < 0 ) mapViewY = 0;
    }

    // Recalculate the precise draw location for the view.
    if( mapViewDrawX != mapViewX )
    {
      mapViewDrawX += SpriteUIUtils.calculateSlideAmount(mapViewDrawX, mapViewX);
    }
    if( mapViewDrawY != mapViewY )
    {
      mapViewDrawY += SpriteUIUtils.calculateSlideAmount(mapViewDrawY, mapViewY);
    }
  }

  @Override // from MapView
  public GameAnimation buildMoveAnimation( Unit unit, Path movePath )
  {
    return new NobunagaBattleAnimation(getTileSize(), movePath.getWaypoint(0).x, movePath.getWaypoint(0).y,
        movePath.getEnd().x, movePath.getEnd().y);
  }

  @Override // from MapView
  public GameAnimation buildBattleAnimation( BattleSummary summary )
  {
    return new NobunagaBattleAnimation(getTileSize(), summary.attacker.x, summary.attacker.y, summary.defender.x, summary.defender.y);
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
    // Only bother iterating over the visible map space (plus a 1-square border).
    for( int y = mapViewY-1; y < mapViewY+mapTilesToDrawY+1; ++y )
    {
      for( int x = mapViewX-1; x < mapViewX+mapTilesToDrawX+1; ++x )
      {
        // Since we are trying to draw a ring of objects around the viewable space to
        // ensure smooth scrolling, make sure we aren't running of the edge of the map.
        if(myGame.gameMap.isLocationValid(x, y))
        {
          // Draw any terrain object here, followed by any unit present.
          mapArtist.drawTerrainObject(g, x, y);
          if( !myGame.gameMap.isLocationEmpty(x, y) )
          {
            Unit u = myGame.gameMap.getLocation(x, y).getResident();
            // If an action is being considered, draw the active unit later, not now.
            GameAction currentAction = mapController.getContemplatedAction();
            if( (null == currentAction) || (u != currentAction.getActor()) )
            {
              unitArtist.drawUnit(g, u, u.x, u.y, animIndex);
            }
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
          GameAction currentAction = mapController.getContemplatedAction();
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
