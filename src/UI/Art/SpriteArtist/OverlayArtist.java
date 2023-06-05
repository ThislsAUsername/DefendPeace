package UI.Art.SpriteArtist;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import Engine.XYCoord;
import Terrain.GameMap;
import UI.GameOverlay;

public class OverlayArtist
{
  public static final Color HIGHLIGHT_COLOR = new Color(255, 255, 255, 255); // white

  public static final Color NOW_FIRE_EDGE    = new Color(240,   0,   0, 255); // red
  public static final Color LATER_FIRE_EDGE  = new Color(  0,   0, 160, 255); // dark blue
  public static final Color FIRE_FILL        = new Color(  0,   0,   0,   0);

  private static final int OVERLAY_EDGE_THICKNESS = 2;

  static AlphaComposite buffComposite = null;
  static long lastCompositeCreationTime = 0;

  private static ByteBuffer oldParams;
  private static BufferedImage overlayImage;

  /**
   * Draw any overlays in the input set, as well as the move/target highlight overlays.
   * @param bigG The map image graphics, ignoring viewport size
   * @param drawX Top left X coordinate w.r.t. the map in 1:1 drawspace
   * @param drawY Top left Y coordinate w.r.t. the map in 1:1 drawspace
   * @param viewWidth min(map size, viewport size) in 1:1 drawspace
   * @param viewHeight min(map size, viewport size) in 1:1 drawspace
   * @param tileSize The factor converting from map space to 1:1 drawspace
   * @param planningMove Whether we're currently considering where to move a unit
   * @param moveLoc Where we're planning to move that unit
   */
  public static void drawHighlights(Graphics bigG,
                                    GameMap gameMap,
                                    Collection<GameOverlay> inputOverlays,
                                    int drawX, int drawY,
                                    int viewWidth, int viewHeight,
                                    int tileSize,
                                    boolean planningMove, XYCoord moveLoc)
  {
    if( viewWidth < 1 || viewHeight < 1 )
      return;

    // Only show stuff we're allowed to
    Collection<GameOverlay> overlays = new ArrayList<GameOverlay>();
    for( GameOverlay ov : inputOverlays )
      if( null == ov.origin || !gameMap.isLocationFogged(ov.origin) )
        overlays.add(ov);

    // Check if input is the same as last time
    ByteBuffer params = ByteBuffer.allocate(Integer.BYTES * (8 + inputOverlays.size()*3) );
    params.putInt(inputOverlays.size())
          .putInt(drawX).putInt(drawY)
          .putInt(viewWidth).putInt(viewHeight)
          .putInt(tileSize);
    if( planningMove )
      params.putInt(moveLoc.xCoord).putInt(moveLoc.yCoord);
    for( GameOverlay ov : overlays )
    {
      params.putInt(ov.area.size());
      if( null != ov.origin )
      {
        params.putInt(ov.origin.xCoord);
        params.putInt(ov.origin.yCoord);
      }
    }
    params.rewind(); // Push the buffer pointer to the start so "remaining elements" exists

    if( !params.equals(oldParams) )
    {
      // Army pushes a mark on threat overlays, so update marks when the overlays change
      MarkArtist.MarkingCache.instance(gameMap.game).InvalidateCache();
      oldParams = params;
      overlayImage = generateOverlayImage(gameMap, overlays, drawX, drawY, viewWidth, viewHeight, tileSize);
    }

    // Set opacity as a function of time.
    long nowTime = System.currentTimeMillis();
    float overlayOpacity = (float)(0.15*Math.sin(nowTime/420.) + 0.7);

    // Only regenerate the AlphaComposite object once per timestep.
    if(lastCompositeCreationTime < nowTime-5)
    {
      lastCompositeCreationTime = nowTime;
      buffComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayOpacity);
    }

    Graphics2D g2d = (Graphics2D)bigG;
    Composite oldComposite = g2d.getComposite();
    g2d.setComposite(buffComposite);
    g2d.drawImage(overlayImage, drawX, drawY, null);
    g2d.setComposite(oldComposite);
  }

  private static void drawOverlayTile(Graphics og, GameOverlay overlay, XYCoord coord, int drawX, int drawY, int tileSize)
  {
    og.setColor(overlay.fill);
    og.fillRect(drawX, drawY, tileSize, tileSize);
    og.setColor(overlay.edge);
    final int eT = OVERLAY_EDGE_THICKNESS;
    final int hT = eT/2;
    if( !overlay.area.contains(coord.left()) )
      og.fillRect(drawX-hT            , drawY-hT            ,          eT, tileSize+eT);
    if( !overlay.area.contains(coord.right()) )
      og.fillRect(drawX+hT+tileSize-eT, drawY-hT            ,          eT, tileSize+eT);
    if( !overlay.area.contains(coord.up()) )
      og.fillRect(drawX-hT            , drawY-hT            , tileSize+eT,          eT);
    if( !overlay.area.contains(coord.down()) )
      og.fillRect(drawX-hT            , drawY+hT+tileSize-eT, tileSize+eT,          eT);
  }

  private static BufferedImage generateOverlayImage(GameMap gameMap,
                                                    Collection<GameOverlay> overlays,
                                                    int drawX, int drawY,
                                                    int viewWidth, int viewHeight,
                                                    int tileSize)
  {
    BufferedImage overlayImage = SpriteLibrary.createTransparentSprite(viewWidth, viewHeight);
    Graphics og = overlayImage.getGraphics();

    for( int w = 0; w < gameMap.mapWidth; ++w )
    {
      if( (w+1)*tileSize < drawX || drawX + viewWidth < (w-2)*tileSize )
        continue;
      for( int h = 0; h < gameMap.mapHeight; ++h )
      {
        if( (h+1)*tileSize < drawY || drawY + viewHeight < (h-2)*tileSize )
          continue;

        XYCoord coord = new XYCoord(w,h);
        for( GameOverlay ov : overlays )
          if( ov.area.contains(coord) )
            drawOverlayTile(og, ov, coord, w * tileSize - drawX, h * tileSize - drawY, tileSize);
      }
    }

    return overlayImage;
  }
}
