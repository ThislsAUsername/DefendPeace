package CommandingOfficers;

import Terrain.GameMap;
import Terrain.MapMaster;

public abstract class CommanderAbility
{
  public final Commander myCommander;
  protected String myName;
  private int myPowerCost;

  public CommanderAbility(Commander commander, String abilityName, int powerCost)
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

    // Increase the cost of this ability for next time to mitigate spam and
    // accommodate the presumably-growing battlefront.
    // Cost grows by at least one, and at most 10% of the current cost.
    myPowerCost += Math.max(myPowerCost*0.1, 1);
    
    perform(gameMap);
  }

  /** Subclasses will override this method to enact the ability's effects. */
  protected abstract void perform(MapMaster gameMap);
}
