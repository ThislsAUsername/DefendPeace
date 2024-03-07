package CommandingOfficers;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.GameEvents.CommanderAbilityRevertEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitModList;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.GameOverlay;
import UI.UIUtils.Faction;
import UI.UnitMarker;
import Units.Unit;
import Units.UnitContext;
import Units.UnitDelta;
import Units.UnitModel;
import Units.UnitModelList;
import Units.UnitModelScheme.GameReadyModels;

public class Commander implements GameEventListener, Serializable, UnitModifierWithDefaults, UnitModList, UnitMarker
{
  private static final long serialVersionUID = 1L;

  public Army army;
  public final CommanderInfo coInfo;
  public final GameScenario.GameRules gameRules;
  public ArrayList<Unit> units;
  public final UnitModelList unitModels;
  public Map<TerrainType, ArrayList<UnitModel>> unitProductionByTerrain = new HashMap<>();
  public int luck = 10; // The number we normally plug into the RNG for luck damage
  public Set<XYCoord> ownedProperties;
  public Color myColor;
  public Faction faction;
  public static final int CHARGERATIO_FUNDS = 9000; // quantity of funds damage to equal 1 unit of power charge
  public static final int CHARGERATIO_HP = 100; // Funds value of 10 HP damage dealt, for the purpose of power charge
  public int incomeAdjustment = 0; // Commander subclasses can increase/decrease income if needed.
  private int myAbilityPower = 0;
  public boolean roundUpRepairs = true; // I blame AWBW
  public boolean aw1Combat = false; // See docs in StrikeParams

  // These negate debuffs, not movecosts. They're fixed params on the CO since that's how weather immunity tends to go. KISS and all.
  public boolean immuneToCold   = false; // Chill/Sleet
  public boolean immuneToClouds = false; // Rain/Smoke vision debuffs
  public boolean immuneToSand   = false; // Sandstorm/Sirocco

  public ArrayList<CommanderAbility> myAbilities = null;
  protected CommanderAbility myActiveAbility = null;

  public Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    coInfo = info;
    gameRules = rules;

    // Fetch our fieldable unit types from the rules
    GameReadyModels GRMs = rules.unitModelScheme.getGameReadyModels();
    // Make our own copy of the shopping list since some COs modify it
    for( TerrainType tt : GRMs.shoppingList.keySet() )
    {
      ArrayList<UnitModel> buyables = new ArrayList<>(GRMs.shoppingList.get(tt));
      unitProductionByTerrain.put(tt, buyables);
    }
    unitModels = GRMs.unitModels;

    units = new ArrayList<Unit>();
    ownedProperties = new HashSet<XYCoord>();

    myAbilities = new ArrayList<CommanderAbility>();
  }

  public void initForGame(GameInstance game)
  {
    this.registerForEvents(game);
    for( CommanderAbility ca : myAbilities )
      ca.initForGame(game);
  }
  public void deInitForGame(GameInstance game)
  {
    this.unregister(game);
    if( null != myActiveAbility )
      myActiveAbility.deactivate(game.gameMap);
    for( CommanderAbility ca : myAbilities )
      ca.deInitForGame(game);
  }

  protected CommanderAbility addCommanderAbility(CommanderAbility ca)
  {
    myAbilities.add(ca);
    return ca;
  }

  public void endTurn()
  {
  }

  /**
   * Deactivate any active ability, and init all units
   */
  public GameEventQueue initTurn(MapMaster map)
  {
    GameEventQueue events = new GameEventQueue();

    events.addAll(revertActiveAbility(map));

    for( Unit u : units )
    {
      events.addAll(u.initTurn(map));
    }

    return events;
  }

  public GameEventQueue revertActiveAbility(MapMaster map)
  {
    GameEventQueue events = new GameEventQueue();

    if( null != myActiveAbility )
    {
      events.add(new CommanderAbilityRevertEvent(myActiveAbility));
      events.addAll(myActiveAbility.getRevertEvents(map));
      myActiveAbility = null;
    }

    return events;
  }

  /**
   * @return whether this CO can currently charge
   */
  public boolean canAcceptCharge()
  {
    return (null == myActiveAbility);
  }

  /**
   * @return whether these COs would like to kill each other
   */
  public boolean isEnemy(Commander other)
  {
    return army.isEnemy(other);
  }
  /**
   * @return whether this CO would like to kill that army
   */
  public boolean isEnemy(Army other)
  {
    return army.isEnemy(other);
  }

  public UnitModel getUnitModel(long unitRole)
  {
    return unitModels.getUnitModel(unitRole);
  }
  public UnitModel getUnitModel(long unitRole, boolean matchOnAny)
  {
    return unitModels.getUnitModel(unitRole, matchOnAny);
  }

  public ArrayList<UnitModel> getAllModels(long unitRole)
  {
    return unitModels.getAllModels(unitRole);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny)
  {
    return unitModels.getAllModels(unitRole, matchOnAny);
  }
  public ArrayList<UnitModel> getAllModelsNot(long excludedUnitRoles)
  {
    return unitModels.getAllModelsNot(excludedUnitRoles);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny, long excludedRoles)
  {
    return unitModels.getAllModels(unitRole, matchOnAny, excludedRoles);
  }

  @Override
  public Color getMarkingColor(Unit unit)
  {
    return myColor;
  }

  /** Get the list of units this Commander can build from the given property type. */
  public ArrayList<UnitModel> getShoppingList(MapLocation buyLocation)
  {
    return (unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType) != null) ? unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType)
        : new ArrayList<UnitModel>();
  }

  private UnitContext getCostContext(UnitModel um, XYCoord coord)
  {
    UnitContext uc = new UnitContext(this, um);
    uc.coord = coord;
    for( UnitModifier mod : getModifiers() )
      mod.modifyCost(uc);
    return uc;
  }
  public int getCost(UnitModel um)
  {
    UnitContext uc = getCostContext(um, null);
    return uc.getCostTotal();
  }
  public int getBuyCost(UnitModel um, XYCoord coord)
  {
    UnitContext uc = getCostContext(um, coord);
    return uc.getCostTotal();
  }
  // Not adding a Produce overload for now since I don't see a simple way to get consistent results pipelined into the displayed buy cost

  /** Return an ArrayList containing every ability this Commander currently has the power to perform. */
  public ArrayList<CommanderAbility> getReadyAbilities()
  {
    ArrayList<CommanderAbility> ready = new ArrayList<CommanderAbility>();
    if( null == myActiveAbility )
    {
      for( CommanderAbility ca : myAbilities )
      {
        if( ca.getCost() <= myAbilityPower )
        {
          ready.add(ca);
        }
      }
    }
    return ready;
  }

  public int[] getAbilityCosts()
  {
    int[] costs = new int[myAbilities.size()];
    for( int i = 0; i < myAbilities.size(); ++i )
    {
      costs[i] = myAbilities.get(i).getCost();
    }
    return costs;
  }

  public int getAbilityPower()
  {
    return myAbilityPower;
  }

  public CommanderAbility getActiveAbility()
  {
    return myActiveAbility;
  }

  /** Lets the commander know that he's using an ability,
   *  and accounts for the cost of using it. */
  public void activateAbility(CommanderAbility ability, MapMaster map)
  {
    modifyAbilityPower(-ability.getCost());
    if( null != myActiveAbility )
      myActiveAbility.deactivate(map);
    myActiveAbility = ability;
  }

  /**
   * Changes the underlying raw energy value for this Commander.<p>
   * If you can use modifyAbilityStars() instead, you probably should.<p>
   * When invoking this, make sure you have knowledge of the right value scale -
   * calling calculateCombatCharge() is a good example of how to get that.<p>
   * If you need more fidelity than "stars" and don't want to write a hook, ask one of the abilities to calcCostPerStar()<p>
   */
  public void modifyAbilityPower(int amount)
  {
    myAbilityPower += amount;
    if( myAbilityPower < 0 )
      myAbilityPower = 0;
    int maxPower = getMaxAbilityPower();
    if( myAbilityPower > maxPower )
      myAbilityPower = maxPower;
  }
  /** Adds N power-fatigue-scaled stars' energy. */
  public void modifyAbilityStars(int stars)
  {
    if( myAbilities.size() < 1 )
      return;
    // Just grab the first ability's star cost since I don't really want to consider what to do under variant star costs.
    int starCost = myAbilities.get(0).costBasis.calcCostPerStar();
    modifyAbilityPower(stars * starCost);
  }

  /**
   * @return The maximum level of ability energy this CO can hold
   */
  public int getMaxAbilityPower()
  {
    int maxPower = 0;
    for( CommanderAbility ca : myAbilities )
    {
      if( maxPower < ca.getCost() )
      {
        maxPower = ca.getCost();
      }
    }
    return maxPower;
  }

  // TODO: determine if this needs parameters, and if so, what?
  public int getRepairPower()
  {
    return 20;
  }

  public ArrayList<GameOverlay> getMyOverlays(GameMap gameMap, boolean amIViewing)
  {
    ArrayList<GameOverlay> overlays = new ArrayList<GameOverlay>();
    return overlays;
  }

  // Note: Cart charge only uses whole HP, so that's what we're doing, too.
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    if( minion == null || enemy == null )
      return 0;

    int guiHPLoss  = minion.getHealthDamage() / 10;
    int guiHPDealt =  enemy.getHealthDamage() / 10;

    int power = 0; // value in funds of the charge we're getting

    // Add up the funds value of the damage done to both participants.
    power += guiHPLoss * minion.unit.getCost() / 10;
    // The damage we deal is worth half as much as the damage we take, to help powers be a comeback mechanic.
    power += guiHPDealt * enemy.unit.getCost() / 10 / 2;
    // Add power based on HP damage dealt; rewards aggressiveness.
    power += guiHPDealt * CHARGERATIO_HP;

    return power;
  }
  public int calculateMassDamageCharge(Unit minion, int lostHealth)
  {
    if( minion == null )
      return 0;

    int power = 0; // value in funds of the charge we're getting

    power += (lostHealth * getCost(minion.model)) / UnitModel.MAXIMUM_HEALTH;

    return power;
  }

  /**
   * Count up the number of profitable properties we own, multiply by the game's income-per-city
   * setting, and tack on any CO-specific income modifier, then return the result.
   */
  public int getIncomePerTurn()
  {
    int count = 0;
    for( XYCoord coord : ownedProperties )
    {
      // Re-check ownership just because.
      if( army.myView.getLocation(coord).getOwner() == this && army.myView.getLocation(coord).isProfitable() ) ++count;
    }
    return count * (gameRules.incomePerCity + incomeAdjustment);
  }

  @Override
  public String toString()
  {
    return coInfo.name;
  }

  private final ArrayList<UnitModifier> unitMods = new ArrayList<UnitModifier>();
  @Override
  public List<UnitModifier> getModifiers()
  {
    // Intended order of operations: model, D2D, environment, abilities, unit-specific
    ArrayList<UnitModifier> output = new ArrayList<UnitModifier>();
    output.add(this);
    output.addAll(unitMods);
    if( null != army )
      output.addAll(army.getModifiers());
    else
      System.out.println("WARNING!: Polling UnitModifiers from a Commander when CO.army == null!");
    return output;
  }

  @Override
  public void addUnitModifier(UnitModifier unitModifier)
  {
    unitMods.add(unitModifier);
  }
  @Override
  public void removeUnitModifier(UnitModifier unitModifier)
  {
    unitMods.remove(unitModifier);
  }
}
