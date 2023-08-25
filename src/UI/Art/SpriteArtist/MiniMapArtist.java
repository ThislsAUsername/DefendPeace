package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import Engine.XYCoord;
import Terrain.MapInfo;
import UI.UIUtils;
import UI.UIUtils.COSpriteSpec;
import UI.UIUtils.Faction;
import UI.Art.SpriteArtist.UnitSpriteSet.AnimState;

public class MiniMapArtist
{
  private static MapInfo lastMapDrawn;
  private static Color[] lastTeamColors;
  private static BufferedImage lastFullMapImage;

  /**
   * Retrieve a BufferedImage with a 1-pixel-per-tile representation of the provided MapInfo.
   * Requested images are generated and store on first call, then simply fetched and returned
   * if this function is called again with the same MapInfo.
   * Teams will be colored according to the default color ordering.
   */
  public static BufferedImage getMapImage(MapInfo mapInfo, int maxWidth, int maxHeight)
  {
    return getMapImage(mapInfo, UIUtils.getFactions(), UIUtils.getCOColors(), maxWidth, maxHeight);
  }

  /**
   * Retrieve a BufferedImage with a 1-pixel-per-tile representation of the provided MapInfo.
   * Requested images are generated and store on first call, then simply fetched and returned
   * if this function is called again with the same MapInfo.
   * @param teamColors A set of colors to be used for drawing each team. If insufficient colors
   *        are provided, black will be used for any remaining ones. If null is passed in,
   *        the default team-color ordering will be used.
   * @param factions Ditto, but factions and SpriteLibrary.DEFAULT_FACTION (Thorn)
   */
  public static BufferedImage getMapImage(MapInfo mapInfo, Faction[] factions, Color[] teamColors, int maxWidth, int maxHeight)
  {
    if( mapInfo != lastMapDrawn || palettesDiffer(teamColors) )
    {
      // If we don't already have an image, generate and store it for later.
      lastFullMapImage = generateFullMapImage(mapInfo, factions, teamColors);
      lastMapDrawn = mapInfo;
      lastTeamColors = teamColors;
    }

    BufferedImage miniMap = lastFullMapImage;
    // Crunch it down a bit if it won't fit
    final int mapHeight = lastFullMapImage.getHeight();
    final int mapWidth = lastFullMapImage.getWidth();
    if(   maxHeight < mapHeight
        || maxWidth < mapWidth )
    {
      final double heightRatio = ((float)maxHeight) / mapHeight;
      final double widthRatio  = ((float)maxWidth ) / mapWidth;
      final double finalRatio  = Math.min(heightRatio, widthRatio);
      final int finalHeight    = (int) (mapHeight * finalRatio);
      final int finalWidth     = (int) (mapWidth * finalRatio);

      miniMap = SpriteLibrary.createTransparentSprite(finalWidth, finalHeight);

      final AffineTransform at = AffineTransform.getScaleInstance(finalRatio, finalRatio);
      final AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
      miniMap = ato.filter(lastFullMapImage, miniMap);
    }

    return miniMap;
  }

  /**
   * Generates a BufferedImage containing the full-scale map.
   */
  private static BufferedImage generateFullMapImage(MapInfo mapInfo, Faction[] factions, Color[] teamColors)
  {
    // If we don't have a palette yet, just get the default one from UIUtis.
    if( null == teamColors ) teamColors = UIUtils.getCOColors();

    BufferedImage image = new BufferedImage(
        mapInfo.getWidth() * SpriteLibrary.baseSpriteSize,
        mapInfo.getHeight() * SpriteLibrary.baseSpriteSize,
        BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.getGraphics();

    for(int y = 0; y < mapInfo.getHeight(); ++y) // Iterate horizontally to layer terrain correctly.
    {
      for(int x = 0; x < mapInfo.getWidth(); ++x)
      {
        XYCoord coord = new XYCoord(x, y);
        COSpriteSpec spec = null;

        boolean anyUnits = (mapInfo.mapUnits.size() > 0);
        String unitName = null;
        COSpriteSpec unitSpec = null;
        boolean unitFlip = false, unitBuff = false;

        for( int co = 0; co < mapInfo.COProperties.length; ++co )
        {
          // Figure out unit details, if any
          if( anyUnits && mapInfo.mapUnits.get(co).containsKey(coord) )
          {
            unitName = mapInfo.mapUnits.get(co).get(coord);
            unitSpec = getNthSpriteSpec(factions, teamColors, co);
            unitFlip = (co % 2) == 1;
          }

          // Figure out team color, if any
          for( int i = 0; i < mapInfo.COProperties[co].length; ++i )
          {
            if( coord.equals(mapInfo.COProperties[co][i]) )
            {
              spec = getNthSpriteSpec(factions, teamColors, co);
              break;
            }
          }
          if( null != spec)
            break;
        }

        // Fetch the relevant sprite set for this terrain type and have it draw itself.
        TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(mapInfo.terrain[x][y], spec);
        spriteSet.drawTerrain(g, mapInfo, x, y, false);
        spriteSet.drawTerrainObject(g, mapInfo, x, y, false);

        if( null != unitName )
        {
          int tileSize = SpriteLibrary.baseSpriteSize;
          UnitSpriteSet unitSpriteSet = SpriteLibrary.getMapUnitSpriteSet(unitName, unitSpec.faction, unitSpec.color);
          unitSpriteSet.drawUnit(g, AnimState.IDLE, 0,
                                 x*tileSize, y*tileSize,
                                 unitFlip  , unitBuff
                                 );
        }
      }
    }

    return image;
  }

  private static COSpriteSpec getNthSpriteSpec(Faction[] factions, Color[] teamColors, int co)
  {
    COSpriteSpec spec;
    Color coColor = Color.BLACK;
    Faction faction = new Faction();

    if( co < factions.length )
      faction = factions[co];

    if( co < teamColors.length )
      coColor = teamColors[co];

    spec = new COSpriteSpec(faction, coColor);
    return spec;
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
