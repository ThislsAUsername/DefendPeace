package CommandingOfficers;

import Terrain.GameMap;

public abstract class CommanderAbility
{
  public final Commander myCommander;
  protected String myName;
  private double myPowerCost;
  public final double baseCost;

  public CommanderAbility(Commander commander, String abilityName, int powerCost)
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
  
  /** Final method to do some bookkeeping, and then call
   * perform() do do the actual work. This allows us to
   * manage global Ability side-effects in one place. */
  public final void activate(GameMap gameMap)
  {
    if( myCommander.getAbilityPower() < myPowerCost )
    {
      System.out.println("WARNING!: Performing ability with insufficient ability power!");
    }

    myCommander.activateAbility(this);
    
    perform(gameMap);
  }

  /** Subclasses will override this method to enact the ability's effects. */
  protected abstract void perform(GameMap gameMap);
}
