package Engine.GameEvents;

import java.util.ArrayList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import Engine.Combat.BattleSummary;
import Engine.GameEvents.MapChangeEvent.EnvironmentAssignment;
import Terrain.Environment.Weathers;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel;

/**
 * Subclasses can register to be notified of any supported event types. This class is both the event distributor,
 * and the extension point for all listeners. Listeners will register to receive events on creation, and should
 * override those receive functions they care about.
 * We use the visitor pattern to call the correct event function whenever an event is distributed.
 */
public abstract class GameEventListener implements Serializable
{
  private static final long serialVersionUID = 1L;
  /** Static list of all event subscribers */
  private static Set<GameEventListener> eventListeners = Collections.newSetFromMap(new WeakHashMap<GameEventListener, Boolean>());

  /** Pass event along to every listener we still have. */
  public static void publishEvent(GameEvent event)
  {
    for( GameEventListener gel : eventListeners )
    {
      // The event will call the appropriate receive method in the listener.
      event.sendToListener(gel);
    }
  }

  /** Sign this listener up to receive events. If a listener registers multiple times, it will still
   *  receive each notification only once. */
  public static void registerEventListener(GameEventListener listener)
  {
    eventListeners.add(listener);
  }

  /** Convenience function to sign this listener up to receive GameEvents. */
  public final void registerForEvents()
  {
    GameEventListener.registerEventListener(this);
  }

  /** Unregister this listener. Call this when a listener is no longer needed, so the JVM knows
   *  it can clean up the object. */
  public static void unregisterEventListener(GameEventListener listener)
  {
    eventListeners.remove(listener);
  }

  /** Convenience function so a listener can unregister themselves. */
  public final void unregister()
  {
    GameEventListener.unregisterEventListener(this);
  }

  // The functions below should be overridden by subclasses for event types they care about.
  // As a rule, we should avoid passing the actual event to the receive hooks when possible.
  public void receiveBattleEvent(BattleSummary summary){};
  public void receiveCreateUnitEvent(Unit unit){};
  public void receiveCaptureEvent(Unit unit, Location location){};
  public void receiveCommanderDefeatEvent(CommanderDefeatEvent event){};
  public void receiveLoadEvent(LoadEvent event){};
  public void receiveMoveEvent(MoveEvent event){};
  public void receiveUnitJoinEvent(UnitJoinEvent event){};
  public void receiveResupplyEvent(ResupplyEvent event){};
  public void receiveUnitDieEvent(UnitDieEvent event){};
  public void receiveUnloadEvent(UnloadEvent event){};
  public void receiveUnitTransformEvent(Unit unit, UnitModel oldType){};
  public void receiveTerrainChangeEvent(ArrayList<EnvironmentAssignment> terrainChanges){};
  public void receiveWeatherChangeEvent(Weathers weather, int duration){};
  public void receiveMapChangeEvent(MapChangeEvent event){};
  public void receiveMassDamageEvent(Map<Unit, Integer> lostHP){};
  
  /**
   * Private method, same signature as in Serializable interface
   * Saves whether the listener is registered, as the registered listeners array is static
   *
   * @param stream
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    stream.defaultWriteObject();

    stream.writeBoolean(eventListeners.contains(this));
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void readObject(ObjectInputStream stream)
          throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();

    if (stream.readBoolean())
      registerEventListener(this);
  }
}
