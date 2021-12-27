package Engine.StateTrackers;

import java.lang.reflect.Constructor;

import Engine.GameInstance;
import Engine.GameEvents.GameEventListener;
import UI.UnitMarker;

/**
 * This class exists to act as a "singleton with respect to a GameInstance".
 * <p>The intended use case is subclasses that track certain activities globally for use in UnitModifiers.
 */
public abstract class StateTracker implements GameEventListener, UnitMarker
{
  private static final long serialVersionUID = 1L;
  protected GameInstance game;

  protected StateTracker()
  {}

  /**
   * The external access point for *all* access to instances of this class and its subclasses
   */
  public static <T extends StateTracker> T instance(GameInstance gi, Class<T> key)
  {
    if( !gi.stateTrackers.containsKey(key) )
      try
      {
        Constructor<T> constructor = key.getDeclaredConstructor();
        constructor.setAccessible(true); // Apparently required for dealing with inner classes
        T instance = constructor.newInstance();
        instance.game = gi;
        gi.stateTrackers.put(key, instance);
        instance.registerForEvents(gi);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        System.exit(-1);
      }

    return key.cast(gi.stateTrackers.get(key));
  }
}
