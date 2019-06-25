package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Engine.XYCoord;
import Terrain.MapInfo;
import UI.UIUtils;

public class MiniMapArtist
{
  private static MapInfo lastMapDrawn;
  private static Color[] lastTeamColors;
  private static BufferedImage lastMapImage;

  /**
   * Retrieve a BufferedImage with a 1-pixel-per-tile representation of the provided MapInfo.
   * Requested images are generated and store on first call, then simply fetched and returned
   * if this function is called again with the same MapInfo.
   * Teams will be colored according to the default color ordering.
   */
  public static BufferedImage getMapImage(MapInfo mapInfo)
  {
    return getMapImage(mapInfo, UIUtils.getCOColors());
  }

  /**
   * Retrieve a BufferedImage with a 1-pixel-per-tile representation of the provided MapInfo.
   * Requested images are generated and store on first call, then simply fetched and returned
   * if this function is called again with the same MapInfo.
   * @param teamColors A set of colors to be used for drawing each team. If insufficient colors
   *        are provided, black will be used for any remaining ones. If null is passed in,
   *        the default team-color ordering will be used.
   */
  public static BufferedImage getMapImage(MapInfo mapInfo, Color[] teamColors)
  {
    if( mapInfo != lastMapDrawn || palettesDiffer(teamColors) )
    {
      // If we don't already have an image, generate and store it for later.
      lastMapImage = generateMapImage(mapInfo, teamColors);
      lastMapDrawn = mapInfo;
      lastTeamColors = teamColors;
    }

    return lastMapImage;
  }

  /**
   * Generates a BufferedImage with a 1-pixel-per=tile representation of the map.
   */
  private static BufferedImage generateMapImage(MapInfo mapInfo, Color[] teamColors)
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

    // If we don't have a palette yet, just get the default one from UIUtis.
    if( null == teamColors ) teamColors = UIUtils.getCOColors();

    // Draw team colors for properties that are owned at the start.
    for(int co = 0; co < mapInfo.COProperties.length; ++co)
    {
      Color coColor;

      // Log a warning if SpriteLibrary doesn't have enough colors to support this map.
      if( co >= teamColors.length )
      {
        System.out.println("WARNING! '" + mapInfo.mapName + "' has more start locations than there are team colors!");

        // But soldier onwards anyway.
        coColor = Color.BLACK;
      }
      else
      {
        coColor = teamColors[co];
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

  private static boolean palettesDiffer(Color[] newColors)
  {
    if( null == lastTeamColors || null == newColors )
      return true;
    if( lastTeamColors.length != newColors.length )
      return true;

    boolean colorsDiffer = false;
    for( int i = 0; i < newColors.length; ++i )
    {
      if( !newColors[i].equals(lastTeamColors[i]) )
      {
        colorsDiffer = true;
        break;
      }
    }

    return colorsDiffer;
  }
}
