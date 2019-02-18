package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import Engine.XYCoord;
import Terrain.MapInfo;

public class SpriteMiniMapArtist
{
  private static HashMap<MapInfo, BufferedImage> mapImages = new HashMap<MapInfo, BufferedImage>();

  /**
   * Retrieve a BufferedImage with a 1-pixel-per-tile representation of the provided MapInfo.
   * Requested images are generated and store on first call, then simply fetched and returned
   * if this function is called again with the same MapInfo.
   */
  public static BufferedImage getMapImage(MapInfo mapInfo)
  {
    if( !mapImages.containsKey(mapInfo) )
    {
      // If we don't already have an image, generate and store it for later.
      mapImages.put(mapInfo, generateMapImage(mapInfo));
    }

    return mapImages.get(mapInfo);
  }

  /**
   * Generates a BufferedImage with a 1-pixel-per=tile representation of the map.
   */
  private static BufferedImage generateMapImage(MapInfo mapInfo)
  {
    BufferedImage image = new BufferedImage(mapInfo.getWidth(), mapInfo.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.getGraphics();

    // Loop through the entire image, filling in each pixel with the appropriate color.
    for(int y = 0; y < image.getHeight(); ++y)
    {
      for(int x = 0; x < image.getWidth(); ++x)
      {
        g.setColor( mapInfo.terrain[x][y].getMainColor() );
        g.fillRect(x, y, 1, 1);
      }
    }

    // Draw team colors for properties that are owned at the start.
    for(int co = 0; co < mapInfo.COProperties.length; ++co)
    {
      Color coColor;
      // Log a warning if SpriteLibrary doesn't have enough colors to support this map.
      if( co >= SpriteLibrary.getCOColors().length )
      {
        System.out.println("WARNING! '" + mapInfo.mapName + "' has more start locations than there are team colors!");

        // But soldier onwards anyway.
        coColor = Color.BLACK;
      }
      else
      {
        coColor = SpriteLibrary.getCOColors()[co];
      }

      // Loop through all locations assigned to this CO by mapInfo.
      for(int i = 0; i < mapInfo.COProperties[co].length; ++i)
      {
        // Draw the specified location in a new color.
        XYCoord loc = mapInfo.COProperties[co][i];
        int x = loc.xCoord;
        int y = loc.yCoord;

        g.setColor( coColor );
        g.fillRect(x, y, 1, 1);
      }
    }

    return image;
  }
}
