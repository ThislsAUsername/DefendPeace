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
 * Deals damage to an arbitrary number of units, without invoking combat
 * Lethality only affects whether to expect units to die, and thus allow all of their HP to be put into the victim map
 * Note: Damage dealt will not be tracked correctly if the event is executed twice
 */
public class MassDamageEvent implements GameEvent
{
  // Records how many HP each victim lost; doubles as our victim storage area
  private Map<Unit, Integer> victims = new HashMap<Unit, Integer>();
  public final int damage;
  public final int minResultHP;

  public MassDamageEvent(Collection<Unit> pVictims, int pDamage, boolean isLethal)
  {
    for(Unit victim : pVictims)
    {
      victims.put(victim, 0);
    }
    damage = pDamage;
    if( isLethal )
      minResultHP = 0;
    else
      minResultHP = 1;
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
      victim.alterHP(-damage);
      victims.put(victim, Math.min(damage, starting - minResultHP));
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
