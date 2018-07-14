package CommandingOfficers;

import Engine.GameInstance;

public abstract class CommanderAbility
{
  public final Commander myCommander;
  public final String myName;
  private int myPowerCost;

  CommanderAbility(Commander commander, String abilityName, int powerCost)
  {
    myCommander = commander;
    myName = abilityName;
    myPowerCost = powerCost;
  }

  public int getCost()
  {
    return myPowerCost;
  }

  @Override
  public String toString()
  {
    return myName;
  }
  
  /** Final method to account for power expenditure, and then call
   * perform method do do the actual work. This just makes it so that
   * subclasses don't have to all manually handle ability cost. */
  public final void activate(GameInstance game)
  {
    if( myCommander.getAbilityPower() < myPowerCost )
    {
      System.out.println("WARNING!: Performing ability with insufficient ability power!");
    }

    myCommander.modifyAbilityPower(-myPowerCost);

    // Increase the cost of this ability for next time to mitigate spam and
    // accommodate the presumably-growing battlefront.
    // Cost grows by at least one, and at most 10% of the current cost.
    myPowerCost += Math.max(myPowerCost*0.1, 1);
    
    perform(game);
  }

  /** Subclasses will override this method to enact the ability's effects. */
  protected abstract void perform(GameInstance game);
}
