package Engine.GameEvents;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * Deals damage to an arbitrary number of units, without invoking combat<p>
 * Lethality only affects whether to *expect* units to die, and thus allow all of their health to be put into the victim map<p>
 * Note: Damage dealt will not be tracked correctly if the event is executed twice
 */
public class MassDamageEvent implements GameEvent
{
  private final Commander attacker;
  // Records how much health each victim lost; doubles as our victim storage area
  private Map<Unit, Integer> victims = new HashMap<Unit, Integer>();
  public final int damage;
  public final boolean lethal;
  public final boolean shouldStun;

  public MassDamageEvent(Commander attacker, Collection<Unit> pVictims,
      int pDamage, boolean isLethal)
  {
    this(attacker, pVictims, pDamage, isLethal, false);
  }
  public MassDamageEvent(Commander attacker, Collection<Unit> pVictims,
      int pDamage, boolean isLethal, boolean pStun)
  {
    if( pDamage < 0 )
      throw new ArithmeticException("Cannot inflict negative damage!");
    this.attacker = attacker;
    for(Unit victim : pVictims)
    {
      victims.put(victim, 0);
    }
    damage = pDamage;
    lethal = isLethal;
    shouldStun = pStun;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveMassDamageEvent(attacker, victims);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    for (Unit victim : victims.keySet())
    {
      int deltaHealth = 0;
      if( lethal )
        deltaHealth = victim.damageHealth(damage);
      else
        deltaHealth = victim.alterHealth(-1 * damage);
      int lostHealth = -deltaHealth;

      if( shouldStun )
        victim.isStunned = true;

      victims.put(victim, lostHealth);
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
