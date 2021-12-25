package CommandingOfficers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import Engine.GameInstance;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Terrain.MapMaster;

public abstract class CommanderAbility implements Serializable
{
  private static final long serialVersionUID = 1L;
  public static final int PHASE_TURN_START = 1;
  public static final int PHASE_BUY = PHASE_TURN_START << 1;
  public static final int PHASE_TURN_END = PHASE_BUY << 1;
  public final Commander myCommander;
  protected String myName;
  protected double myPowerCost;
  public int AIFlags = PHASE_TURN_START;
  private HashMap<Commander, ArrayList<UnitModifier>> modsApplied = new HashMap<>();

  public CommanderAbility(Commander commander, String abilityName, double powerCost)
  {
    myCommander = commander;
    myName = abilityName;
    myPowerCost = powerCost;
  }
  public void initForGame(GameInstance game)
  {}
  public void deInitForGame(GameInstance game)
  {}

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
  protected void adjustCost()
  {
    // Increase the cost of this ability for next time to mitigate spam and
    // accommodate the presumably-growing battlefront.
    // Cost grows by at least one, and at most 10% of the current cost.
    myPowerCost += Math.max(myPowerCost*0.1, 1);
  }

  /** Final method to do some bookkeeping, and then call
   * perform() do do the actual work. This allows us to
   * manage global Ability side-effects in one place. */
  public final void activate(MapMaster gameMap)
  {
    if( myCommander.getAbilityPower() < myPowerCost )
    {
      System.out.println("WARNING!: Performing ability with insufficient ability power!");
    }

    myCommander.activateAbility(this, gameMap);
    applyUnitModifiers(myCommander, gameMap);

    adjustCost();
    perform(gameMap);
  }
  private final void applyUnitModifiers(Commander co, MapMaster gameMap)
  {
    if(!modsApplied.containsKey(co)) {
      ArrayList<UnitModifier> unitModsToApply = new ArrayList<>();
      enqueueUnitMods(gameMap, unitModsToApply);
      modsApplied.put(co, unitModsToApply);
      for(UnitModifier mod : unitModsToApply)
        co.addUnitModifier(mod);
    }
  }

  /**
   * Public hook to handle cleanup.
   */
  public final void deactivate(MapMaster gameMap)
  {
    for( Commander co : new ArrayList<Commander>(modsApplied.keySet()) )
    {
      ArrayList<UnitModifier> modsToDrop = modsApplied.remove(co);
      // Revert in reverse order, just to be safe
      for( int i = modsToDrop.size() - 1; i >= 0; --i )
        co.removeUnitModifier(modsToDrop.get(i));
    }
    revert(gameMap);
  }

  /** Subclasses will override this method to enact any ability effects that don't go in an event or a UnitModifier.
   * <p>Note: getEvents() will be called before perform()*/
  // Defaults to nothing since most powers don't need to directly affect game state
  protected void perform(MapMaster gameMap)
  {}
  // Defaults to nothing since most powers don't need special revert logic.
  protected void revert(MapMaster gameMap)
  {}

  /** Allows a CommanderAbility to generate events that will be animated and published. */
  public GameEventQueue getEvents(MapMaster gameMap)
  {
    return new GameEventQueue();
  }
  public GameEventQueue getRevertEvents(MapMaster gameMap)
  {
    return new GameEventQueue();
  }

  /** Allows the subclass to specify any modifiers that it would like to be active while the ability is */
  protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList) {}

  public Collection<DamagePopup> getDamagePopups(GameMap map)
  {
    return new ArrayList<DamagePopup>();
  }
}
