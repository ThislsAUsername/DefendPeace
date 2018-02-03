package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import CommandingOfficers.Modifiers.COModifier;
import Engine.BattleInstance;
import Engine.GameInstance;
import Engine.XYCoord;
import Terrain.Environment.Terrains;
import Terrain.GameMap;
import Terrain.Location;
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
  public ArrayList<UnitModel> unitModels;
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

    // TODO Obviously we don't want to hard-code the UnitModel array.
    unitModels = new ArrayList<UnitModel>(19);
    unitModels.add(new InfantryModel());
    unitModels.add(new MechModel());
    unitModels.add(new APCModel());
    unitModels.add(new ArtilleryModel());
    unitModels.add(new ReconModel());
    unitModels.add(new TankModel());
    unitModels.add(new MDTankModel());
    unitModels.add(new NeotankModel());
    unitModels.add(new RocketsModel());
    unitModels.add(new AntiAirModel());
    unitModels.add(new MobileSAMModel());

    unitModels.add(new LanderModel());
    unitModels.add(new CruiserModel());
    unitModels.add(new SubModel());
    unitModels.add(new BattleshipModel());

    unitModels.add(new TCopterModel());
    unitModels.add(new BCopterModel());
    unitModels.add(new FighterModel());
    unitModels.add(new BomberModel());
    
    modifiers = new ArrayList<COModifier>();
    units = new ArrayList<Unit>();
    money = DEFAULTSTARTINGMONEY;
  }

  /**
   * Allows a Commander to inject modifications before evaluating a battle.
   * Simple damage buffs, etc. can be accomplished via COModifiers, but effects
   * that depend on circumstances that must be evaluated at combat time (e.g. a
   * terrain-based firepower bonus) can be handled here.
   */
  public void applyCombatModifiers( BattleInstance params ) {}

  public void addCOModifier( COModifier mod )
  {
    mod.apply(this);
    modifiers.add( mod ); // Add to the list so the modifier can be reverted next turn.
  }

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

    for( Unit unit : units )
    {
      unit.initTurn(map.getLocation(unit.x, unit.y));
    }
  }

  public UnitModel getUnitModel(UnitEnum unitType)
  {
    UnitModel um = null;

    for( int i = 0; i < unitModels.size(); ++i )
    {
      if( unitModels.get(i).type == unitType )
      {
        um = unitModels.get(i);
        break;
      }
    }

    return um;
  }

  public ArrayList<UnitModel> getShoppingList(Terrains buyLocation)
  {
    ArrayList<UnitModel> shoppingList = new ArrayList<UnitModel>();
    switch(buyLocation)
    {
      case AIRPORT:
        for( int i = 0; i < unitModels.size(); i++ )
        {
          UnitModel.ChassisEnum chassis = unitModels.get(i).chassis;
          if (UnitModel.ChassisEnum.AIR_HIGH == chassis || UnitModel.ChassisEnum.AIR_LOW == chassis)
            shoppingList.add(unitModels.get(i));
        }
        break;
      case SEAPORT:
        for( int i = 0; i < unitModels.size(); i++ )
        {
          UnitModel.ChassisEnum chassis = unitModels.get(i).chassis;
          if (UnitModel.ChassisEnum.SHIP == chassis)
            shoppingList.add(unitModels.get(i));
        }
        break;
      default:
        for( int i = 0; i < unitModels.size(); i++ )
        {
          UnitModel.ChassisEnum chassis = unitModels.get(i).chassis;
          if (UnitModel.ChassisEnum.TROOP == chassis || UnitModel.ChassisEnum.TRUCK == chassis || UnitModel.ChassisEnum.TANK == chassis)
            shoppingList.add(unitModels.get(i));
        }
        break;
    }
    return shoppingList;
  }

  public ArrayList<String> getReadyAbilities()
  {
    System.out.println("WARNING! Calling getReadyAbilities on Commander base class!");
    ArrayList<String> coas = new ArrayList<String>();
    return coas;
  }

  public void doAbility( String abilityName, GameInstance game )
  {
    System.out.println("WARNING! Calling doAbility on Commander base class!");
  }
}
