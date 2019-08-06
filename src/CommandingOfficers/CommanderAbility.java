package CommandingOfficers;

import java.io.Serializable;

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

  public CommanderAbility(Commander commander, String abilityName, double powerCost)
  {
    myCommander = commander;
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

  /** Provide a hook to increase the ability's cost for its next invocation.
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

    myCommander.activateAbility(this);

    adjustCost();
    perform(gameMap);
  }

  /** Subclasses will override this method to enact the ability's effects. */
  protected abstract void perform(MapMaster gameMap);
}
