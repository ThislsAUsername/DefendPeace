package CommandingOfficers;

import Terrain.MapMaster;

public abstract class CommanderAbility
{
  public static final int PHASE_TURN_START = 1;
  public static final int PHASE_BUY = PHASE_TURN_START << 1;
  public static final int PHASE_TURN_END = PHASE_BUY << 1;
  public final Commander myCommander;
  protected String myName;
  private double myPowerCost;
  public final double baseCost;

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
    
    perform(gameMap);
  }

  /** Subclasses will override this method to enact the ability's effects. */
  protected abstract void perform(MapMaster gameMap);
}
