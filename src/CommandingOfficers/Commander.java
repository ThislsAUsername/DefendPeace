package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayDeque;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import java.util.Set;

import AI.AIController;
import AI.AILibrary;
import AI.AIMaker;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameAction;
import Engine.XYCoord;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.Combat.BattleInstance.CombatContext;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import UI.Art.SpriteArtist.SpriteLibrary;
import Units.APCModel;
import Units.AntiAirModel;
import Units.ArtilleryModel;
import Units.BCopterModel;
import Units.BattleshipModel;
import Units.BomberModel;
import Units.CruiserModel;
import Units.FighterModel;
import Units.InfantryModel;
import Units.LanderModel;
import Units.MDTankModel;
import Units.MechModel;
import Units.MobileSAMModel;
import Units.NeotankModel;
import Units.ReconModel;
import Units.RocketsModel;
import Units.SubModel;
import Units.TCopterModel;
import Units.TankModel;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;

public class Commander extends GameEventListener implements Serializable
{
  private static final long serialVersionUID = -6740892333060450105L;
  
  public final CommanderInfo coInfo;
  public GameMap myView;
  public ArrayList<Unit> units;
  public ArrayList<UnitModel> unitModels = new ArrayList<UnitModel>();
  public transient Map<TerrainType, ArrayList<UnitModel>> unitProductionByTerrain;
  public Set<XYCoord> ownedProperties;
  public ArrayDeque<COModifier> modifiers;
  public Color myColor;
  public String factionName = SpriteLibrary.DEFAULT_SPRITE_KEY;
  public static final int DEFAULTSTARTINGMONEY = 0;
  public static final int CHARGERATIO_FUNDS = 9000; // quantity of funds damage to equal 1 unit of power charge
  public static final int CHARGERATIO_HP = 0; // Funds value of 1 HP damage dealt, for the purpose of power charge
  public int money = 0;
  public int incomePerCity = 1000;
  public int team = -1;
  public boolean isDefeated = false;
  public XYCoord HQLocation = null;
  private double myAbilityPower = 0;
  private int numTowers = 0;

  private ArrayList<CommanderAbility> myAbilities = null;
  private String myActiveAbilityName = "";
  private TowerListener myTowerListener = null;

  // The AI has to be effectively stateless anyway (to be able to adapt to whatever scenario it finds itself in on map start),
  //   so may as well not require them to care about serializing their contents.
  private transient AIController aiController = null;

  public Commander(CommanderInfo info)
  {
    coInfo = info;

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
    factoryModels.add(new RocketsModel());
    factoryModels.add(new AntiAirModel());
    factoryModels.add(new MobileSAMModel());

    // Record those units we can get from a Seaport.
    seaportModels.add(new LanderModel());
    seaportModels.add(new CruiserModel());
    seaportModels.add(new SubModel());
    seaportModels.add(new BattleshipModel());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new TCopterModel());
    airportModels.add(new BCopterModel());
    airportModels.add(new FighterModel());
    airportModels.add(new BomberModel());

    // Dump these lists into a hashmap for easy reference later.
    unitProductionByTerrain = new HashMap<TerrainType, ArrayList<UnitModel>>();
    unitProductionByTerrain.put(TerrainType.FACTORY, factoryModels);
    unitProductionByTerrain.put(TerrainType.SEAPORT, seaportModels);
    unitProductionByTerrain.put(TerrainType.AIRPORT, airportModels);

    // Compile one master list of everything we can build.
    unitModels.addAll(factoryModels);
    unitModels.addAll(seaportModels);
    unitModels.addAll(airportModels);

    modifiers = new ArrayDeque<COModifier>();
    units = new ArrayList<Unit>();
    ownedProperties = new HashSet<XYCoord>();
    money = DEFAULTSTARTINGMONEY;

    myAbilities = new ArrayList<CommanderAbility>();
    
    // Handles tower damage boost
    myTowerListener = new TowerListener(this);
    GameEventListener.registerEventListener(myTowerListener);
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
    mod.apply(this);
    modifiers.offer(mod); // Add to the list so the modifier can be reverted next turn.
  }

  public void endTurn()
  {
    if( aiController != null ) aiController.endTurn();
    myView.resetFog();
  }
  
  public void adjustTowerPower() {
	  // First, revert all previous damage modifiers
	  new CODamageModifier(10*numTowers).revert(this);
	  // Count number of towers, and apply new modifiers
	  numTowers = 0;
	  for (XYCoord xy : this.ownedProperties) 
	  {
		  Location loc = this.myView.getLocation(xy);
		  if (loc.getEnvironment().terrainType.equals(TerrainType.TOWER)
			  && loc.getOwner() == this) 
		  {
			  numTowers += 1;
		  }
	  }
	  new CODamageModifier(10*numTowers).apply(this);
  }
  
  private static class TowerListener extends GameEventListener{
	  private Commander myCommander = null;
	  public TowerListener(Commander myCo) {
		  myCommander = myCo;
	  }
	  
	  @Override
	  public void receiveCaptureEvent(Unit unit, Location location) {
		  if (unit.CO == myCommander && location.getEnvironment().terrainType.equals(TerrainType.TOWER))
		  {
			  myCommander.adjustTowerPower();
		  }
	  }
  }

  /**
   * Collect income, adjust tower power, and handle any COModifiers.
   * @param map
   */
  public GameEventQueue initTurn(GameMap map)
  {
    myView.resetFog();
    adjustTowerPower();
    myActiveAbilityName = "";
    // Accrue income for each city under your control.
    int turnIncome = 0;
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        Location loc = map.getLocation(w, h);
        if( loc.isProfitable() && loc.getOwner() == this )
        {
          turnIncome += incomePerCity;
        }
      }
    }
    money += turnIncome;

    // Un-apply any modifiers that were activated last turn.
    // TODO: If/when we have modifiers that last multiple turns, figure out how to handle them.
    while (!modifiers.isEmpty())
    {
      modifiers.poll().revert(this);
    }

    if( null != aiController )
    {
      aiController.initTurn(myView);
    }

    return new GameEventQueue();
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

  public UnitModel getUnitModel(UnitEnum unitType)
  {
    UnitModel um = null;

    for( UnitModel iter : unitModels )
    {
      if( iter.type == unitType )
      {
        um = iter;
        break;
      }
    }

    return um;
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
    return (unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType) != null)
        ? unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType) : new ArrayList<UnitModel>();
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

    for( CommanderAbility ca : myAbilities )
    {
      ca.increaseCost(ca.baseCost * 0.2);
    }
    // default power boost
    addCOModifier(new CODamageModifier(10));
    addCOModifier(new CODefenseModifier(10));
    myActiveAbilityName = ability.toString();
  }

  public void modifyAbilityPower(double amount)
  {
    myAbilityPower += amount;
    if( myAbilityPower < 0 )
      myAbilityPower = 0;
    double maxPower = 0;
    for( CommanderAbility ca : myAbilities )
    {
      if( maxPower < ca.getCost() )
      {
        maxPower = ca.getCost();
      }
    }
    if( myAbilityPower > maxPower )
      myAbilityPower = maxPower;
  }

  public int getRepairPower()
  {
    return 2;
  }

  /**
   * Track battles that happen, and get ability power based on combat this CO is in.
   */
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
    if( minion != null && enemy != null && myActiveAbilityName.equalsIgnoreCase("") )
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
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();

    // save our index into the AILibrary
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

    // Write out our shopping list as a flattened 2D array of booleans; for each terrain, for all unit types, can I build it?
    // This is probably inefficient use of space, but just look at all this not-caring I can do!
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      for( UnitEnum ue : UnitModel.UnitEnum.values() )
      {
        ArrayList<UnitModel> shoppingList = (unitProductionByTerrain.get(terrain) != null) ? unitProductionByTerrain.get(terrain)
            : new ArrayList<UnitModel>();
        stream.writeBoolean(shoppingList.contains(getUnitModel(ue)));
      }
    }
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void readObject(ObjectInputStream stream)
          throws IOException, ClassNotFoundException {
    stream.defaultReadObject();

    // use our AI index to get back where we were before
    aiController = AILibrary.getAIList().get(stream.readInt()).create(this);

    // rebuild our shopping list
    unitProductionByTerrain = new HashMap<TerrainType, ArrayList<UnitModel>>();
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      ArrayList<UnitModel> shoppingList = new ArrayList<UnitModel>();
      for( UnitEnum ue : UnitModel.UnitEnum.values() )
      {
        if( stream.readBoolean() )
          shoppingList.add(getUnitModel(ue));
      }
      unitProductionByTerrain.put(terrain, shoppingList);
    }
  }
}
