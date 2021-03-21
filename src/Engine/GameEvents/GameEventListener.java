package Engine.GameEvents;

import java.util.ArrayList;
import java.io.Serializable;
import java.util.Map;
import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.MapChangeEvent.EnvironmentAssignment;
import Engine.UnitActionLifecycles.JoinLifecycle;
import Engine.UnitActionLifecycles.LoadLifecycle;
import Engine.UnitActionLifecycles.UnloadLifecycle;
import Terrain.Environment.Weathers;
import Terrain.MapLocation;
import Units.Unit;
import Units.UnitModel;

/**
 * Subclasses can register to be notified of any supported event types. This class is both the event distributor,
 * and the extension point for all listeners. Listeners will register to receive events on creation, and should
 * override those receive functions they care about.
 * We use the visitor pattern to call the correct event function whenever an event is distributed.
 */
public interface GameEventListener extends Serializable
{
  /** Pass event along to every listener we still have. 
   * @return */
  public static GameEventQueue publishEvent(GameEvent event, GameInstance gi)
  {
    GameEventQueue events = new GameEventQueue();
    for( GameEventListener gel : gi.eventListeners )
    {
      // The event will call the appropriate receive method in the listener.
      GameEventQueue newEvents = event.sendToListener(gel);
      if( null != newEvents )
        events.addAll(newEvents);
    }
    return events;
  }

  /** Allows GameInstance to make informed decisons on whether to try saving this listener */
  default public boolean shouldSerialize() { return true; }

  /** Sign this listener up to receive events. If a listener registers multiple times, it will still
   *  receive each notification only once. */
  public static void registerEventListener(GameEventListener listener, GameInstance gi)
  {
    listener.registerForEvents(gi);
  }

  default public void registerForEvents(GameInstance gi)
  {
    gi.eventListeners.add(this);
  }

  /** Unregister this listener. Call this when a listener is no longer needed, so the JVM knows
   *  it can clean up the object. */
  public static void unregisterEventListener(GameEventListener listener, GameInstance gi)
  {
    listener.unregister(gi);
  }

  default public void unregister(GameInstance gi)
  {
    gi.eventListeners.remove(this);
  }

  // The functions below should be overridden by subclasses for event types they care about.
  // As a rule, we should avoid passing the actual event to the receive hooks when possible.
  default public GameEventQueue receiveBattleEvent(BattleSummary summary){ return null; };
  default public GameEventQueue receiveDemolitionEvent(Unit actor, XYCoord tile){ return null; };
  default public GameEventQueue receiveCreateUnitEvent(Unit unit){ return null; };
  default public GameEventQueue receiveCaptureEvent(Unit unit, MapLocation location){ return null; };
  default public GameEventQueue receiveCommanderDefeatEvent(CommanderDefeatEvent event){ return null; };
  default public GameEventQueue receiveLoadEvent(LoadLifecycle.LoadEvent event){ return null; };
  default public GameEventQueue receiveMoveEvent(MoveEvent event){ return null; };
  default public GameEventQueue receiveTeleportEvent(Unit teleporter, XYCoord from, XYCoord to){ return null; };
  default public GameEventQueue receiveTurnInitEvent(Commander co, int turn){ return null; };
  default public GameEventQueue receiveUnitJoinEvent(JoinLifecycle.JoinEvent event){ return null; };
  default public GameEventQueue receiveResupplyEvent(ResupplyEvent event){ return null; };
  default public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath){ return null; };
  default public GameEventQueue receiveUnloadEvent(UnloadLifecycle.UnloadEvent event){ return null; };
  default public GameEventQueue receiveUnitTransformEvent(Unit unit, UnitModel oldType){ return null; };
  default public GameEventQueue receiveTerrainChangeEvent(ArrayList<EnvironmentAssignment> terrainChanges){ return null; };
  default public GameEventQueue receiveWeatherChangeEvent(Weathers weather, int duration){ return null; };
  default public GameEventQueue receiveMapChangeEvent(MapChangeEvent event){ return null; };
  default public GameEventQueue receiveMassDamageEvent(Commander attacker, Map<Unit, Integer> lostHP){ return null; };
  default public GameEventQueue receiveModifyFundsEvent(Commander beneficiary, int fundsDelta){ return null; };

}
