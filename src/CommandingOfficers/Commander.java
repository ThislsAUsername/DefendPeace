package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Modifiers.COModifier;
import Engine.GameInstance;
import Engine.XYCoord;
import Engine.Combat.BattleInstance;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.Types.Airport;
import Terrain.Types.BaseTerrain;
import Terrain.Types.Factory;
import Terrain.Types.Seaport;
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

public class Commander
{
  public final CommanderInfo coInfo;
  public ArrayList<Unit> units;
  public ArrayList<UnitModel> unitModels = new ArrayList<UnitModel>();
  public Map<BaseTerrain, ArrayList<UnitModel>> unitProductionByTerrain;
  public ArrayList<Location> ownedProperties;
  public ArrayList<COModifier> modifiers;
  public Color myColor;
  public static final int DEFAULTSTARTINGMONEY = 10000;
  public int money = 0;
  public int incomePerCity = 1000;
  public boolean isDefeated = false;
  public XYCoord HQLocation = null;

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
    unitProductionByTerrain = new HashMap<BaseTerrain, ArrayList<UnitModel>>();
    unitProductionByTerrain.put(Factory.getInstance(), factoryModels);
    unitProductionByTerrain.put(Seaport.getInstance(), seaportModels);
    unitProductionByTerrain.put(Airport.getInstance(), airportModels);

    // Compile one master list of everything we can build.
    unitModels.addAll(factoryModels);
    unitModels.addAll(seaportModels);
    unitModels.addAll(airportModels);

    modifiers = new ArrayList<COModifier>();
    units = new ArrayList<Unit>();
    ownedProperties = new ArrayList<Location>();
    money = DEFAULTSTARTINGMONEY;
  }

  /**
   * Allows a Commander to inject modifications before evaluating a battle.
   * Simple damage buffs, etc. can be accomplished via COModifiers, but effects
   * that depend on circumstances that must be evaluated at combat time (e.g. a
   * terrain-based firepower bonus) can be handled here.
   */
  public void applyCombatModifiers(BattleInstance params)
  {}

  public void addCOModifier(COModifier mod)
  {
    mod.apply(this);
    modifiers.add(mod); // Add to the list so the modifier can be reverted next turn.
  }

  /**
   * Collect income and handle any COModifiers.
   * @param map
   */
  public void initTurn(GameMap map)
  {
    // Accrue income for each city under your control.
    int turnIncome = 0;
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        Location loc = map.getLocation(w, h);
        if( loc.isCaptureable() && loc.getOwner() == this )
        {
          turnIncome += incomePerCity;
        }
      }
    }
    money += turnIncome;

    // Un-apply any modifiers that were activated last turn.
    // TODO: If/when we have modifiers that last multiple turns, figure out how to handle them.
    for( int i = modifiers.size() - 1; i >= 0; --i )
    {
      modifiers.get(i).revert(this);
      modifiers.remove(i);
    }
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

  /** Get the list of units this commander can build from the given property type. */
  public ArrayList<UnitModel> getShoppingList(BaseTerrain buyLocation)
  {
    return unitProductionByTerrain.get(buyLocation);
  }

  public ArrayList<String> getReadyAbilities()
  {
    System.out.println("WARNING! Calling getReadyAbilities on Commander base class!");
    ArrayList<String> coas = new ArrayList<String>();
    return coas;
  }

  public void doAbility(String abilityName, GameInstance game)
  {
    System.out.println("WARNING! Calling doAbility on Commander base class!");
  }
}
