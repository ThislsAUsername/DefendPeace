package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Terrain.GameMap;
import UI.MapView;
import UI.Art.FillRectArtist.FillRectMapArtist;

import Engine.GameAction;
import Engine.GameInstance;
import Engine.Path;
import Engine.Path.PathNode;

public class SpriteMapArtist
{
  private GameInstance myGame;
  private GameMap gameMap;
  private MapView myView;

  BufferedImage baseMapImage;

  private int drawScale;
  private int tileSize;

  FillRectMapArtist backupArtist; // TODO: Make this obsolete.

  public SpriteMapArtist(GameInstance game, MapView view)
  {
    myGame = game;
    gameMap = game.gameMap;
    myView = view;

    drawScale = SpriteOptions.getDrawScale();
    tileSize = SpriteLibrary.baseSpriteSize * drawScale;

    // TODO: make this obsolete.
    backupArtist = new FillRectMapArtist(myGame);
    backupArtist.setView(myView);

    baseMapImage = new BufferedImage(gameMap.mapWidth * myView.getTileSize(), gameMap.mapHeight * myView.getTileSize(),
        BufferedImage.TYPE_INT_RGB);

    // Build base map image.
    buildMapImage();
  }

  public void drawBaseTerrain(Graphics g)
  {
    // TODO: Change what/where we draw based on camera location.
    g.drawImage(baseMapImage, 0, 0, null);
  }

  public void drawTerrainObject(Graphics g, int x, int y)
  {
    TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(gameMap.getLocation(x, y));

    spriteSet.drawTerrainObject(g, gameMap, x, y, drawScale);
  }

  public void drawCursor(Graphics g, GameAction currentAction )
  {
    if( null == currentAction || currentAction.getActionType() == GameAction.ActionType.INVALID )
    {
      // Draw the default map cursor.
      backupArtist.drawCursor(g);
    }
    else
    {
      SpriteLibrary.drawImageCenteredOnPoint(g, SpriteLibrary.getActionCursor(),
          myGame.getCursorX() * tileSize + (tileSize / 2), myGame.getCursorY() * tileSize + (tileSize / 2), drawScale);
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
    for( int w = 0; w < gameMap.mapWidth; ++w )
    {
      for( int h = 0; h < gameMap.mapHeight; ++h )
      {
        if( gameMap.isLocationValid(w, h) )
        {
          Terrain.Location locus = gameMap.getLocation(w, h);
          if( locus.isHighlightSet() )
          {
            g.setColor(new Color(255, 255, 255, 160));
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
    // Get the Graphics object of the local map image, to use for drawing.
    Graphics g = baseMapImage.getGraphics();

    // Choose and draw all base sprites (grass, water, shallows).
    for( int y = 0; y < gameMap.mapHeight; ++y ) // Iterate horizontally to layer terrain correctly.
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        // Fetch the relevant sprite set for this terrain type and have it draw itself.
        TerrainSpriteSet spriteSet = SpriteLibrary.getTerrainSpriteSet(gameMap.getLocation(x, y));
        spriteSet.drawTerrain(g, gameMap, x, y, drawScale);
      }
    }
  }
}
