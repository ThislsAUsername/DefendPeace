package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.Path;
import Engine.Path.PathNode;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.MapChangeEvent.EnvironmentAssignment;
import Terrain.Environment.Weathers;
import Terrain.GameMap;
import UI.MapView;
import Units.Unit;

public class MapArtist
{
  private GameInstance myGame;
  private MapView myView;

  BufferedImage baseMapImage;
  MapImageUpdater baseMapImageUpdater;

  private int tileSize;

  private Color FOG_COLOR;
  private Color HIGHLIGHT_COLOR;

  SpriteCursor spriteCursor;

  public MapArtist(GameInstance game, MapView view)
  {
    myGame = game;
    GameMap gameMap = myGame.gameMap;
    myView = view;

    tileSize = myView.getTileSize();
    Commander co0 = game.commanders[0];
    spriteCursor = new SpriteCursor(co0.HQLocation.xCoord * tileSize, co0.HQLocation.yCoord * tileSize, tileSize, tileSize, co0.myColor);

    baseMapImage = new BufferedImage(gameMap.mapWidth * tileSize, gameMap.mapHeight * tileSize, BufferedImage.TYPE_INT_RGB);

    // Choose colors for fog and highlighted tiles.
    FOG_COLOR = new Color(72, 72, 96, 200); // dark blue
    HIGHLIGHT_COLOR = new Color(255, 255, 255, 160); // white
    TerrainSpriteSet.setFogColor(FOG_COLOR);

    // Build base map image.
    buildMapImage();
    baseMapImageUpdater = new MapImageUpdater(this);
    GameEventListener.registerEventListener(baseMapImageUpdater);
  }

  public void drawBaseTerrain(Graphics g, GameMap gameMap, int viewX, int viewY, int viewW, int viewH)
  {
    // First four coords are the dest x,y,x2,y2. Next four are the source coords.
    g.drawImage(baseMapImage, viewX, viewY, viewX + viewW, viewY + viewH, viewX, viewY, viewX + viewW, viewY + viewH, null);

    // Draw fog effects.
    int numTilesY = (viewY + viewH) / tileSize;
    int numTilesX = (viewX + viewW) / tileSize;
    for( int y = viewY / tileSize; y < numTilesY+1; ++y )
      for( int x = viewX / tileSize; x < numTilesX+1; ++x )
      {
        if( gameMap.isLocationFogged(x, y) )
        {
          g.setColor(FOG_COLOR);
          g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
        }
      }
  }

  public void drawTerrainObject(Graphics g, GameMap gameMap, int x, int y)
  {
    TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(gameMap.getLocation(x, y));

    boolean drawFog = gameMap.isLocationFogged(x, y);
    spriteSet.drawTerrainObject(g, gameMap, x, y, drawFog);
  }

  public void drawCursor(Graphics g, Unit unitActor, boolean isTargeting, int drawX, int drawY)
  {
    if( !isTargeting )
    {
      // Draw the default map cursor.
      spriteCursor.set(myGame.activeCO.myColor);
      spriteCursor.set(drawX*tileSize, drawY*tileSize);
      spriteCursor.draw(g);
    }
    else
    {
      SpriteUIUtils.drawImageCenteredOnPoint(g, SpriteLibrary.getActionCursor(), myGame.getCursorX() * tileSize + (tileSize / 2),
          myGame.getCursorY() * tileSize + (tileSize / 2));
    }
  }

  public void drawMovePath(Graphics g, Path path)
  {
    Sprite moveLineSprites = SpriteLibrary.getMoveCursorLineSprite();
    Sprite moveArrowSprites = SpriteLibrary.getMoveCursorArrowSprite();

    for( int i = 0; i < path.getWaypoints().size(); ++i )
    {
      // Figure out which line segment type to draw.
      PathNode lastp = null;
      PathNode thisp = path.getWaypoints().get(i);
      PathNode nextp = null;
      if( i > 0 )
      {
        lastp = path.getWaypoints().get(i - 1);
      }
      if( (i + 1) < path.getWaypoints().size() )
      {
        nextp = path.getWaypoints().get(i + 1);
      }

      // Calculate which frame to draw
      short dir1 = getMovePathDirection(thisp, lastp);
      short dir2 = getMovePathDirection(thisp, nextp);
      int index = dir1 | dir2;

      // Choose the specific image; if nextp is null, we know it's the end of the path, so use an arrow;
      BufferedImage movePathSprite = (nextp != null) ? moveLineSprites.getFrame(index) : moveArrowSprites.getFrame(index);

      // Draw it.
      g.drawImage(movePathSprite, thisp.x * tileSize, thisp.y * tileSize, tileSize, tileSize, null);
    }
  }

  /**
   * Returns a short value representing the direction from 'before' to 'after'
   * NOTE: It is assumed that 'before' and 'after' will be cardinally oriented; no diagonals.
   */
  private short getMovePathDirection(PathNode before, PathNode after)
  {
    short NORTH = 0x1;
    short EAST = 0x2;
    short SOUTH = 0x4;
    short WEST = 0x8;

    short actualDir = 0x0;

    // We need two things for there to be a direction between them.
    if( before != null && after != null )
    {
      // Either x or y can be different. Check x offset.
      if( before.x != after.x )
      {
        actualDir = (before.x < after.x) ? EAST : WEST;
      }
      else if( before.y != after.y ) // If not x, maybe y is different.
      {
        actualDir = (before.y < after.y) ? SOUTH : NORTH;
      }
    }

    return actualDir;
  }

  public void drawHighlights(Graphics g)
  {
    GameMap gameMap = myGame.activeCO.myView;
    for( int w = 0; w < gameMap.mapWidth; ++w )
    {
      for( int h = 0; h < gameMap.mapHeight; ++h )
      {
        if( gameMap.isLocationValid(w, h) )
        {
          Terrain.Location locus = gameMap.getLocation(w, h);
          if( locus.isHighlightSet() )
          {
            g.setColor(HIGHLIGHT_COLOR);
            g.fillRect(w * tileSize, h * tileSize, tileSize, tileSize);
          }
        }
      }
    }
  }

  /**
   * Populates baseMapImage with an image of the map terrain only - it does not draw
   * terrain objects, units, etc. This image would only need to change if the map itself
   * was modified mid-game.
   */
  private void buildMapImage()
  {
    GameMap gameMap = myGame.gameMap;
    // Get the Graphics object of the local map image, to use for drawing.
    Graphics g = baseMapImage.getGraphics();

    // Choose and draw all base sprites (grass, water, shallows).
    for( int y = 0; y < gameMap.mapHeight; ++y ) // Iterate horizontally to layer terrain correctly.
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        // Fetch the relevant sprite set for this terrain type and have it draw itself.
        TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(gameMap.getLocation(x, y));
        spriteSet.drawTerrain(g, gameMap, x, y, false);
      }
    }
  }

  private void redrawBaseTile(XYCoord coord)
  {
    GameMap gameMap = myGame.gameMap;
    // Get the Graphics object of the local map image, to use for drawing.
    Graphics g = baseMapImage.getGraphics();

    // If this tile changes, it might necessitate terrain transition changes
    // in adjacent tiles, so find and redraw the adjacent tiles as well.
    for( XYCoord drawCoord : Utils.findLocationsInRange(gameMap, coord, 0, 1) )
    {
      // Fetch the relevant sprite set for this terrain type and have it draw itself.
      TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(gameMap.getLocation(drawCoord.xCoord, drawCoord.yCoord));
      spriteSet.drawTerrain(g, gameMap, drawCoord.xCoord, drawCoord.yCoord, false);
    }
  }

  private static class MapImageUpdater extends GameEventListener
  {
    private static final long serialVersionUID = 1L;
    MapArtist myArtist;
    MapImageUpdater(MapArtist artist)
    {
      myArtist = artist;
    }

    @Override
    public void receiveTerrainChangeEvent(ArrayList<EnvironmentAssignment> terrainChanges)
    {
      for( EnvironmentAssignment ea : terrainChanges )
      {
        if( null != ea.where ) myArtist.redrawBaseTile(ea.where); // Redraw each tile that changed.
      }
    }

    @Override
    public void receiveWeatherChangeEvent(Weathers weather, int duration)
    {
      myArtist.buildMapImage(); // Redraw the whole map.
    }
  }

  public void cleanup()
  {
    GameEventListener.unregisterEventListener(baseMapImageUpdater);
    baseMapImageUpdater = null;
  }
}
