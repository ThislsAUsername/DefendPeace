package Engine.UnitMods;

import Engine.GameInstance;
import Engine.GameEvents.GameEventListener;

/**
 * This class exists to act as a "singleton with respect to a GameInstance".
 * <p>The intended use case is subclasses that track certain activities globally for use in UnitModifiers.
 */
public abstract class StateTracker<T extends StateTracker<T>> implements GameEventListener
{
  private static final long serialVersionUID = 1L;
  public final Class<T> key; // TODO: Verify ref-compare works on save-load
  public final GameInstance game;

  protected StateTracker(Class<T> key, GameInstance gi)
  {
    this.key = key;
    this.game = gi;
  }

  public static <T extends StateTracker<T>> T initialize(GameInstance gi, Class<T> key)
  {
    if( !gi.stateTrackers.containsKey(key) )
      try
      {
        T instance = key.getDeclaredConstructor(Class.class, GameInstance.class).newInstance(key, gi);
        gi.stateTrackers.put(key, instance);
        instance.registerForEvents(gi);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        System.exit(-1);
      }

    return key.cast(gi.stateTrackers.get(key)).item();
  }

  protected abstract T item();
}
