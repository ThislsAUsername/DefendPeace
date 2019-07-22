package Engine.GameEvents;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * Deals damage of configurable lethality to an arbitrary number of units, without invoking combat
 * Note: Damage dealt will not be tracked correctly if the event is executed twice
 */
public class MassDamageEvent implements GameEvent
{
  // Records how many HP each victim lost; doubles as our victim storage area
  private Map<Unit, Integer> victims = new HashMap<Unit, Integer>();
  public final int damage;
  public final boolean lethal;

  public MassDamageEvent(Collection<Unit> pVictims, int pDamage, boolean isLethal)
  {
    for(Unit victim : pVictims)
    {
      victims.put(victim, 0);
    }
    damage = pDamage;
    lethal = isLethal;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveMassDamageEvent(victims);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    for (Unit victim : victims.keySet())
    {
      int starting = victim.getHP();
      if( lethal && damage > starting )
      {
        victim.damageHP(-damage);
        victims.put(victim, starting);

        // Guess he's not gonna make it.
        // TODO: Is there a better way to do this?
        UnitDieEvent event = new UnitDieEvent(victim);
        event.performEvent(gameMap);
        GameEventListener.publishEvent(event);
      }
      else
      {
        victim.alterHP(-damage);
        victims.put(victim, starting - victim.getHP());
      }
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return null;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return null;
  }
}
