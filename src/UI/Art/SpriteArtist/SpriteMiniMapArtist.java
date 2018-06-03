package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.Environment.Terrains;
import Terrain.MapInfo;

public class SpriteMiniMapArtist
{
  private static HashMap<MapInfo, BufferedImage> mapImages = new HashMap<MapInfo, BufferedImage>();

  private static HashMap<Environment.Terrains, Color> terrainColors = new HashMap<Environment.Terrains, Color>(){
    private static final long serialVersionUID = 1L;
    {
      // Create a mapping of terrain types to colors, to help us draw the minimap.
      put(Terrains.BRIDGE, new Color(189, 189, 189));
      put(Terrains.CITY, new Color(125, 125, 125));
      put(Terrains.DUNES, new Color(240, 210, 120));
      put(Terrains.FACTORY, new Color(125, 125, 125));
      put(Terrains.FOREST, new Color(46, 196, 24));
      put(Terrains.GRASS, new Color(166, 253, 77));
      put(Terrains.HQ, new Color(125, 125, 125));
      put(Terrains.MOUNTAIN, new Color(153, 99, 67));
      put(Terrains.REEF, new Color(218, 152, 112));
      put(Terrains.ROAD, new Color(189, 189, 189));
      put(Terrains.SEA, new Color(94, 184, 236));
      put(Terrains.SHOAL, new Color(253, 224, 93));
    }
  };

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
        g.setColor( terrainColors.get(mapInfo.terrain[x][y]) );
        g.fillRect(x, y, 1, 1);
      }
    }

    // Draw team colors for properties that are owned at the start.
    for(int co = 0; co < mapInfo.COProperties.length; ++co)
    {
      Color coColor;
      // Log a warning if SpriteLibrary doesn't have enough colors to support this map.
      if( co >= SpriteLibrary.coColorList.length )
      {
        System.out.println("WARNING! '" + mapInfo.mapName + "' has more start locations than there are team colors!");

        // But soldier onwards anyway.
        coColor = Color.BLACK;
      }
      else
      {
        coColor = SpriteLibrary.coColorList[co];
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
