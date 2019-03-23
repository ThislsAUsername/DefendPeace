package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Queue;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.Path;
import Engine.Utils;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatEngine;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import UI.CO_InfoMenu;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import UI.Art.Animation.NoAnimation;
import UI.Art.Animation.NobunagaBattleAnimation;
import UI.Art.Animation.ResupplyAnimation;
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

  boolean dimensionsChanged = false; // If the window is resized, don't bother sliding the view into place; just snap.

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
  public void setPreferredDimensions(int width, int height)
  {
    // The user wants to use a specific amount of screen. Figure out how many tiles to draw for them.
    mapViewWidth = width;
    mapViewHeight = height;
    int tileSize = SpriteLibrary.baseSpriteSize * SpriteOptions.getDrawScale();
    mapTilesToDrawX = mapViewWidth / tileSize;
    mapTilesToDrawY = mapViewHeight / tileSize;

    // Let SpriteOptions know we are changing things.
    SpriteOptions.setScreenDimensions(mapViewWidth, mapViewHeight);

    dimensionsChanged = true; // Let render() know that the window was resized.
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
    if( unitFacings.containsKey(co) ) // Make sure we don't try to assign null to a boolean.
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
    unitFacings.put(co, co.HQLocation.xCoord >= map.mapWidth / 2);
  }

  @Override
  public void animate(GameEventQueue newEvents)
  {
    if( null != newEvents )
    {
      eventsToAnimate.addAll(newEvents);

      // If we aren't currently animating anything, load up the next animation.
      if( null == currentAnimation )
      {
        loadNextEventAnimation();
      }
      if( null == currentAnimation )
      {
        // Nothing to animate. Release control.
        mapController.animationEnded(null, true);
      }
    }
    else
    {
      mapController.animationEnded(null, true);
    }
  }

  /**
   * Utility function to get the animation for the next animatable GameEvent
   * in the GameEvent queue.
   */
  private void loadNextEventAnimation()
  {
    GameMap gameMap = getDrawableMap(myGame);

    // Keep pulling events off the queue until we get one we can draw.
    while (null == currentAnimation && !eventsToAnimate.isEmpty())
    {
      GameEvent event = eventsToAnimate.peek();
      currentAnimation = event.getEventAnimation(this);
      boolean isEventHidden = !(null == event.getStartPoint()) && gameMap.isLocationFogged(event.getStartPoint())
          && gameMap.isLocationFogged(event.getEndPoint());
      if( null == currentAnimation || isEventHidden )
      {
        // If we want to animate something hidden, or we don't have anything to animate, animate nothing instead.
        currentAnimation = new NoAnimation();
      }
    }
  }

  @Override
  public void render(Graphics g)
  {
    GameMap gameMap = getDrawableMap(myGame);
    
    DiagonalBlindsBG.draw(g);
    // If we are in the CO_INFO menu, don't draw the map, etc.
    if( mapController.isInCoInfoMenu )
    {
      drawCOInfoMenu(g);
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
      int drawX = (int) (mapViewDrawX * drawMultiplier);
      int drawY = (int) (mapViewDrawY * drawMultiplier);

      // Make sure we specify draw coordinates that are valid per the underlying map image.
      int maxDrawX = mapImage.getWidth() - mapViewWidth;
      int maxDrawY = mapImage.getHeight() - mapViewHeight;
      if( drawX > maxDrawX )
        drawX = maxDrawX;
      if( drawX < 0 )
        drawX = 0;
      if( drawY > maxDrawY )
        drawY = maxDrawY;
      if( drawY < 0 )
        drawY = 0;

      mapArtist.drawBaseTerrain(mapGraphics, gameMap, drawX, drawY, mapViewWidth, mapViewHeight);

      // Update the central sprite indices so animations happen in sync.
      updateAnimationIndices();

      // Draw units, buildings, trees, etc.
      drawUnitsAndMapObjects(mapGraphics, gameMap);

      // Apply any relevant map highlight.
      mapArtist.drawHighlights(mapGraphics);

      // Draw Unit icons on top of everything, to make sure they are seen clearly.
      drawUnitIcons(mapGraphics, gameMap);

      // Get a reference to the current action being built, if one exists.
      Unit currentActor = mapController.getContemplatedActor();
      Path currentPath = mapController.getContemplatedMove();
      boolean isTargeting = mapController.isTargeting();

      // Draw the movement arrow if the user is contemplating a move/action (but not once the action commences).
      if( null != currentPath && null == currentAnimation )
      {
        mapArtist.drawMovePath(mapGraphics, mapController.getContemplatedMove());
      }

      // Draw the currently-acting unit so it's on top of everything.
      if( null != currentActor )
      {
        unitArtist.drawUnit(mapGraphics, currentActor, currentActor.x, currentActor.y, fastAnimIndex);
        unitArtist.drawUnitIcons(mapGraphics, currentActor, currentActor.x, currentActor.y, animIndex);
      }

      if( currentAnimation != null )
      {
        // Animate until it tells you it's done.
        if( currentAnimation.animate(mapGraphics) )
        {
          currentAnimation = null;

          // The animation is over; remove the corresponding event and notify the controller.
          mapController.animationEnded(eventsToAnimate.poll(), eventsToAnimate.isEmpty());

          // Get the next event animation if one exists.
          loadNextEventAnimation();
        }
      }
      else if( getCurrentGameMenu() == null )
      {
        mapArtist.drawCursor(mapGraphics, currentActor, isTargeting, myGame.getCursorX(), myGame.getCursorY());
        Unit target = myGame.gameMap.getLocation(myGame.getCursorX(), myGame.getCursorY()).getResident();
        if( isTargeting && null != currentPath && null != target)
        {
          drawDamagePreviewSet(mapGraphics, currentActor, currentPath, target);
        }
      }
      else
      {
        menuArtist.drawMenu(mapGraphics, mapViewX, mapViewY);
      }
      
      // When we draw the map, we want to center it if it's smaller than the view dimensions
      int deltaX = 0, deltaY = 0;
      if (mapViewWidth > mapImage.getWidth())
        deltaX = (mapViewWidth - mapImage.getWidth())/2;
      if (mapViewHeight > mapImage.getHeight())
        deltaY = (mapViewHeight - mapImage.getHeight())/2;

      // Copy the map image into the window's graphics buffer.
      // First four coords are the dest x,y,x2,y2. Next four are the source coords.      
      g.drawImage(mapImage, deltaX, deltaY, deltaX + mapViewWidth, deltaY + mapViewHeight, drawX, drawY, drawX + mapViewWidth, drawY + mapViewHeight, null);

      // Draw the Commander overlay with available funds.
      drawCommanderOverlay(g);
    } // End of case for no overlay menu.
  }

  private void drawCOInfoMenu(Graphics g)
  {
    // Get the CO info menu.
    CO_InfoMenu menu = mapController.getCoInfoMenu();
    int drawScale = SpriteOptions.getDrawScale();
    
    int paneOuterBuffer = 4*drawScale; // sets both outer border and frame border size
    int paneHSize = (int) (mapViewWidth*0.7) - paneOuterBuffer*4; // width of the BG color area
    int paneVSize = (int) (mapViewHeight   ) - paneOuterBuffer*4; // height ""

    // Get the current menu selections.
    int co = menu.getCoSelection();
    int page = menu.getPageSelection();

    // Draw the commander art. (the caller draws our background, so we don't have to)
    BufferedImage COPic = SpriteLibrary.getCommanderSprites(myGame.commanders[co].coInfo.name).body;
    // justify bottom/right
    g.drawImage(COPic, mapViewWidth - COPic.getWidth()*drawScale, mapViewHeight - COPic.getHeight()*drawScale,
        COPic.getWidth()*drawScale, COPic.getHeight()*drawScale, null);

    CommanderOverlayArtist.drawCommanderOverlay(g, myGame.commanders[co], false);
    
    // add the actual info
    g.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    g.fillRect(  paneOuterBuffer,   paneOuterBuffer, paneHSize + 2*paneOuterBuffer, paneVSize + 2*paneOuterBuffer);
    g.setColor(SpriteUIUtils.MENUBGCOLOR);
    g.fillRect(2*paneOuterBuffer, 2*paneOuterBuffer, paneHSize                    , paneVSize                    );
    
    // TODO: consider drawing this all as one big image, so the user can scroll smoothly through it regardless of screen size
    switch (myGame.commanders[co].coInfo.maker.infoPages.get(page).pageType)
    {
      case CO_HEADERS:
        int overlayHeight = 30*drawScale;
        int heightOffset = 0;
        for (Commander CO : myGame.commanders)
        {
          BufferedImage overlayPic = SpriteLibrary.createTransparentSprite(100*drawScale, overlayHeight);
          CommanderOverlayArtist.drawCommanderOverlay(overlayPic.getGraphics(), CO, true);
          g.drawImage(overlayPic,2*paneOuterBuffer, 2*paneOuterBuffer + heightOffset, null);
          heightOffset += overlayHeight;
        }
        break;
      case GAME_STATUS:
        String status = Utils.getGameStatusData(getDrawableMap(myGame), myGame.commanders[co]);
        BufferedImage statusText = SpriteUIUtils.paintTextNormalized(status, paneHSize-paneOuterBuffer);
        g.drawImage(statusText,3*paneOuterBuffer, 3*paneOuterBuffer, null);
        break;
      case BASIC:
        BufferedImage infoText = SpriteUIUtils.paintTextNormalized(myGame.commanders[co].coInfo.maker.infoPages.get(page).info, paneHSize-paneOuterBuffer);
        g.drawImage(infoText,3*paneOuterBuffer, 3*paneOuterBuffer, null);
        break;
    }
  }

  private void adjustViewLocation()
  {
    GameMap gameMap = getDrawableMap(myGame);
    int curX = myGame.getCursorX();
    int curY = myGame.getCursorY();

    // Maintain a 2-space buffer between the cursor and the edge of the visible map, when possible.
    int buffer = 2; // Note the cursor takes up one space, so we will have to add 1 when checking the right/bottom border.
    if( (mapViewX + mapTilesToDrawX) < (curX + buffer + 1) )
    {
      mapViewX = curX - mapTilesToDrawX + buffer + 1; // Move our view to keep the cursor in sight.
    }
    else if( (curX - buffer) < mapViewX )
    {
      mapViewX = curX - buffer; // Move our view to keep the cursor in sight.
    }

    // Now do the y-axis.
    if( (curY + buffer + 1) >= (mapViewY + mapTilesToDrawY) )
    {
      mapViewY = curY - mapTilesToDrawY + buffer + 1; // Move our view to keep the cursor in sight.
    }
    else if( (curY - buffer) < mapViewY )
    {
      mapViewY = curY - buffer; // Move our view to keep the cursor in sight.
    }

    // Pin the view to the edge of the map (i.e., don't show dead
    // space unless the window is larger than the map).
    int maxViewX = gameMap.mapWidth - mapTilesToDrawX;
    int maxViewY = gameMap.mapHeight - mapTilesToDrawY;
    if( mapViewX > maxViewX )
      mapViewX = maxViewX;
    if( mapViewX < 0 )
      mapViewX = 0;
    if( mapViewY > maxViewY )
      mapViewY = maxViewY;
    if( mapViewY < 0 )
      mapViewY = 0;

    // Recalculate the precise draw location for the view.
    if( dimensionsChanged )
    {
      // If the window was resized, don't slide the view into
      // place, just snap it to where it belongs.
      dimensionsChanged = false;
      mapViewDrawX = mapViewX;
      mapViewDrawY = mapViewY;
    }
    else
    {
      if( mapViewDrawX != mapViewX )
      {
        mapViewDrawX += SpriteUIUtils.calculateSlideAmount(mapViewDrawX, mapViewX);
      }
      if( mapViewDrawY != mapViewY )
      {
        mapViewDrawY += SpriteUIUtils.calculateSlideAmount(mapViewDrawY, mapViewY);
      }
    }
  }

  @Override // from MapView
  public GameAnimation buildBattleAnimation(BattleSummary summary)
  {
    return new NobunagaBattleAnimation(getTileSize(), summary.attacker.x, summary.attacker.y, summary.defender.x,
        summary.defender.y);
  }

  @Override
  // from MapView
  public GameAnimation buildMoveAnimation(Unit unit, Path movePath)
  {
    return new NobunagaBattleAnimation(getTileSize(), movePath.getWaypoint(0).x, movePath.getWaypoint(0).y, movePath.getEnd().x,
        movePath.getEnd().y);
  }

  @Override // from MapView
  public GameAnimation buildResupplyAnimation(Unit unit)
  {
    return new ResupplyAnimation(unit.x, unit.y);
  }

  /**
   * Draws all units and map objects in order from left to right, top to bottom,
   * to ensure that they are layered correctly (near things are drawn on top of far
   * things, and units are drawn on top of terrain objects).
   * NOTE: Does not draw the currently-active unit, if one exists; that will
   * be drawn later so it is more visible, and so it can be animated.
   */
  private void drawUnitsAndMapObjects(Graphics g, GameMap gameMap)
  {
    // Draw terrain objects and units in order so they overlap correctly.
    // Only bother iterating over the visible map space (plus a 2-square border).
    int drawY = (int) mapViewDrawY;
    int drawX = (int) mapViewDrawX;
    for( int y = drawY - 1; y < drawY + mapTilesToDrawY + 2; ++y )
    {
      for( int x = drawX - 1; x < drawX + mapTilesToDrawX + 2; ++x )
      {
        // Since we are trying to draw a ring of objects around the viewable space to
        // ensure smooth scrolling, make sure we aren't running off the edge of the map.
        if( gameMap.isLocationValid(x, y) )
        {
          // Draw any terrain object here, followed by any unit present (provided it's not under fog).
          mapArtist.drawTerrainObject(g, gameMap, x, y);
          if( !gameMap.isLocationEmpty(x, y) )
          {
            Unit resident = gameMap.getLocation(x, y).getResident();
            // If an action is being considered, draw the active unit later, not now.
            Unit currentActor = mapController.getContemplatedActor();
            if( resident != currentActor )
            {
              unitArtist.drawUnit(g, resident, resident.x, resident.y, animIndex);
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
  public void drawUnitIcons(Graphics g, GameMap gameMap)
  {
    for( int y = 0; y < gameMap.mapHeight; ++y )
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        if( !gameMap.isLocationEmpty(x, y) )
        {
          Unit resident = gameMap.getLocation(x, y).getResident();
          // If an action is being considered, draw the active unit later, not now.
          Unit currentActor = mapController.getContemplatedActor();
          if( resident != currentActor )
          {
            unitArtist.drawUnitIcons(g, resident, resident.x, resident.y, animIndex);
          }
        }
      }
    }
  }
  
  /**
   * Draws the predicted damage for both attacker and defender, as applicable.
   */
  public void drawDamagePreviewSet(Graphics g, Unit currentActor, Path currentPath, Unit target)
  {
    int dist = Math.abs(target.x - currentPath.getEnd().x) + Math.abs(target.y - currentPath.getEnd().y);
    if( currentActor.canAttack(target.model, dist, currentPath.getPathLength() > 1) )
    {
      // find out how it would go
      BattleSummary summary = CombatEngine.simulateBattleResults(currentActor, target, getDrawableMap(myGame),
          currentPath.getEnd().x, currentPath.getEnd().y);
      // draw any damage done, with the color of the one dealing the damage
      if( summary.attackerHPLoss > 0 )
        drawDamagePreview(g, summary.attackerHPLoss, summary.defender.CO, summary.attacker.x, summary.attacker.y);
      if( summary.defenderHPLoss > 0 )
        drawDamagePreview(g, summary.defenderHPLoss, summary.attacker.CO, summary.defender.x, summary.defender.y);
    }
  }
  public void drawDamagePreview(Graphics g, double damage, Commander attacker, int x, int y)
  {
    // grab the two most significant digits and convert to %
    String damageText = (int) (damage*10) + "%";

    // Build a display of the expected damage.
    Color[] colors = SpriteLibrary.getMapUnitColors(attacker.myColor).paletteColors;
    BufferedImage dmgImage = SpriteUIUtils.makeTextFrame(colors[4], colors[2], damageText,
        2 * SpriteOptions.getDrawScale(), 2 * SpriteOptions.getDrawScale());

    // Draw the damage estimate directly above the unit being targeted.
    int drawScale = SpriteOptions.getDrawScale();
    int tileSize = SpriteLibrary.baseSpriteSize * drawScale;
    int estimateX = (x * tileSize) + (tileSize / 2);
    int estimateY = (y * tileSize) - dmgImage.getHeight() / 2;
    SpriteLibrary.drawImageCenteredOnPoint(g, dmgImage, estimateX, estimateY, 1);
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
    // Choose the CO overlay location based on the cursor location on the screen.
    if( !overlayIsLeft && (myGame.getCursorX() - mapViewX) > (mapTilesToDrawX - 1) * 3 / 5 )
    {
      overlayIsLeft = true;
    }
    if( overlayIsLeft && (myGame.getCursorX() - mapViewX) < mapTilesToDrawX * 2 / 5 )
    {
      overlayIsLeft = false;
    }

    CommanderOverlayArtist.drawCommanderOverlay(g, myGame.activeCO, overlayIsLeft);
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
