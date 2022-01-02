package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;

import AI.AICombatUtils;
import Engine.Army;
import Engine.GameInstance;
import Engine.GamePath;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapPerspective;
import UI.GameOverlay;
import UI.MapView;
import UI.SlidingValue;
import UI.UIUtils;
import UI.Art.Animation.BaseUnitActionAnimation;
import UI.Art.Animation.GameAnimation;
import UI.Art.Animation.GameEndAnimation;
import UI.Art.Animation.NoAnimation;
import UI.Art.Animation.NobunagaBattleAnimation;
import UI.Art.Animation.ResupplyAnimation;
import UI.Art.Animation.TurnInitAnimation;
import UI.Art.SpriteArtist.SpriteOptions.SelectedUnitThreatAreaMode;
import UI.Art.SpriteArtist.Backgrounds.DiagonalBlindsBG;
import UI.Art.Animation.AirDropAnimation;
import UI.Art.Animation.MoveAnimation;
import Units.Unit;
import Units.UnitContext;
import Units.WeaponModel;

public class SpriteMapView extends MapView
{
  private GameInstance myGame;

  // Local map buffer to simplify drawing for sub-artists. Game assets are drawn
  // onto their absolute locations on this image, and then the relevant portion
  // of this image is drawn to the screen.
  private BufferedImage mapImage = null;

  private MapArtist mapArtist;
  private UnitArtist unitArtist;
  private MenuArtist menuArtist;

  // Overlay management variables.
  private boolean overlayIsLeft = true;
  final int COMPREHENSIVE_HUD_H_SIZE;

  // Variables for controlling map animations.
  protected Queue<GameEvent> eventsToAnimate = new GameEventQueue();
  private static final int animIndexUpdateInterval = 250;

  // Separate animation speed for "active" things (e.g. units moving).
  private static final int fastAnimIndexUpdateInterval = 125;
  private final BaseUnitActionAnimation contemplationAnim = new BaseUnitActionAnimation(0, null, null);

  /** Width of the visible space in pixels. */
  private int mapViewWidth;
  /** Height of the visible space in pixels. */
  private int mapViewHeight;

  // The number of map tiles to draw. This corresponds to the size of the game window.
  private int mapTilesToDrawX;
  private int mapTilesToDrawY;
  // Coordinates of the draw view, with double precision. Will constantly move towards (mapViewX, mapViewY).
  private SlidingValue mapViewDrawX;
  private SlidingValue mapViewDrawY;

  boolean dimensionsChanged = false; // If the window is resized, don't bother sliding the view into place; just snap.

  public SpriteMapView(GameInstance game)
  {
    // Create an initial image that can contain the entire map.
    mapImage = SpriteLibrary.createDefaultBlankSprite(
        SpriteLibrary.baseSpriteSize * game.gameMap.mapWidth,
        SpriteLibrary.baseSpriteSize * game.gameMap.mapHeight);

    mapArtist = new MapArtist(game);
    MapTileDetailsArtist.register(game);
    unitArtist = new UnitArtist(game);
    menuArtist = new MenuArtist(game, this);

    myGame = game;
    COMPREHENSIVE_HUD_H_SIZE = SpriteLibrary.getCoOverlay(myGame.activeArmy.cos[0], true).getWidth() + 42;

    // Start the view at the top-left by default.
    mapViewDrawX = new SlidingValue(0);
    mapViewDrawY = new SlidingValue(0);

    // Set the view to show the whole map, if possible
    mapViewWidth = SpriteLibrary.baseSpriteSize * game.gameMap.mapWidth;
    mapViewHeight = SpriteLibrary.baseSpriteSize * game.gameMap.mapHeight;
    SpriteOptions.setScreenDimensions(mapViewWidth * SpriteOptions.getDrawScale(), mapViewHeight * SpriteOptions.getDrawScale());
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return new Dimension(mapViewWidth * SpriteOptions.getDrawScale(), mapViewHeight * SpriteOptions.getDrawScale());
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    // The user wants to use a specific amount of screen. Figure out how many tiles to draw.
    int drawScale = SpriteOptions.getDrawScale();
    mapViewWidth = width / drawScale;
    mapViewHeight = height / drawScale;
    int tileSize = SpriteLibrary.baseSpriteSize;
    mapTilesToDrawX = mapViewWidth / tileSize;
    mapTilesToDrawY = mapViewHeight / tileSize;

    // Let SpriteOptions know we are changing things.
    SpriteOptions.setScreenDimensions(width, height);

    dimensionsChanged = true; // Let render() know that the window was resized.
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
      boolean isEventHidden = !(null == event.getStartPoint()) && gameMap.isLocationFogged(event.getStartPoint())
          && gameMap.isLocationFogged(event.getEndPoint());

      currentAnimation = event.getEventAnimation(this);
      if( SpriteOptions.getAnimationsEnabled() && (null != currentAnimation)
          && !isEventHidden)
      {
        if(event.getEndPoint() != null)
        {
          myGame.setCursorLocation(event.getEndPoint());
        }
      }
      else
        // If we want to animate something hidden, or we don't have anything to animate, animate nothing instead.
        currentAnimation = new NoAnimation();
    }
  }

  private void renderCurrentAnimation(Graphics g, boolean notifyControllerOnEnd)
  {
    // Animate until it tells you it's done.
    if( currentAnimation.animate(g) )
    {
      currentAnimation = null;

      // The animation is over; remove the corresponding event and notify the controller.
      if( notifyControllerOnEnd ) // ...but only if we're animating events
        mapController.animationEnded(eventsToAnimate.poll(), eventsToAnimate.isEmpty());

      // Get the next event animation if one exists.
      loadNextEventAnimation();
    }
  }

  /**
   * Draw the background, the map, and any visible units/map animations/etc.
   * @param mapGraphics The Graphics object for the Map image. Drawn elements
   * can be drawn based on map-tile locations.
   */
  private BufferedImage renderMap()
  {
    final MapPerspective gameMap = getDrawableMap(myGame);
    
    // We draw in three stages. First, we draw the map/units onto a canvas which is the size
    // of the entire map; then we copy the visible section of that canvas onto a screen-sized
    // image, then draw the overlay and scale that composite as we draw it to the window.
    final Graphics mapGraphics = mapImage.getGraphics();

    // Make sure the view is centered where we want it.
    adjustViewLocation();

    // Draw the portion of the base terrain that is currently in-window.
    final int drawMultiplier = SpriteLibrary.baseSpriteSize;
    int drawX = (int) (mapViewDrawX.get() * drawMultiplier);
    int drawY = (int) (mapViewDrawY.get() * drawMultiplier);

    // Make sure we specify draw coordinates that are valid per the underlying map image.
    final int maxDrawX = mapImage.getWidth() - mapViewWidth;
    final int maxDrawY = mapImage.getHeight() - mapViewHeight;
    if( drawX > maxDrawX )
      drawX = maxDrawX;
    if( drawX < 0 )
      drawX = 0;
    if( drawY > maxDrawY )
      drawY = maxDrawY;
    if( drawY < 0 )
      drawY = 0;

    // Get a reference to the current action being built, if one exists.
    final Unit currentActor = mapController.getContemplatedActor();
    final XYCoord actorCoord = mapController.getContemplationCoord();
    final XYCoord cursorCoord = myGame.getCursorCoord();
    boolean notifyOnAnimEnd = true;
    if( null != currentActor && null == currentAnimation ) // Draw the currently-acting unit so it's on top of everything.
    {
      currentAnimation = contemplationAnim.update(drawMultiplier, currentActor, actorCoord);
      notifyOnAnimEnd = false;
    }
    final GamePath currentPath = mapController.getContemplatedMove();
    final boolean isTargeting = mapController.isTargeting();

    // Start actually drawing things
    mapArtist.drawBaseTerrain(mapGraphics, gameMap, drawX, drawY, mapViewWidth, mapViewHeight);

    // Update the central sprite indices so animations happen in sync.
    final int animIndex = getAnimIndex();

    // Draw units, buildings, trees, etc.
    drawUnitsAndMapObjects(mapGraphics, gameMap, animIndex);

    ArrayList<GameOverlay> overlays = new ArrayList<GameOverlay>();
    // Apply any relevant map highlights.
    for( Army army : myGame.armies )
    {
      overlays.addAll(army.getMyOverlays(gameMap, army == gameMap.viewer));
    }

    // Highlight current available selection targets
    Collection<XYCoord> options = mapController.getSelectableCoords();
    if( null != options && !options.isEmpty() )
    {
      overlays.add(new GameOverlay(null, options, OverlayArtist.HIGHLIGHT_COLOR, OverlayArtist.HIGHLIGHT_COLOR));
    }

    // Highlight our currently-selected unit's range on top of everything else
    if( null != currentPath && null != currentActor && !mapController.isTargeting() )
    {
      SelectedUnitThreatAreaMode threatMode = SpriteOptions.getSelectedUnitThreatAreaMode();
      for( WeaponModel w : currentActor.model.weapons )
      {
        // Display what we can shoot next turn...
        if( threatMode == SelectedUnitThreatAreaMode.All
            || threatMode == SelectedUnitThreatAreaMode.Future )
          overlays.add(new GameOverlay(null,
                       AICombatUtils.findThreatPower(gameMap, currentActor, cursorCoord, null).keySet(),
                       OverlayArtist.FIRE_FILL, OverlayArtist.LATER_FIRE_EDGE));

        // ...and this turn's targets on top
        if( threatMode == SelectedUnitThreatAreaMode.All
            || threatMode == SelectedUnitThreatAreaMode.Current )
          if( w.canFireAfterMoving || currentPath.getPathLength() == 1 )
          {
            UnitContext uc = new UnitContext(gameMap, currentActor, w, currentPath, cursorCoord);
            overlays.add(new GameOverlay(null,
                         Utils.findLocationsInRange(gameMap, cursorCoord,
                                                    (1 == uc.rangeMin)? 0 : uc.rangeMin, uc.rangeMax),
                         OverlayArtist.FIRE_FILL, OverlayArtist.NOW_FIRE_EDGE));
          }

      } // ~per-weapon loop
    }
    OverlayArtist.drawHighlights(mapGraphics, gameMap, overlays,
                                 drawX, drawY,
                                 mapViewWidth, mapViewHeight,
                                 drawMultiplier,
                                 null != currentPath, cursorCoord);

    // Draw icons on top of everything, to make sure they are seen clearly.
    drawStatusIcons(mapGraphics, gameMap, animIndex);

    // Draw the movement arrow if the user is contemplating a move/action (but not once the action commences).
    if( null != currentPath )
    {
      mapArtist.drawMovePath(mapGraphics, mapController.getContemplatedMove());
    }

    if( currentAnimation != null && currentAnimation.isMapAnimation() )
    {
      renderCurrentAnimation(mapGraphics, notifyOnAnimEnd);
    }

    // Keep track of whether we want to draw tile info.
    boolean showTileDetails = false;

    // Draw the cursor/our menu if we aren't animating events
    if( currentAnimation == null || !notifyOnAnimEnd )
    {
      showTileDetails = true; // Only draw tile details when not animating.
      if( getCurrentGameMenu() == null )
      {
        mapArtist.drawCursor(mapGraphics, currentActor, isTargeting, myGame.getCursorX(), myGame.getCursorY());
      }
      else
      {
        menuArtist.drawMenu(mapGraphics, mapViewDrawX.geti(), mapViewDrawY.geti());
      }
    }

    for( DamagePopup popup : mapController.getDamagePopups() )
      drawDamagePreview(mapGraphics, popup, gameMap.isLocationEmpty(popup.coords));

    // Decide where to draw the map in the window
    int deltaX = 0, deltaY = 0;
    if (mapViewWidth > mapImage.getWidth())
    {
      int rightJustified = (mapViewWidth - mapImage.getWidth());
      int centered = rightJustified/2;

      // Until we hit COMPREHENSIVE_HUD_H_SIZE, stick to the right
      deltaX = Math.min(rightJustified, COMPREHENSIVE_HUD_H_SIZE);
      // After that, try to be centered
      deltaX = Math.max(deltaX, centered);
    }
    if (mapViewHeight > mapImage.getHeight())
      deltaY = (mapViewHeight - mapImage.getHeight())/2;

    // Copy the relevant section of the map image onto a screen-sized image buffer.
    Dimension dims = SpriteOptions.getScreenDimensions();
    int drawScale = SpriteOptions.getDrawScale();
    BufferedImage screenImage = SpriteLibrary.createTransparentSprite(dims.width/drawScale, dims.height/drawScale);
    Graphics screenGraphics = screenImage.getGraphics();

    int drawWidth  = Math.min(mapViewWidth,  mapImage.getWidth());
    int drawHeight = Math.min(mapViewHeight, mapImage.getHeight());

    // First four coords are the dest x,y,x2,y2. Next four are the source coords.
    screenGraphics.drawImage(mapImage, deltaX, deltaY, (deltaX + drawWidth), (deltaY + drawHeight),
                                       drawX,  drawY,  (drawX  + drawWidth), (drawY  + drawHeight), null);

    // Draw the Commander overlay with available funds.
    drawHUD(screenGraphics, showTileDetails);

    return screenImage;
  }

  @Override
  public void render(Graphics g)
  {
    if( null == currentAnimation || currentAnimation.isMapVisible() )
    {
      DiagonalBlindsBG.draw(g);
      BufferedImage screenImage = renderMap();
      int drawScale = SpriteOptions.getDrawScale();
      g.drawImage(screenImage, 0, 0, screenImage.getWidth()*drawScale, screenImage.getHeight()*drawScale, null);
    }

    // Map animations are handled in the map-drawing code. Screen animations are covered here.
    if( null != currentAnimation && !currentAnimation.isMapAnimation() )
    {
      renderCurrentAnimation(g, true);
    }
  }

  private void adjustViewLocation()
  {
    GameMap gameMap = getDrawableMap(myGame);
    int curX = myGame.getCursorX();
    int curY = myGame.getCursorY();

    // Maintain a 2-space buffer between the cursor and the edge of the visible map, when possible.
    int buffer = 2; // Note the cursor takes up one space, so we will have to add 1 when checking the right/bottom border.
    int mapViewX = mapViewDrawX.getDestination();
    int mapViewY = mapViewDrawY.getDestination();
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
      mapViewDrawX.snap(mapViewX);
      mapViewDrawY.snap(mapViewY);
    }
    else
    {
      mapViewDrawX.set(mapViewX);
      mapViewDrawY.set(mapViewY);
    }
  }

  @Override // from MapView
  public GameAnimation buildBattleAnimation(BattleSummary summary)
  {
    return new NobunagaBattleAnimation(
        SpriteLibrary.baseSpriteSize, summary.attacker.unit,
        summary.attacker.unit.x, summary.attacker.unit.y,
        summary.defender.unit.x, summary.defender.unit.y);
  }

  @Override // from MapView
  public GameAnimation buildDemolitionAnimation( StrikeParams params, XYCoord target, int damage )
  {
    return new NobunagaBattleAnimation(SpriteLibrary.baseSpriteSize, params.attacker.unit, params.attacker.coord.xCoord, params.attacker.coord.yCoord, target.xCoord, target.yCoord);
  }

  @Override // from MapView
  public GameAnimation buildMoveAnimation(Unit unit, GamePath movePath)
  {
    return new MoveAnimation(SpriteLibrary.baseSpriteSize, myGame.gameMap, unit, movePath);
  }

  @Override // from MapView
  public GameAnimation buildAirdropAnimation( Unit unit, XYCoord start, XYCoord end, Unit obstacle )
  {
    return new AirDropAnimation(SpriteLibrary.baseSpriteSize, unit, start, end);
  }

  @Override // from MapView
  public GameAnimation buildTeleportAnimation( Unit unit, XYCoord start, XYCoord end, Unit obstacle )
  {
    return null;
  }

  @Override
  public GameAnimation buildTurnInitAnimation( Army cmdr, int turn, boolean hideMap, Collection<String> message )
  {
    boolean requireButton = hideMap && !cmdr.isAI();
    return new TurnInitAnimation(cmdr, turn, hideMap, requireButton, message);
  }

  @Override // from MapView
  public GameAnimation buildResupplyAnimation(Unit supplier, Unit unit)
  {
    return new ResupplyAnimation(SpriteLibrary.baseSpriteSize, supplier, unit.x, unit.y);
  }

  /**
   * Draws all units and map objects in order from left to right, top to bottom,
   * to ensure that they are layered correctly (near things are drawn on top of far
   * things, and units are drawn on top of terrain objects).
   * NOTE: Does not draw the currently-active unit, if one exists; that will
   * be drawn later so it is more visible, and so it can be animated separately.
   */
  private void drawUnitsAndMapObjects(Graphics g, GameMap gameMap, int animIndex)
  {
    // Draw terrain objects and units in order so they overlap correctly.
    // Only bother iterating over the visible map space (plus a 2-square border).
    int drawY = (int) mapViewDrawY.get();
    int drawX = (int) mapViewDrawX.get();
    ArrayList<Unit> actors = null;
    if( null != currentAnimation )
      actors = currentAnimation.getActors();
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
            if( null == actors || !actors.contains(resident) )
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
  public void drawStatusIcons(Graphics g, GameMap gameMap, int animIndex)
  {
    ArrayList<Unit> actors = null;
    if( null != currentAnimation )
      actors = currentAnimation.getActors();
    for( int y = 0; y < gameMap.mapHeight; ++y )
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        if( !gameMap.isLocationEmpty(x, y) )
        {
          Unit resident = gameMap.getLocation(x, y).getResident();
          // If an action is being considered, draw the active unit later, not now.
          if( null == actors || !actors.contains(resident) )
          {
            unitArtist.drawUnitIcons(g, resident, resident.x, resident.y, animIndex);
            MarkArtist.drawMark(g, myGame, resident, animIndex);
          }
        }
        else
          MarkArtist.drawMark(g, myGame, new XYCoord(x, y), animIndex);
      }
    }
  }

  /**
   * Draws a predicted effect panel; not necessarily damage
   */
  public void drawDamagePreview(Graphics g, DamagePopup data, boolean spaceEmpty)
  {
    // Build a display of the expected damage.
    Color[] colors = UIUtils.getMapUnitColors(data.color).paletteColors;
    BufferedImage dmgImage = SpriteUIUtils.makeTextFrame(colors[4], colors[2], data.quantity, 2, 2);

    // Draw the damage estimate directly above the unit being targeted.
    int tileSize = SpriteLibrary.baseSpriteSize;
    int estimateX = (data.coords.xCoord * tileSize) + (tileSize / 2);
    int estimateY = Math.max((data.coords.yCoord * tileSize) - dmgImage.getHeight() / 2, dmgImage.getHeight() / 2); // Don't want it floating off-screen
    SpriteUIUtils.drawImageCenteredOnPoint(g, dmgImage, estimateX, estimateY);

    if( spaceEmpty && 0 != data.coords.yCoord )
    {
      int arrowY = (data.coords.yCoord * tileSize) + (tileSize / 2) - 1; // break the bottom border of the text frame
      SpriteUIUtils.drawImageCenteredOnPoint(g, SpriteLibrary.getPreviewArrow(data.color), estimateX, arrowY);
    }
  }

  /**
   * Fetch the index which determines the frame that is drawn for map animations.
   */
  public static int getAnimIndex()
  {
    // Calculate the sprite index to use.
    long thisTime = System.currentTimeMillis();
    // Fun fact: casting long->int can produce negative numbers, for some reason.
    return Math.abs((int) (thisTime / animIndexUpdateInterval));
  }
  public static int getFastAnimIndex()
  {
    // Calculate the sprite index to use.
    long thisTime = System.currentTimeMillis();
    // Fun fact: casting long->int can produce negative numbers, for some reason.
    return Math.abs((int) (thisTime / fastAnimIndexUpdateInterval));
  }
  public static boolean shouldFlip(Unit u)
  {
    return u.CO.faction.flip;
  }

  private void drawHUD(Graphics g, boolean includeTileDetails)
  {
    // Choose the CO overlay location based on the cursor location on the screen.
    if( !overlayIsLeft && (myGame.getCursorX() - mapViewDrawX.get()) > (mapTilesToDrawX - 1) * 3 / 5 )
    {
      overlayIsLeft = true;
    }
    if( overlayIsLeft && (myGame.getCursorX() - mapViewDrawX.get()) < mapTilesToDrawX * 2 / 5 )
    {
      overlayIsLeft = false;
    }

    // If the CO overlay won't overlap the map, draw all CO overlays to use the space
    final int overlayHSpaceAvailable = mapViewWidth - mapImage.getWidth();
    final boolean drawAllHeaders = overlayHSpaceAvailable > COMPREHENSIVE_HUD_H_SIZE;

    if( drawAllHeaders )
      overlayIsLeft = true;

    drawTurnCounter(g, overlayIsLeft);
    int headerOffset = turnNumImage.getHeight() + 6;

    if( drawAllHeaders )
    {
      BufferedImage coOverlays = CommanderOverlayArtist.drawAllCommanderOverlays(
          myGame.armies,
          getDrawableMap(myGame),
          COMPREHENSIVE_HUD_H_SIZE, mapViewHeight, myGame.activeArmy);
      if( overlayIsLeft )
        g.drawImage(coOverlays, 0, headerOffset, null);
      else
        g.drawImage(coOverlays, mapViewWidth - coOverlays.getWidth(), headerOffset, null);
    }
    else
      CommanderOverlayArtist.drawCommanderOverlay(g, myGame.activeArmy, headerOffset, overlayIsLeft);

    // Draw terrain defense and unit status.
    if( includeTileDetails )
      MapTileDetailsArtist.drawTileDetails(g, myGame.activeArmy.myView, myGame.getCursorCoord(), overlayIsLeft);
  }

  private int lastTurnNum = -1;
  private BufferedImage turnNumImage;
  private void drawTurnCounter(Graphics g, boolean overlayIsLeft)
  {
    // Generate the turn-counter image.
    int turnNum = myGame.getCurrentTurn();
    if( lastTurnNum != turnNum )
    {
      lastTurnNum = turnNum;
      final PixelFont pf = SpriteLibrary.getFontStandard();
      final int wordHeight = pf.getAscent()+pf.getDescent();

      // Our final image will contain word+digit
      final BufferedImage word = SpriteUIUtils.getTextAsImage("Turn ");
      final BufferedImage digit = SpriteUIUtils.getBoldTextAsImage(Integer.toString(turnNum));

      final int width = word.getWidth() + digit.getWidth();
      final int height = Math.max(wordHeight, digit.getHeight());

      turnNumImage = SpriteLibrary.createTransparentSprite(width, height);
      Graphics dcg = turnNumImage.getGraphics();

      // Note that the word currently has no descender characters
      int wordVOffset = height-wordHeight+pf.getDescent();
      dcg.drawImage(word, 0, wordVOffset, null);

      // Bottom-justify the digit, to match with the word
      final int digitVOffset = height - digit.getHeight();
      dcg.drawImage(digit, turnNumImage.getWidth()-digit.getWidth(), digitVOffset, null);
    }

    // Draw the turn counter.
    int xDraw = overlayIsLeft ?
        2
        : (SpriteOptions.getScreenDimensions().width / SpriteOptions.getDrawScale()) - 2 - turnNumImage.getWidth();
    int yDraw = 3;

    // Draw a CO-colored background with the counter.
    int arcW = turnNumImage.getHeight()+4;
    g.setColor(UIUtils.getMapUnitColors(myGame.activeArmy.cos[0].myColor).paletteColors[5]); // 0 is darker, 5 is lighter.
    g.fillArc(xDraw - (arcW/2), yDraw-2, arcW, arcW-1, 90, 180);
    g.fillArc(xDraw + turnNumImage.getWidth()-(arcW/2), yDraw-2, arcW, arcW-1, -90, 180);
    g.fillRect(xDraw, yDraw-1, turnNumImage.getWidth()+1, turnNumImage.getHeight()+2);
    g.setColor(Color.BLACK);
    g.drawArc(xDraw - (arcW/2), yDraw-2, arcW, arcW-1, 90, 180);
    g.drawArc(xDraw + turnNumImage.getWidth()-(arcW/2), yDraw-2, arcW, arcW-1, -90, 180);
    g.drawLine(xDraw, yDraw-2, xDraw + turnNumImage.getWidth(), yDraw-2);
    g.drawLine(xDraw, yDraw+turnNumImage.getHeight()+1, xDraw + turnNumImage.getWidth(), yDraw+turnNumImage.getHeight()+1);
    g.drawImage(turnNumImage, xDraw, yDraw, null);
  }

  /**
   * To be called once all but one faction has been eliminated.
   * Animates the victory/defeat overlay.
   */
  @Override
  public void gameIsOver()
  {
    if( currentAnimation != null )
    {
      // Delete the previous animation if one exists (which it shouldn't).
      currentAnimation.cancel();
      currentAnimation = null;
    }

    // Create a new animation to show the game results.
    currentAnimation = new GameEndAnimation(myGame.armies);
  }

  @Override
  public void cleanup()
  {
    mapArtist.cleanup();
    mapArtist = null;
    unitArtist = null;
    menuArtist = null;
  }
}
