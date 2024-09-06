package Engine.GameEvents;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import lombok.var;

/**
 * Heals an arbitrary number of units, potentially at cost<p>
 * Note: Healing dealt will not be tracked correctly if the event is executed twice
 */
public class MassHealEvent implements GameEvent
{
  // Records how much health each victim lost; doubles as our victim storage area
  private Map<Unit, Integer> healResult = new LinkedHashMap<>();
  public final int repairPowerHealth;
  public boolean roundUp = true, canOverheal = false;
  public final Army payer;

  public MassHealEvent(Army payer, Collection<Unit> pToHeal, int heal)
  {
    if( heal < 0 )
      throw new ArithmeticException("Cannot negatively heal!");
    this.payer = payer;
    for( Unit patient : pToHeal )
    {
      healResult.put(patient, 0);
    }
    repairPowerHealth = heal;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    var events = new GameEventQueue();
    for( var entry : healResult.entrySet() )
    {
      var ee = listener.receiveHealEvent(payer, entry.getKey(), repairPowerHealth, entry.getValue());
      if( null != ee )
        events.addAll(ee);
    }
    return events;
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    for( var entry : healResult.entrySet() )
    {
      Unit patient = entry.getKey();
      int healAmount = HealUnitEvent.healAtCost(payer, patient, repairPowerHealth, roundUp, canOverheal);
      healResult.put(patient, healAmount);
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
