package UI.Art.SpriteArtist;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Terrain.GameMap;

public class OverlayArtist
{
  private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 255, 160); // white;

  static AlphaComposite buffComposite = null;
  static long lastCompositeCreationTime = 0;

  public static void drawHighlights(Graphics bigG,
                                    GameMap gameMap,
                                    int drawX, int drawY,
                                    int viewWidth, int viewHeight,
                                    int tileSize)
  {
    if( viewWidth < 1 || viewHeight < 1 )
      return;

    BufferedImage overlay = SpriteLibrary.createTransparentSprite(viewWidth, viewHeight);
    Graphics og = overlay.getGraphics();

    for( int w = 0; w < gameMap.mapWidth; ++w )
    {
      if( (w+1)*tileSize < drawX || drawX + viewWidth < (w-2)*tileSize )
        continue;
      for( int h = 0; h < gameMap.mapHeight; ++h )
      {
        if( (h+1)*tileSize < drawY || drawY + viewHeight < (h-2)*tileSize )
          continue;
        
        if( gameMap.isLocationValid(w, h) )
        {
          Terrain.Location locus = gameMap.getLocation(w, h);
          if( locus.isHighlightSet() )
          {
            og.setColor(HIGHLIGHT_COLOR);
            og.fillRect(w * tileSize - drawX, h * tileSize - drawY, tileSize, tileSize);
          }
        }
      }
    }

    // Set opacity as a function of time.
    long nowTime = System.currentTimeMillis();
//    float overlayOpacity = (float)(0.5*Math.max(0, Math.sin(nowTime/420.)));
    float overlayOpacity = (float)(0.15*Math.sin(nowTime/420.) + 0.7);

    // Only regenerate the AlphaComposite object once per timestep.
//    if(lastCompositeCreationTime != nowTime)
    if(lastCompositeCreationTime < nowTime-5)
    {
      lastCompositeCreationTime = nowTime;
      buffComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayOpacity);
    }

    Graphics2D g2d = (Graphics2D)bigG;
    Composite oldComposite = g2d.getComposite();
    g2d.setComposite(buffComposite);
    g2d.drawImage(overlay, drawX, drawY, null);
    g2d.setComposite(oldComposite);
  }
}
