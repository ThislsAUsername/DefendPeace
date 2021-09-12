package CommandingOfficers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import CommandingOfficers.Modifiers.COModifier;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;

public abstract class CommanderAbility implements Serializable
{
  private static final long serialVersionUID = 1L;
  public static final int PHASE_TURN_START = 1;
  public static final int PHASE_BUY = PHASE_TURN_START << 1;
  public static final int PHASE_TURN_END = PHASE_BUY << 1;
  protected String myName;
  protected double myPowerCost;
  public int AIFlags = PHASE_TURN_START;
  private HashMap<Commander, ArrayList<COModifier>> coModsApplied = new HashMap<Commander, ArrayList<COModifier>>();

  public CommanderAbility(String abilityName, double powerCost)
  {
    myName = abilityName;
    myPowerCost = powerCost;
  }

  public double getCost()
  {
    return myPowerCost;
  }

  @Override
  public String toString()
  {
    return myName;
  }

  /** Provides a hook to increase the ability's cost for its next invocation.
   * Being in its own function allows an easy way for individual abilities
   * to change the cost function if needed.
   */
  public void adjustCost()
  {
    // Increase the cost of this ability for next time to mitigate spam and
    // accommodate the presumably-growing battlefront.
    // Cost grows by at least one, and at most 10% of the current cost.
    myPowerCost += Math.max(myPowerCost*0.1, 1);
  }

  /** Public hook to apply this Ability's effects. */
  public final void activate(Commander co, MapMaster gameMap)
  {
    // Don't re-apply CO mods if we've already applied them to this CO
    if(!coModsApplied.containsKey(co)) {
      ArrayList<COModifier> coModsToApply = new ArrayList<COModifier>();
      enqueueCOMods(co, gameMap, coModsToApply);
      coModsApplied.put(co, coModsToApply);
      for(COModifier com : coModsToApply)
        com.applyChanges(co);
    }
    perform(co, gameMap);
  }
  /** Public interface to handle any cleanup */
  public final void deactivate(Commander co, MapMaster gameMap)
  {
    if(coModsApplied.containsKey(co)) {
      ArrayList<COModifier> coModsToApply = coModsApplied.remove(co);
      // Revert in reverse order, just to be safe
      for( int i = coModsToApply.size() - 1; i >= 0; --i )
        coModsToApply.get(i).revertChanges(co);
    }
    revert(co, gameMap);
  }

  /** Subclasses will override this method to enact the ability's effects.
   * Note that `getEvents` will be called before `perform`, so events can be cached. */
  protected abstract void perform(Commander co, MapMaster gameMap);
  /** Called when the ability is canceled. Defaults to nothing since most powers don't need special revert logic. */
  protected void revert(Commander co, MapMaster gameMap) {}

  /** Allows a CommanderAbility to generate events that will be animated and published. */
  public GameEventQueue getEvents(Commander co, MapMaster gameMap)
  {
    return new GameEventQueue();
  }
  public GameEventQueue getRevertEvents(Commander co, MapMaster gameMap)
  {
    return new GameEventQueue();
  }

  /** Allows the subclass to specify any CO modifiers that it would like this class to handle */
  protected void enqueueCOMods(Commander co, MapMaster gameMap, ArrayList<COModifier> modList) {}

  public Collection<DamagePopup> getDamagePopups(Commander co, GameMap map)
  {
    return new ArrayList<DamagePopup>();
  }
}
