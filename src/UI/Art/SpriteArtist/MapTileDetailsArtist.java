package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.MoveEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.GameEvents.MapChangeEvent.EnvironmentAssignment;
import Engine.UnitActionLifecycles.JoinLifecycle;
import Engine.UnitActionLifecycles.LoadLifecycle;
import Engine.UnitActionLifecycles.UnloadLifecycle;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitModel;

/**
 * Generates an overlay image to show details about the unit and terrain under the cursor.
 */
public class MapTileDetailsArtist
{
  private static XYCoord currentTile = new XYCoord(-1, -1);
  private static BufferedImage tileOverlay;
  private static MtdaListener mtdaListener = new MtdaListener();

  static
  {
    mtdaListener.registerForEvents();
  }

  public static void resetOverlay()
  {
    currentTile = new XYCoord(-1, -1);
  }

  public static void drawTileDetails(Graphics g, GameMap map, XYCoord tileToDetail, boolean overlayIsLeft)
  {
    // Rebuild the overlay image if needed.
    generateOverlay(map, tileToDetail);

    int edgeBuffer = 2;
    int mapViewHeight = SpriteOptions.getScreenDimensions().height / SpriteOptions.getDrawScale();
    if( overlayIsLeft )
    { // Draw the overlay on the left side.
      int drawX = edgeBuffer;
      int drawY = mapViewHeight - edgeBuffer - tileOverlay.getHeight();
      g.drawImage(tileOverlay, drawX, drawY, null);
    }
    else
    { // Draw the overlay on the right side.
      int mapViewWidth = SpriteOptions.getScreenDimensions().width / SpriteOptions.getDrawScale();
      int drawX = mapViewWidth - edgeBuffer - tileOverlay.getWidth();
      int drawY = mapViewHeight - edgeBuffer - tileOverlay.getHeight();
      g.drawImage(tileOverlay, drawX, drawY, null);
    }
  }

  private static void generateOverlay(GameMap map, XYCoord coord)
  {
    if( currentTile.equals(coord) )
      return; // Did this already; just use the cached image.

    // Define useful quantities.
    int tileSize = SpriteLibrary.baseSpriteSize;
    int iconSize = SpriteLibrary.baseSpriteSize/2;
    Location loc = map.getLocation(coord);
    TerrainType terrain = loc.getEnvironment().terrainType;
    boolean isTerrainObject = TerrainSpriteSet.isTerrainObject(terrain);
    Unit unit = loc.getResident();
    
    // Get the terrain image to draw.
    TerrainSpriteSet tss = SpriteLibrary.getTerrainSpriteSet(loc);
    int terrainSubIndex = TerrainSpriteSet.getTileVariation(coord.xCoord, coord.yCoord);
    BufferedImage terrainSprite = tss.getTerrainSprite().getFrame(terrainSubIndex);

    // Collect terrain attributes to draw.
    ArrayList<AttributeArtist> terrainAttrs = new ArrayList<AttributeArtist>();
    terrainAttrs.add(new AttributeArtist(SpriteLibrary.getShieldIcon(), terrain.getDefLevel()));
    if( loc.durability < 99 ) terrainAttrs.add(new AttributeArtist(SpriteLibrary.getHeartIcon(), loc.durability));
    
    // Get the unit image.
    ArrayList<AttributeArtist> unitAttrs = new ArrayList<AttributeArtist>();
    BufferedImage unitImage = null;
    if( null != unit )
    {
      UnitSpriteSet uss = SpriteLibrary.getMapUnitSpriteSet(unit);
      unitImage = uss.getUnitImage();
      
      unitAttrs.add(new AttributeArtist(SpriteLibrary.getHeartIcon(), unit.getHP()));
      unitAttrs.add(new AttributeArtist(SpriteLibrary.getFuelIcon(), unit.fuel));
      if( unit.ammo >= 0 ) 
        unitAttrs.add(new AttributeArtist(SpriteLibrary.getAmmoIcon(), unit.ammo));
      if( unit.getCaptureProgress() > 0)
        unitAttrs.add(new AttributeArtist(SpriteLibrary.getCaptureIcon(unit.CO.myColor), 20-unit.getCaptureProgress()));
    }

    ///////////////////////////////////////////////////////////////
    // Calculate the size of the panel.
    int terrainPanelW = tileSize * 3;
    int unitPanelW = (unitAttrs.isEmpty() ? 0 : (tileSize * 2));
    int panelW = terrainPanelW + unitPanelW;
    
    // Each attribute with a 1-px buffer, plus space for the tile itself.
    int numAttrs = Math.max(terrainAttrs.size(), unitAttrs.size());
    int bufferPx = 3;
    int panelH = numAttrs*iconSize+numAttrs+(tileSize*2)+bufferPx;

    // Create the overlay image.
    tileOverlay = SpriteLibrary.createTransparentSprite(panelW, panelH);
    Graphics ltog = tileOverlay.getGraphics();

    // Draw the semi-transparent panel backing.
    ltog.setColor(new Color(0, 0, 0, 100));
    //ltog.fillRect(0, 0, tileOverlay.getWidth(), tileOverlay.getHeight());
    ltog.fillRoundRect(0, 0, tileOverlay.getWidth(), tileOverlay.getHeight(), bufferPx*2, bufferPx*2);

    // Figure out where to draw.
    int drawX = isTerrainObject ? 0 : tileSize;
    int drawY = isTerrainObject ? 0 : tileSize;

    // Draw all the terrain stuff.
    drawColumn(ltog, terrainSprite, terrainAttrs, drawX, drawY, isTerrainObject ? iconSize : -iconSize);

    // Draw all the unit stuff.
    if( null != unitImage )
    {
      drawX = terrainPanelW;
      drawY = tileSize;
      drawColumn(ltog, unitImage, unitAttrs, drawX, drawY, -iconSize/2);
    }

    // Draw the tile coordinates.
    drawX = bufferPx;
    drawY = bufferPx;
    String coordStr = String.format("(%d, %d)", coord.xCoord, coord.yCoord);
    SpriteUIUtils.drawTextSmallCaps(ltog, coordStr, drawX, drawY);

    currentTile = coord;
  }

  private static void drawColumn(Graphics g, BufferedImage image, ArrayList<AttributeArtist> attrs, int drawX, int drawY, int bumpAmt)
  {
    int iconSize = SpriteLibrary.baseSpriteSize/2;
    g.drawImage(image, drawX, drawY, null);
    drawX += bumpAmt;
    drawY += image.getHeight();
    for( AttributeArtist aa : attrs )
    {
      drawY++;
      aa.draw(g, drawX, drawY);
      drawY += iconSize;
    }
  }

  private static class AttributeArtist
  {
    private BufferedImage icon;
    private Integer value;
    public AttributeArtist(BufferedImage image, Integer quantity)
    {
      icon = image;
      value = quantity;
    }
    public void draw(Graphics g, int drawX, int drawY)
    {
      g.drawImage(icon, drawX, drawY, null);
      drawX += icon.getWidth() + 2;
      Sprite numbers = SpriteLibrary.getMapUnitNumberSprites();
      int tensVal = (int)(value / 10);
      if( 0 != tensVal )
      {
        BufferedImage tens = numbers.getFrame(tensVal);
        g.drawImage(tens, drawX, drawY, null);
      }
      int onesVal = (int)(value % 10);
      BufferedImage ones = numbers.getFrame(onesVal);
      drawX += ones.getWidth();
      g.drawImage(ones, drawX, drawY, null);
    }
  }

  /** This class just listens for any event that could change what is under the cursor, which is pretty much all of them. */
  private static class MtdaListener extends GameEventListener
  {
    private static final long serialVersionUID = 1L;
    public void receiveBattleEvent(BattleSummary summary){MapTileDetailsArtist.resetOverlay();};
    public void receiveCreateUnitEvent(Unit unit){MapTileDetailsArtist.resetOverlay();};
    public void receiveCaptureEvent(Unit unit, Location location){MapTileDetailsArtist.resetOverlay();};
    public void receiveCommanderDefeatEvent(CommanderDefeatEvent event){MapTileDetailsArtist.resetOverlay();};
    public void receiveLoadEvent(LoadLifecycle.LoadEvent event){MapTileDetailsArtist.resetOverlay();};
    public void receiveMoveEvent(MoveEvent event){MapTileDetailsArtist.resetOverlay();};
    public void receiveTeleportEvent(Unit teleporter, XYCoord from, XYCoord to){MapTileDetailsArtist.resetOverlay();};
    public void receiveUnitJoinEvent(JoinLifecycle.JoinEvent event){MapTileDetailsArtist.resetOverlay();};
    public void receiveResupplyEvent(ResupplyEvent event){MapTileDetailsArtist.resetOverlay();};
    public void receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath){MapTileDetailsArtist.resetOverlay();};
    public void receiveUnloadEvent(UnloadLifecycle.UnloadEvent event){MapTileDetailsArtist.resetOverlay();};
    public void receiveUnitTransformEvent(Unit unit, UnitModel oldType){MapTileDetailsArtist.resetOverlay();};
    public void receiveTerrainChangeEvent(ArrayList<EnvironmentAssignment> terrainChanges){MapTileDetailsArtist.resetOverlay();};
    public void receiveWeatherChangeEvent(Weathers weather, int duration){MapTileDetailsArtist.resetOverlay();};
    public void receiveMapChangeEvent(MapChangeEvent event){MapTileDetailsArtist.resetOverlay();};
    public void receiveMassDamageEvent(Map<Unit, Integer> lostHP){MapTileDetailsArtist.resetOverlay();};
  }
}
