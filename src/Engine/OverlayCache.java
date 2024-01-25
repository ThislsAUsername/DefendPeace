package Engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import AI.AICombatUtils;
import Engine.MapController.OverlayMode;
import Engine.GameEvents.GameEventListener.CacheInvalidationListener;
import Terrain.MapPerspective;
import UI.GameOverlay;
import Units.Unit;

public class OverlayCache implements CacheInvalidationListener
{
  private static final long serialVersionUID = 1L;
  private static OverlayCache staticCache;

  public static OverlayCache instance(GameInstance game)
  {
    if( staticCache == null || game != staticCache.game )
    {
      if( staticCache != null )
        staticCache.unregister(staticCache.game);

      staticCache = new OverlayCache(game);
    }

    return staticCache;
  }

  private OverlayCache(GameInstance game)
  {
    this.game = game;
    registerForEvents(game);
  }

  @Override
  public boolean shouldSerialize()
  {
    return false;
  }

  private HashMap<OverlayMode, ArrayList<GameOverlay>> cache = new HashMap<>();
  private GameInstance game;

  @Override
  public void InvalidateCache()
  {
    cache.clear();
  }

  public ArrayList<GameOverlay> getNormalOverlays(final OverlayMode mode, final MapPerspective drawableMap)
  {
    if( !cache.containsKey(mode) )
    {
      ArrayList<GameOverlay> cacheLine = new ArrayList<>();
      ArrayList<Unit> threats = new ArrayList<>();

      final Army viewer = drawableMap.viewer;
      if( null == viewer )
        return new ArrayList<>();

      for( Army army : game.armies )
      {
        cacheLine.addAll(army.getMyOverlays(drawableMap, army == viewer));
      }

      switch (mode)
      {
        case THREATS_MANUAL:
          for( Unit threat : viewer.threatsToOverlay )
            // If the unit's tile's fogged, the overlay draw code will handle ignoring the unit
            if( !threat.model.hidden || drawableMap.isConfirmedVisible(threat) )
              threats.add(threat);
          break;
        case THREATS_ALL:
        case VISION:
          for( int y = 0; y < drawableMap.mapHeight; ++y )
          {
            for( int x = 0; x < drawableMap.mapWidth; ++x )
            {
              Unit resident = drawableMap.getResident(x, y);
              if( null != resident && viewer.isEnemy(resident.CO) )
                threats.add(resident);
            }
          }
          break;
        case NONE:
          break;
      }
      for( Unit u : threats )
      {
        XYCoord uCoord = new XYCoord(u);

        final Collection<XYCoord> overlayCoords;
        final Color edgeColor;
        final Color fillColor;
        if( mode == OverlayMode.VISION )
        {
          final Color basis = Color.YELLOW;
          int r = basis.getRed(), g = basis.getGreen(), b = basis.getBlue();
          edgeColor = new Color(r, g, b, 200);
          fillColor = new Color(r, g, b, 100);
          overlayCoords = Utils.findLocationsInRange(drawableMap, uCoord, u.model.visionRange);
        }
        else
        {
          final Color basis = u.CO.myColor;
          int r = basis.getRed(), g = basis.getGreen(), b = basis.getBlue();
          edgeColor = new Color(r, g, b, 200);
          fillColor = new Color(r, g, b, 100);
          overlayCoords = AICombatUtils.findThreatPower(drawableMap, u, null).keySet();
        }

        cacheLine.add(new GameOverlay(uCoord, overlayCoords,
                                      fillColor, edgeColor));
      }

      cache.put(mode, cacheLine);
    }

    return new ArrayList<>(cache.get(mode));
  }

}
