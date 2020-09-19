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

  /** Pass event along to every listener we still have. */
  public static void publishEvent(GameEvent event, GameInstance gi)
  {
    for( GameEventListener gel : gi.eventListeners )
    {
      // The event will call the appropriate receive method in the listener.
      event.sendToListener(gel);
    }
  }

  /** Allows GameInstance to make informed decisons on whether to try saving this listener */
  public boolean shouldSerialize() { return true; }

  /** Sign this listener up to receive events. If a listener registers multiple times, it will still
   *  receive each notification only once. */
  public static void registerEventListener(GameEventListener listener, GameInstance gi)
  {
    listener.registerForEvents(gi);
  }

  public void registerForEvents(GameInstance gi)
  {
    gi.eventListeners.add(this);
  }

  /** Unregister this listener. Call this when a listener is no longer needed, so the JVM knows
   *  it can clean up the object. */
  public static void unregisterEventListener(GameEventListener listener, GameInstance gi)
  {
    listener.unregister(gi);
  }

  public void unregister(GameInstance gi)
  {
    gi.eventListeners.remove(this);
  }

  // The functions below should be overridden by subclasses for event types they care about.
  // As a rule, we should avoid passing the actual event to the receive hooks when possible.
  public void receiveBattleEvent(BattleSummary summary){};
  public void receiveDemolitionEvent(Unit actor, XYCoord tile){};
  public void receiveCreateUnitEvent(Unit unit){};
  public void receiveCaptureEvent(Unit unit, Location location){};
  public void receiveCommanderDefeatEvent(CommanderDefeatEvent event){};
  public void receiveLoadEvent(LoadLifecycle.LoadEvent event){};
  public void receiveMoveEvent(MoveEvent event){};
  public void receiveTeleportEvent(Unit teleporter, XYCoord from, XYCoord to){};
  public void receiveTurnInitEvent(Commander co, int turn){};
  public void receiveUnitJoinEvent(JoinLifecycle.JoinEvent event){};
  public void receiveResupplyEvent(ResupplyEvent event){};
  public void receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath){};
  public void receiveUnloadEvent(UnloadLifecycle.UnloadEvent event){};
  public void receiveUnitTransformEvent(Unit unit, UnitModel oldType){};
  public void receiveTerrainChangeEvent(ArrayList<EnvironmentAssignment> terrainChanges){};
  public void receiveWeatherChangeEvent(Weathers weather, int duration){};
  public void receiveMapChangeEvent(MapChangeEvent event){};
  public void receiveMassDamageEvent(Map<Unit, Integer> lostHP){};

}
