package Engine.GameEvents;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import Engine.Combat.BattleSummary;
import Terrain.Location;
import Units.Unit;

/**
 * Subclasses can register to be notified of any supported event types. This class is both the event distributor,
 * and the extension point for all listeners. Listeners will register to receive events on creation, and should
 * override those receive functions they care about.
 * We use the visitor pattern to call the correct event function whenever an event is distributed.
 */
public abstract class GameEventListener implements Serializable
{
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
  public void receiveBattleEvent(BattleSummary summary){};
  public void receiveCreateUnitEvent(Unit unit){};
  public void receiveCaptureEvent(Unit unit, Location location){};
  public void receiveCommanderDefeatEvent(CommanderDefeatEvent event){};
  public void receiveLoadEvent(LoadEvent event){};
  public void receiveMoveEvent(MoveEvent event){};
  public void receiveResupplyEvent(ResupplyEvent event){};
  public void receiveUnitDieEvent(UnitDieEvent event){};
  public void receiveUnloadEvent(UnloadEvent event){};
  public void receiveMapChangeEvent(MapChangeEvent event){};
}
