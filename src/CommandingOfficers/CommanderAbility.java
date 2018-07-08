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

  public double getCost()
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
    if( myCommander.abilityPower < myPowerCost )
    {
      System.out.println("WARNING!: Performing ability with insufficient ability power!");
    }

    myCommander.abilityPower -= myPowerCost;

    // Increase the cost of this ability for next time to mitigate spam and
    // accommodate the presumably-growing battlefront.
    myPowerCost += 4;
    
    perform(game);
  }

  /** Subclasses will override this method to enact the ability's effects. */
  protected abstract void perform(GameInstance game);
}
