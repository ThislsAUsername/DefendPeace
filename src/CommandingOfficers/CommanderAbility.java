package CommandingOfficers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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

  /** A separate class that enables sharing of cost-basis information across multiple powers. */
  public static class CostBasis implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public int numCasts; // How many times the power has been used.
    public final int baseStarRatio; // The starting energy cost of each star; all abilities for a given Commander should use the same charge units
    public int starRatioPerCast; // Extra energy cost per star per cast
    public int maxScalingCasts; // Cap on cast scaling
    /** The ratio used when numCasts >= maxScalingCasts.<p>
     * Cart logic is buggy, so not necessarily == maxScalingCasts * starRatioPerCast
     */
    public int maxStarRatio;
    public CostBasis(int chargeRatio)
    {
      numCasts = 0;
      baseStarRatio = chargeRatio;
      starRatioPerCast = chargeRatio / 5; // Default is 20% of base per cast, i.e. 1800 funds
      maxScalingCasts = 10;
      maxStarRatio = chargeRatio + maxScalingCasts * starRatioPerCast;
    }
    public int calcCostPerStar() { return calcCostPerStar(numCasts); }
    public int calcCostPerStar(int numCasts)
    {
      int cost = baseStarRatio;
      if( numCasts < maxScalingCasts )
        cost += numCasts * starRatioPerCast;
      else
        cost = maxStarRatio;
      return cost;
    }
    public int calcCost(int stars)
    {
      int costRatio = calcCostPerStar();
      return stars * costRatio;
    }
  }
  public final CostBasis costBasis;
  public final int baseStars; // The number of "stars" this power initially cost
  public int AIFlags = PHASE_TURN_START;
  private ArrayList<UnitModifier> modsApplied = new ArrayList<>();

  public CommanderAbility(Commander commander, String abilityName, int stars)
  {
    this(commander, abilityName, stars, Commander.CHARGERATIO_FUNDS);
  }
  public CommanderAbility(Commander commander, String abilityName, int stars, int chargeRatio)
  {
    this(commander, abilityName, stars, new CostBasis(chargeRatio));
  }
  public CommanderAbility(Commander commander, String abilityName, int stars, CostBasis basis)
  {
    myCommander = commander;
    myName = abilityName;
    baseStars = stars;
    costBasis = basis;
  }
  public void initForGame(GameInstance game)
  {}
  public void deInitForGame(GameInstance game)
  {}

  /**
   * ...in whatever unit of measurement the basis uses.<p>
   * AW1-2 and BW use (CO-modifiable) funds value. That's also our default.<p>
   * DoR uses HP.<p>
   * DS uses UnitModel.abilityPowerValue.
   */
  public int getCost()
  {
    return costBasis.calcCost(baseStars);
  }

  @Override
  public String toString()
  {
    return myName;
  }

  /** Method to do some bookkeeping, and then call perform() to execute instant effects
   * that don't have an associated event.<p>
   * It's final to allow us to manage global Ability side-effects in one place. */
  public final void activate(MapMaster gameMap)
  {
    if( myCommander.getAbilityPower() < getCost() )
    {
      System.out.println("WARNING!: Performing ability with insufficient ability power!");
    }

    myCommander.activateAbility(this, gameMap);
    applyUnitModifiers(gameMap);

    costBasis.numCasts += 1;
    perform(gameMap);
  }
  private final void applyUnitModifiers(MapMaster gameMap)
  {
    ArrayList<UnitModifier> unitModsToApply = new ArrayList<>();
    enqueueUnitMods(gameMap, unitModsToApply);
    modsApplied.addAll(unitModsToApply);
    for( UnitModifier mod : unitModsToApply )
      myCommander.army.addUnitModifier(mod);
  }

  /**
   * Public hook to handle cleanup.
   */
  public final void deactivate(MapMaster gameMap)
  {
    // Revert in reverse order, just to be safe
    for( int i = modsApplied.size() - 1; i >= 0; --i )
      myCommander.army.removeUnitModifier(modsApplied.get(i));
    modsApplied.clear();
    revert(gameMap);
  }

  /** Subclasses can override this method to enact any ability effects that don't go in an event or a UnitModifier.
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
  public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList) {}

  public Collection<DamagePopup> getDamagePopups(GameMap map)
  {
    return new ArrayList<DamagePopup>();
  }
}
