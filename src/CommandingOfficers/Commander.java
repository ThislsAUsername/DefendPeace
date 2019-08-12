package CommandingOfficers;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import AI.AIController;
import AI.AILibrary;
import AI.AIMaker;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameAction;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.Combat.BattleInstance.CombatContext;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.UIUtils.Faction;
import Units.APCModel;
import Units.AntiAirModel;
import Units.ArtilleryModel;
import Units.BBoatModel;
import Units.BBombModel;
import Units.BCopterModel;
import Units.BattleshipModel;
import Units.BomberModel;
import Units.CarrierModel;
import Units.CruiserModel;
import Units.FighterModel;
import Units.InfantryModel;
import Units.LanderModel;
import Units.MDTankModel;
import Units.MechModel;
import Units.MegatankModel;
import Units.MobileSAMModel;
import Units.NeotankModel;
import Units.PiperunnerModel;
import Units.ReconModel;
import Units.RocketsModel;
import Units.StealthHideModel;
import Units.StealthModel;
import Units.SubModel;
import Units.SubSubModel;
import Units.TCopterModel;
import Units.TankModel;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;

public class Commander extends GameEventListener implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public final CommanderInfo coInfo;
  public final GameScenario.GameRules gameRules;
  public GameMap myView;
  public ArrayList<Unit> units;
  public Map<UnitEnum, UnitModel> unitModels = new HashMap<UnitEnum, UnitModel>();
  public Map<TerrainType, ArrayList<UnitModel>> unitProductionByTerrain;
  public Set<XYCoord> ownedProperties;
  public ArrayList<COModifier> modifiers;
  public Color myColor;
  public Faction faction;
  public static final int CHARGERATIO_FUNDS = 9000; // quantity of funds damage to equal 1 unit of power charge
  public static final int CHARGERATIO_HP = 100; // Funds value of 1 HP damage dealt, for the purpose of power charge
  public int money = 0;
  public int incomeAdjustment = 0; // Commander subclasses can increase/decrease income if needed.
  public int team = -1;
  public boolean isDefeated = false;
  public XYCoord HQLocation = null;
  private double myAbilityPower = 0;

  private ArrayList<CommanderAbility> myAbilities = null;
  private String myActiveAbilityName = "";

  // The AI has to be effectively stateless anyway (to be able to adapt to whatever scenario it finds itself in on map start),
  //   so may as well not require them to care about serializing their contents.
  private transient AIController aiController = null;

  public Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    coInfo = info;
    gameRules = rules;

    // TODO We probably don't want to hard-code the buildable units.
    ArrayList<UnitModel> factoryModels = new ArrayList<UnitModel>();
    ArrayList<UnitModel> seaportModels = new ArrayList<UnitModel>();
    ArrayList<UnitModel> airportModels = new ArrayList<UnitModel>();

    // Define everything we can build from a Factory.
    factoryModels.add(new InfantryModel());
    factoryModels.add(new MechModel());
    factoryModels.add(new APCModel());
    factoryModels.add(new ArtilleryModel());
    factoryModels.add(new ReconModel());
    factoryModels.add(new TankModel());
    factoryModels.add(new MDTankModel());
    factoryModels.add(new NeotankModel());
    factoryModels.add(new MegatankModel());
    factoryModels.add(new RocketsModel());
    factoryModels.add(new AntiAirModel());
    factoryModels.add(new MobileSAMModel());
    factoryModels.add(new PiperunnerModel());

    // Record those units we can get from a Seaport.
    seaportModels.add(new LanderModel());
    seaportModels.add(new CruiserModel());
    seaportModels.add(new SubModel());
    seaportModels.add(new BattleshipModel());
    seaportModels.add(new CarrierModel());
    seaportModels.add(new BBoatModel());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new TCopterModel());
    airportModels.add(new BCopterModel());
    airportModels.add(new FighterModel());
    airportModels.add(new BomberModel());
    airportModels.add(new StealthModel());
    airportModels.add(new BBombModel());

    // Dump these lists into a hashmap for easy reference later.
    unitProductionByTerrain = new HashMap<TerrainType, ArrayList<UnitModel>>();
    unitProductionByTerrain.put(TerrainType.FACTORY, factoryModels);
    unitProductionByTerrain.put(TerrainType.SEAPORT, seaportModels);
    unitProductionByTerrain.put(TerrainType.AIRPORT, airportModels);

    // Compile one master list of everything we can build.
    for (UnitModel um : factoryModels)
      unitModels.put(um.type, um);
    for (UnitModel um : seaportModels)
      unitModels.put(um.type, um);
    for (UnitModel um : airportModels)
      unitModels.put(um.type, um);

    UnitModel subsub = new SubSubModel();
    unitModels.put(subsub.type, subsub); // We don't want a separate "submerged sub" build option
    UnitModel stealthy = new StealthHideModel();
    unitModels.put(stealthy.type, stealthy);

    modifiers = new ArrayList<COModifier>();
    units = new ArrayList<Unit>();
    ownedProperties = new HashSet<XYCoord>();

    myAbilities = new ArrayList<CommanderAbility>();
  }

  protected void addCommanderAbility(CommanderAbility ca)
  {
    myAbilities.add(ca);
  }

  /**
   * These functions Allow a Commander to inject modifications before evaluating a battle.
   * Simple damage buffs, etc. can be accomplished via COModifiers, but effects
   * that depend on circumstances that must be evaluated at combat time (e.g. a
   * terrain-based firepower bonus) can be handled here.
   * applyCombatModifiers() will serve for most combat changes, like the above example.
   * changeCombatContext() allows the CO to tweak the BattleInstance itself,
   * to allow for drastic changes to the combat like counterattacking first or at 2+ range.
   */
  public void changeCombatContext(CombatContext instance)
  {}
  /**
   * Allows a Commander to inject modifications that must be evaluated at combat time
   * @param striking - lets the Commander know if it's dealing or taking damage 
   */
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {}

  public void addCOModifier(COModifier mod)
  {
    mod.applyChanges(this);
    modifiers.add(mod); // Add to the list so the modifier can be reverted next turn.
  }

  public void endTurn()
  {
    if( aiController != null ) aiController.endTurn();
    myView.resetFog();
  }

  /**
   * Collect income and handle any COModifiers.
   * @param map
   */
  public GameEventQueue initTurn(MapMaster map)
  {
    myView.resetFog();
    myActiveAbilityName = "";

    // Accrue income for each city under your control.
    money += getIncomePerTurn();

    // Un-apply any modifiers that were activated last turn.
    // TODO: If/when we have modifiers that last multiple turns, figure out how to handle them.
    for( int i = modifiers.size() - 1; i >= 0; --i )
    {
      modifiers.get(i).revertChanges(this);
      modifiers.remove(i);
    }

    if( null != aiController )
    {
      aiController.initTurn(myView);
    }

    GameEventQueue events = new GameEventQueue();
    for( Unit u : units )
    {
      events.addAll(u.initTurn(map));
    }

    return events;
  }

  /**
   * This is called after every GameAction, and between turns, and allows Commanders to inject
   * events that don't arise via normal gameplay. Most Commanders should not need to override this.
   */
  public void pollForEvents(GameEventQueue eventsOut) {}

  /**
   * @return whether these COs would like to kill each other
   */
  public boolean isEnemy(Commander other)
  {
    // If the other CO doesn't exist, we can't be friends.
    if( other == null )
      return true;
    // If the other CO is us, we can't *not* be friends.
    if( other == this )
      return false;
    // If we have no team, we have no friends.
    if( team < 0 || other.team < 0 )
      return true;
    // If we have teams and we're on the same team...
    // we're on the same team.
    if( team == other.team )
      return false;
    // Otherwise, we hate each other.
    return true;
  }

  // Leaving this for now, to avoid merge conflicts
  public UnitModel getUnitModel(UnitEnum unitType)
  {
    return unitModels.get(unitType);
  }
  
  /**
   * Returns a character to be displayed on the unit.
   * Primary usage should be pieces of info that aren't otherwise immediately apparent from the map.
   * Our rendering only supports alphanumeric values at this time.
   */
  public char getUnitMarking(Unit unit)
  {
    // We don't have anything useful to print, so don't.
    return '\0';
  }

  /** Get the list of units this commander can build from the given property type. */
  public ArrayList<UnitModel> getShoppingList(Location buyLocation)
  {
    return (unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType) != null) ? unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType)
        : new ArrayList<UnitModel>();
  }

  /** Return an ArrayList containing every ability this Commander currently has the power to perform. */
  public ArrayList<CommanderAbility> getReadyAbilities()
  {
    ArrayList<CommanderAbility> ready = new ArrayList<CommanderAbility>();
    if( myActiveAbilityName.isEmpty() )
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

  public String getActiveAbilityName()
  {
    return myActiveAbilityName;
  }

  /** Lets the commander know that he's using an ability,
   *  and accounts for the cost of using it. */
  public void activateAbility(CommanderAbility ability)
  {
    modifyAbilityPower(-ability.getCost());
    myActiveAbilityName = ability.toString();
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

  /**
   * Track battles that happen, and get ability power based on combat this CO is in.
   */
  @Override
  public void receiveBattleEvent(final BattleSummary summary)
  {
    // We only care who the units belong to, not who picked the fight. 
    Unit minion = null;
    double myHPLoss = -10;
    Unit enemy = null;
    double myHPDealt = -10;
    if( this == summary.attacker.CO )
    {
      minion = summary.attacker;
      myHPLoss = summary.attackerHPLoss;
      enemy = summary.defender;
      myHPDealt = summary.defenderHPLoss;
    }
    if( this == summary.defender.CO )
    {
      minion = summary.defender;
      myHPLoss = summary.defenderHPLoss;
      enemy = summary.attacker;
      myHPDealt = summary.attackerHPLoss;
    }

    // Do nothing if we're not involved
    if( minion != null && enemy != null )
    {
      double power = 0; // value in funds of the charge we're getting

      // Add up the funds value of the damage done to both participants.
      power += myHPLoss / minion.model.maxHP * minion.model.getCost();
      // The damage we deal is worth half as much as the damage we take, to help powers be a comeback mechanic.
      power += myHPDealt / enemy.model.maxHP * enemy.model.getCost() / 2;
      // Add power based on HP damage dealt; rewards aggressiveness.
      power += myHPDealt * CHARGERATIO_HP;
      
      // Convert funds to ability power units
      power /= CHARGERATIO_FUNDS;

      modifyAbilityPower(power);
    }
  }

  /**
   * Track mass damage done to my units, and get ability power based on it.
   */
  @Override
  public void receiveMassDamageEvent(Map<Unit, Integer> lostHP)
  {
    for( Entry<Unit, Integer> damageEntry : lostHP.entrySet() )
    {
      Unit unit = damageEntry.getKey();
      if (this == unit.CO)
      {
      double power = 0; // value in funds of the charge we're getting

      power += ((double)damageEntry.getValue()) / unit.model.maxHP * unit.model.getCost();

      // Convert funds to ability power units
      power /= CHARGERATIO_FUNDS;

      modifyAbilityPower(power);
      }
    }
  }

  public void setAIController(AIController ai)
  {
    aiController = ai;
  }

  public boolean isAI()
  {
    return (aiController != null);
  }

  public GameAction getNextAIAction(GameMap gameMap)
  {
    if( aiController != null )
    {
      return aiController.getNextAction(myView);
    }
    return null;
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    stream.defaultWriteObject();

    // save our index into the AILibrary
    // TODO: Consider serializing AI as well, so we don't need this method
    if( null == aiController )
      stream.writeInt(0); // Humans live at index 0 of the AI array. That sounds philosophical.
    else
    {
      for( AIMaker AI : AILibrary.getAIList() )
      {
        if( AI.getName().equalsIgnoreCase(aiController.getAIInfo().getName()) )
          stream.writeInt(AILibrary.getAIList().indexOf(AI));
      }
    }
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();

    // use our AI index to get back where we were before
    aiController = AILibrary.getAIList().get(stream.readInt()).create(this);
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
}
