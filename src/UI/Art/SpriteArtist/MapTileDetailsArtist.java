package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

import Engine.GameEvents.GameEventListener.CacheInvalidationListener;
import Engine.GameInstance;
import Engine.XYCoord;
import Terrain.MapLocation;
import Terrain.MapPerspective;
import Terrain.TerrainType;
import UI.UnitMarker;
import UI.Art.SpriteArtist.MarkArtist.MarkingCache;
import UI.UnitMarker.CustomStatData;
import Units.Unit;

/**
 * Generates an overlay image to show details about the unit and terrain under the cursor.
 */
public class MapTileDetailsArtist
{
  private static XYCoord currentTile = new XYCoord(-1, -1);
  private static BufferedImage tileOverlay;
  private static MtdaListener mtdaListener = new MtdaListener();

  public static void register(GameInstance gi)
  {
    mtdaListener.registerForEvents(gi);
  }

  public static void resetOverlay()
  {
    currentTile = new XYCoord(-1, -1);
  }

  public static void drawTileDetails(Graphics g, MapPerspective map, XYCoord tileToDetail, boolean overlayIsLeft)
  {
    // Rebuild the overlay image if needed.
    generateOverlay(map, tileToDetail);

    int edgeBuffer = 2;
    int mapViewHeight = SpriteOptions.getScreenDimensions().height / SpriteOptions.getDrawScale();
    int mapViewWidth = SpriteOptions.getScreenDimensions().width / SpriteOptions.getDrawScale();
    if( overlayIsLeft )
    { // Draw the overlay on the left side.
      int drawX = edgeBuffer;
      int drawY = mapViewHeight - edgeBuffer - tileOverlay.getHeight();
      g.drawImage(tileOverlay, drawX, drawY, null);
    }
    else
    { // Draw the overlay on the right side.
      int drawX = mapViewWidth - edgeBuffer - tileOverlay.getWidth();
      int drawY = mapViewHeight - edgeBuffer - tileOverlay.getHeight();
      g.drawImage(tileOverlay, drawX, drawY, null);
    }
  }

  private static void generateOverlay(MapPerspective map, XYCoord coord)
  {
    if( currentTile.equals(coord) )
      return; // Did this already; just use the cached image.

    // Define useful quantities.
    int tileSize = SpriteLibrary.baseSpriteSize;
    int iconSize = SpriteLibrary.baseSpriteSize/2;
    MapLocation loc = map.getLocation(coord);
    TerrainType terrain = loc.getEnvironment().terrainType;
    Unit unit = loc.getResident();

    // Get the terrain image to draw.
    TerrainSpriteSet tss = SpriteLibrary.getTerrainSpriteSet(loc);
    int terrainSubIndex = TerrainSpriteSet.getTileVariation(coord.x, coord.y);
    BufferedImage terrainSprite = tss.getTerrainSprite().getFrame(terrainSubIndex);

    // Collect terrain attributes to draw.
    ArrayList<AttributeArtist> terrainAttrs = new ArrayList<AttributeArtist>();
    terrainAttrs.add(new AttributeArtist(SpriteLibrary.MapIcons.SHIELD.getIcon(), terrain.getDefLevel()));
    if( loc.durability < 99 ) terrainAttrs.add(new AttributeArtist(SpriteLibrary.MapIcons.HEART.getIcon(), loc.durability));

    // Collect any unit and its cargo
    ArrayList<ArrayList<AttributeArtist>> unitAttrCache = new ArrayList<>();
    ArrayList<BufferedImage> unitImageCache = new ArrayList<>();
    if( null != unit )
    {
      addDetailInfoFor(unitImageCache, unitAttrCache,
                       map, coord, unit);
    }

    ///////////////////////////////////////////////////////////////
    // Calculate the size of the panel.
    int bufferPx = 3;
    //            left buf          attr icon               tile     right buffer
    int columnW = bufferPx + iconSize+ATTR_TEXT_SPACING + tileSize + bufferPx*2;
    int panelW = columnW * (unitImageCache.size() + 1);
    if( unitImageCache.size() > 0 )
      panelW += bufferPx*2; // The panel gets N-1 right buffers

    int numAttrs = terrainAttrs.size();
    for( ArrayList<AttributeArtist> attrList : unitAttrCache )
      numAttrs = Math.max(numAttrs, attrList.size());
    //         upper buffer   tile   lower buffer   each attribute with a 1-px buffer
    int panelH = iconSize + tileSize + bufferPx  +  numAttrs*(iconSize+1);

    // Create the overlay image.
    tileOverlay = SpriteLibrary.createTransparentSprite(panelW, panelH);
    Graphics ltog = tileOverlay.getGraphics();

    // Draw the semi-transparent panel backing.
    ltog.setColor(new Color(0, 0, 0, 100));
    //ltog.fillRect(0, 0, tileOverlay.getWidth(), tileOverlay.getHeight());
    ltog.fillRoundRect(0, 0, tileOverlay.getWidth(), tileOverlay.getHeight(), bufferPx*2, bufferPx*2);

    // Match the buffers from above
    int drawX = bufferPx;
    int drawY = iconSize;

    // Draw all the terrain stuff.
    drawColumn(ltog, terrainSprite, terrainAttrs, drawX, drawY, false);

    // Draw all the unit stuff.
    for( int i = 0; i < unitImageCache.size(); i++ )
    {
      drawX += columnW;
      drawColumn(ltog, unitImageCache.get(i), unitAttrCache.get(i), drawX, drawY, true);
    }

    currentTile = coord;
  }

  private static void addDetailInfoFor(ArrayList<BufferedImage> unitImageCache,
                                       ArrayList<ArrayList<AttributeArtist>> unitAttrCache,
                                       MapPerspective map, XYCoord coord, Unit unit)
  {
    UnitSpriteSet uss = SpriteLibrary.getMapUnitSpriteSet(unit);
    unitImageCache.add(uss.getUnitImage());
    ArrayList<AttributeArtist> unitAttrs = new ArrayList<>();
    unitAttrCache.add(unitAttrs);

    unitAttrs.add(new AttributeArtist(SpriteLibrary.MapIcons.HEART.getIcon(), unit.getHP()));
    if( unit.model.needsFuel() )
      unitAttrs.add(new AttributeArtist(SpriteLibrary.MapIcons.FUEL.getIcon(), unit.fuel));
    if( unit.ammo >= 0 )
      unitAttrs.add(new AttributeArtist(SpriteLibrary.MapIcons.AMMO.getIcon(), unit.ammo));
    if( unit.model.maxMaterials > 0 )
      unitAttrs.add(new AttributeArtist(SpriteLibrary.getMaterialIcon(unit.CO.myColor), unit.materials));
    if( unit.getCaptureProgress() > 0 && null != coord )
      unitAttrs.add(new AttributeArtist(SpriteLibrary.getCaptureIcon(unit.CO.myColor),
          map.getEnvironment(coord).terrainType.getCaptureThreshold()-unit.getCaptureProgress()));

    final MarkingCache cache = MarkingCache.instance(map.game);
    cache.setupMarkers();
    for( UnitMarker m : cache.markers )
    {
      CustomStatData stat = m.getCustomStat(unit);
      if (null == stat)
        continue;
      BufferedImage symbol = SpriteLibrary.getColoredMapTextSprites(stat.markColor).get(stat.mark);
      unitAttrs.add(new AttributeArtist(symbol, stat.textColor, stat.text));
    }
    boolean fogOn = map.game.isFogEnabled();
    boolean hideCargo = fogOn && unit.CO.isEnemy(map.viewer);
    if( hideCargo )
      return;
    // Also display cargo via DFS
    for( Unit cargo : unit.heldUnits)
      addDetailInfoFor(unitImageCache, unitAttrCache,
                       map, null, cargo);
  }

  private static void drawColumn(Graphics g, BufferedImage image, ArrayList<AttributeArtist> attrs, int drawX, int drawY, boolean centerImage)
  {
    int tileSize = SpriteLibrary.baseSpriteSize;
    int iconSize = SpriteLibrary.baseSpriteSize/2;
    int imageShiftX = iconSize + ATTR_TEXT_SPACING; // Scoot it over to align with the text
    // Make sure the main image sits where it would relative to its place in the map/tile
    if( centerImage )
      imageShiftX  += (tileSize - image.getWidth() )/2;
    else
      imageShiftX  += (tileSize - image.getWidth() );
    int imageShiftY = (tileSize - image.getHeight());
    g.drawImage(image, drawX + imageShiftX, drawY + imageShiftY, null);
    drawY += SpriteLibrary.baseSpriteSize;
    for( AttributeArtist aa : attrs )
    {
      drawY++;
      aa.draw(g, drawX, drawY);
      drawY += iconSize;
    }
  }

  private static final int ATTR_TEXT_SPACING = 2;
  private static final int ATTR_TEXT_COUNT   = 3;
  private static class AttributeArtist
  {
    private BufferedImage icon, text;
    public AttributeArtist(BufferedImage image, Integer value)
    {
      this(image, Color.white, ""+Math.min(99, value));
    }
    public AttributeArtist(BufferedImage image, Color textColor, String value)
    {
      icon = image;
      Map<Character, BufferedImage> allChars = SpriteLibrary.getColoredMapTextSprites(textColor);
      text = allChars.get('A');
      int charWidth = text.getWidth();
      text = SpriteLibrary.createTransparentSprite(charWidth*ATTR_TEXT_COUNT, text.getHeight());
      Graphics g = text.getGraphics();

      int drawX = 0, drawY = 0;
      int maxIter = Math.min(2, value.length());
      drawX += charWidth*(2-maxIter); // Pre-cook our spacing
      for( int i = 0; i < maxIter; ++i )
      {
        char foundChar = value.charAt(i);
        BufferedImage digit = allChars.getOrDefault(foundChar, null);
        if( null != digit )
          g.drawImage(digit, drawX, drawY, null);
        drawX += charWidth;
      }
    }
    public void draw(Graphics g, int drawX, int drawY)
    {
      g.drawImage(icon, drawX, drawY, null);
      drawX += icon.getWidth() + ATTR_TEXT_SPACING;
      g.drawImage(text, drawX, drawY, null);
    }
  }

  /** This class just listens for any event that could change what is under the cursor, which is pretty much all of them. */
  private static class MtdaListener implements CacheInvalidationListener
  {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean shouldSerialize() { return false; }

    @Override
    public void InvalidateCache() { MapTileDetailsArtist.resetOverlay(); }
  }
}
