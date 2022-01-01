package UI.Art.SpriteArtist;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GameInstance;
import Engine.XYCoord;
import Engine.GameEvents.GameEventListener.CacheInvalidationListener;
import Engine.StateTrackers.StateTracker;
import UI.UnitMarker;
import UI.UnitMarker.MarkData;
import Units.Unit;

public class MarkArtist
{
  /**
   * Draws any marks for the unit or the terrain it's on, from Commanders or StateTrackers
   * <p>Assumes the unit is on the coordinate it thinks it's on; don't use this for animations
   */
  public static void drawMark(Graphics g, GameInstance game, Unit unit, int animIndex)
  {
    final MarkingCache cache = MarkingCache.instance(game);
    final XYCoord xyc = new XYCoord(unit);

    ArrayList<MarkData> markList = cache.getMarks(unit);
    markList.addAll(cache.getMarks(xyc));

    drawMark(g, markList, animIndex, xyc);
  }
  public static void drawMark(Graphics g, GameInstance game, XYCoord coord, int animIndex)
  {
    ArrayList<MarkData> markList = MarkingCache.instance(game).getMarks(coord);

    drawMark(g, markList, animIndex, coord);
  }

  private static void drawMark(Graphics g, ArrayList<MarkData> markList, int animIndex, XYCoord xyc)
  {
    // Convert "real" location into a draw-space location, then draw icons.
    final int drawX = (int) (SpriteLibrary.baseSpriteSize * xyc.xCoord);
    final int drawY = (int) (SpriteLibrary.baseSpriteSize * xyc.yCoord);

    // Draw one mark, based on our animation index
    if( !markList.isEmpty() )
    {
      MarkData mark = markList.get((animIndex%(markList.size()*UnitSpriteSet.ANIM_FRAMES_PER_MARK))/UnitSpriteSet.ANIM_FRAMES_PER_MARK);

      BufferedImage symbol = SpriteLibrary.getColoredMapTextSprites(mark.color).get(mark.mark);
      // draw in the upper right corner
      g.drawImage(symbol, drawX + ((SpriteLibrary.baseSpriteSize) / 2), drawY, symbol.getWidth(), symbol.getHeight(), null);    }
  }

  public static class MarkingCache implements CacheInvalidationListener
  {
    private static final long serialVersionUID = 1L;
    private static MarkingCache markCache;

    public static MarkingCache instance(GameInstance game)
    {
      if( markCache == null || game != markCache.game )
      {
        if( markCache != null )
          markCache.unregister(markCache.game);

        markCache = new MarkingCache(game);
      }

      return markCache;
    }

    private MarkingCache(GameInstance game)
    {
      this.game = game;
      registerForEvents(game);
      InvalidateCache();
    }

    @Override
    public boolean shouldSerialize()
    {
      return false;
    }

    private ArrayList<UnitMarker> markers = new ArrayList<>();
    private HashMap<XYCoord, ArrayList<MarkData>> placeMarks = new HashMap<>();
    private HashMap<Unit, ArrayList<MarkData>> unitMarks = new HashMap<>();
    private GameInstance game;

    @Override
    // Don't immediately repopulate the cache to ensure all other listeners have updated their state before we query marks
    public void InvalidateCache()
    {
      markers.clear();
      placeMarks.clear();
      unitMarks.clear();
    }

    public ArrayList<MarkData> getMarks(Unit unit)
    {
      setupMarkers();
      if( !unitMarks.containsKey(unit) )
      {
        ArrayList<MarkData> marks = new ArrayList<>();
        for( UnitMarker m : markers )
        {
          char symbol = m.getUnitMarking(unit, game.activeArmy);
          if( '\0' != symbol ) // null char is our sentry value
            marks.add(new MarkData(symbol, m.getMarkingColor(unit)));
        }
        unitMarks.put(unit, marks);
      }

      ArrayList<MarkData> output = new ArrayList<>();
      output.addAll(unitMarks.get(unit));
      return output;
    }

    public ArrayList<MarkData> getMarks(XYCoord xyc)
    {
      setupMarkers();

      if( !placeMarks.containsKey(xyc) )
      {
        ArrayList<MarkData> marks = new ArrayList<>();
        for( UnitMarker m : markers )
        {
          char symbol = m.getPlaceMarking(xyc, game.activeArmy);
          if( '\0' != symbol ) // null char is our sentry value
            marks.add(new MarkData(symbol, m.getMarkingColor(xyc)));
        }
        placeMarks.put(xyc, marks);
      }

      ArrayList<MarkData> output = new ArrayList<>();
      output.addAll(placeMarks.get(xyc));
      return output;
    }

    private void setupMarkers()
    {
      if( null == game || markers.size() > 0 )
        return;

      for( Army army : game.armies )
        if( !army.isDefeated )
        {
          markers.add(army);
          for( Commander co : army.cos )
            markers.add(co);
        }
      for( StateTracker st : game.stateTrackers.values() )
        markers.add(st);
    }
  } // ~MarkingCache
}
