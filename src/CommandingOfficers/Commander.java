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
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitModList;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.MapPerspective;
import Terrain.TerrainType;
import UI.GameOverlay;
import UI.UIUtils.Faction;
import UI.UnitMarker;
import Units.Unit;
import Units.UnitContext;
import Units.UnitDelta;
import Units.UnitModel;
import Units.UnitModelScheme.GameReadyModels;

public class Commander implements GameEventListener, Serializable, UnitModifierWithDefaults, UnitModList, UnitMarker
{
  private static final long serialVersionUID = 1L;

  public Army army;
  public final CommanderInfo coInfo;
  public final GameScenario.GameRules gameRules;
  public MapPerspective myView;
  public ArrayList<Unit> units;
  public ArrayList<UnitModel> unitModels = new ArrayList<UnitModel>();
  public Map<TerrainType, ArrayList<UnitModel>> unitProductionByTerrain = new HashMap<>();
  public Set<XYCoord> ownedProperties;
  public Color myColor;
  public Faction faction;
  public static final int CHARGERATIO_FUNDS = 9000; // quantity of funds damage to equal 1 unit of power charge
  public static final int CHARGERATIO_HP = 100; // Funds value of 1 HP damage dealt, for the purpose of power charge
  public int incomeAdjustment = 0; // Commander subclasses can increase/decrease income if needed.
  private double myAbilityPower = 0;

  private ArrayList<CommanderAbility> myAbilities = null;
  private CommanderAbility myActiveAbility = null;

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
    for( UnitModel um : GRMs.unitModels )
    {
      unitModels.add(um);
    }

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

  protected void addCommanderAbility(CommanderAbility ca)
  {
    myAbilities.add(ca);
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

    if( null != myActiveAbility )
    {
      events.addAll(myActiveAbility.getRevertEvents(map));
      myActiveAbility.deactivate(map);
      myActiveAbility = null;
    }

    for( Unit u : units )
    {
      events.addAll(u.initTurn(map));
    }

    return events;
  }

  /**
   * @return whether these COs would like to kill each other
   */
  public boolean isEnemy(Commander other)
  {
    // If the other CO doesn't exist, we can't be friends.
    if( other == null )
      return true;
    return army.isEnemy(other.army);
  }

  public UnitModel getUnitModel(long unitRole)
  {
    return getUnitModel(unitRole, true);
  }
  public UnitModel getUnitModel(long unitRole, boolean matchOnAny)
  {
    UnitModel um = null;

    for( UnitModel iter : unitModels )
    {
      boolean some = iter.isAny(unitRole);
      boolean all = iter.isAll(unitRole);
      if( all || (some && matchOnAny) )
      {
        um = iter;
        break;
      }
    }

    return um;
  }

  public ArrayList<UnitModel> getAllModels(long unitRole)
  {
    return getAllModels(unitRole, true);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny)
  {
    return getAllModels(unitRole, matchOnAny, 0);
  }
  public ArrayList<UnitModel> getAllModelsNot(long excludedUnitRoles)
  {
    long allFlags = ~0;
    return getAllModels(allFlags, true, excludedUnitRoles);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny, long excludedRoles)
  {
    ArrayList<UnitModel> models = new ArrayList<UnitModel>();

    for( UnitModel iter : unitModels )
    {
      boolean some = iter.isAny(unitRole) && iter.isNone(excludedRoles);
      boolean all = iter.isAll(unitRole) && iter.isNone(excludedRoles);
      if( all || (some && matchOnAny) )
      {
        models.add(iter);
      }
    }

    return models;
  }

  @Override
  public Color getMarkingColor(Unit unit)
  {
    return myColor;
  }

  /** Get the list of units this army can build from the given property type. */
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

  public double[] getAbilityCosts()
  {
    double[] costs = new double[myAbilities.size()];
    for( int i = 0; i < myAbilities.size(); ++i )
    {
      costs[i] = myAbilities.get(i).getCost();
    }
    return costs;
  }

  public double getAbilityPower()
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

  public void modifyAbilityPower(double amount)
  {
    myAbilityPower += amount;
    if( myAbilityPower < 0 )
      myAbilityPower = 0;
    double maxPower = getMaxAbilityPower();
    if( myAbilityPower > maxPower )
      myAbilityPower = maxPower;
  }

  /**
   * @return The maximum level of ability energy this CO can hold
   */
  public double getMaxAbilityPower()
  {
    double maxPower = 0;
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
    return 2;
  }

  public ArrayList<GameOverlay> getMyOverlays(GameMap gameMap, boolean amIViewing)
  {
    ArrayList<GameOverlay> overlays = new ArrayList<GameOverlay>();
    return overlays;
  }

  // Note: Cart charge only uses whole HP, so that's what we're doing, too.
  public double calculateCombatCharge(UnitDelta minion, UnitDelta enemy)
  {
    if( minion == null || enemy == null )
      return 0;

    double myHPLoss = minion.getHPDamage();
    double myHPDealt = enemy.getHPDamage();

    double power = 0; // value in funds of the charge we're getting

    // Add up the funds value of the damage done to both participants.
    power += myHPLoss / UnitModel.MAXIMUM_HP * minion.unit.getCost();
    // The damage we deal is worth half as much as the damage we take, to help powers be a comeback mechanic.
    power += myHPDealt / UnitModel.MAXIMUM_HP * enemy.unit.getCost() / 2;
    // Add power based on HP damage dealt; rewards aggressiveness.
    power += myHPDealt * CHARGERATIO_HP;

    // Convert funds to ability power units
    power /= CHARGERATIO_FUNDS;

    return power;
  }
  public double calculateMassDamageCharge(Unit minion, int lostHP)
  {
    if( minion == null )
      return 0;

    double power = 0; // value in funds of the charge we're getting

    power += ((double)lostHP) / UnitModel.MAXIMUM_HP * getCost(minion.model);

    // Convert funds to ability power units
    power /= CHARGERATIO_FUNDS;

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
      if( myView.getLocation(coord).getOwner() == this && myView.getLocation(coord).isProfitable() ) ++count;
    }
    return count * (gameRules.incomePerCity + incomeAdjustment);
  }

  private final ArrayList<UnitModifier> unitMods = new ArrayList<UnitModifier>();
  @Override
  public List<UnitModifier> getModifiers()
  {
    // Intended order of operations: model, D2D, environment, abilities, unit-specific
    ArrayList<UnitModifier> output = new ArrayList<UnitModifier>();
    output.add(this);
    output.addAll(unitMods);
    output.addAll(this.army.getModifiers());
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
