package CommandingOfficers;

import java.io.Serializable;

import Terrain.MapMaster;

public abstract class CommanderAbility implements Serializable
{
  public static final int PHASE_TURN_START = 1;
  public static final int PHASE_BUY = PHASE_TURN_START << 1;
  public static final int PHASE_TURN_END = PHASE_BUY << 1;
  public final Commander myCommander;
  protected String myName;
  public final double baseCost;
  protected double myPowerCost;
  public int AIFlags = PHASE_TURN_START;

  public CommanderAbility(Commander commander, String abilityName, double powerCost)
  {
    myCommander = commander;
    myName = abilityName;
    myPowerCost = powerCost;
    baseCost = myPowerCost;
  }

  public double getCost()
  {
    return myPowerCost;
  }
  public void increaseCost(double input)
  {
    myPowerCost += input;
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
    // Increase the cost of all abilities
    for( CommanderAbility ca : myCommander.myAbilities )
    {
      ca.increaseCost(ca.baseCost * 0.2);
    }
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
