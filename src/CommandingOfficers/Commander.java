package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import Terrain.Environment.Terrains;
import Terrain.GameMap;
import Terrain.Location;
import Units.AntiAirModel;
import Units.APCModel;
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
import CommandingOfficers.Modifiers.COModifier;
import Engine.BattleInstance;
import Engine.GameInstance;
import Units.UnitModel.UnitEnum;

public class Commander
{
  public final CommanderInfo coInfo;
  public ArrayList<Unit> units;
  public ArrayList<UnitModel> landModels;
  public ArrayList<UnitModel> seaModels;
  public ArrayList<UnitModel> airModels;
  public ArrayList<COModifier> modifiers;
  public Color myColor;
  public static final int DEFAULTSTARTINGMONEY = 10000;
  public int money = 0;
  public int incomePerCity = 1000;
  public boolean isDefeated = false;
  public Location HQLocation = null;

  public Commander(CommanderInfo info)
  {
    coInfo = info;
    // TODO Obviously we don't want to hard-code the UnitModel array.
    landModels = new ArrayList<UnitModel>(11);
    landModels.add(new InfantryModel());
    landModels.add(new MechModel());
    landModels.add(new APCModel());
    landModels.add(new ArtilleryModel());
    landModels.add(new ReconModel());
    landModels.add(new TankModel());
    landModels.add(new MDTankModel());
    landModels.add(new NeotankModel());
    landModels.add(new RocketsModel());
    landModels.add(new AntiAirModel());
    landModels.add(new MobileSAMModel());

    seaModels = new ArrayList<UnitModel>(4);
    seaModels.add(new LanderModel());
    seaModels.add(new CruiserModel());
    seaModels.add(new SubModel());
    seaModels.add(new BattleshipModel());

    airModels = new ArrayList<UnitModel>(0);
    airModels.add(new TCopterModel());
    airModels.add(new BCopterModel());
    airModels.add(new FighterModel());
    airModels.add(new BomberModel());
    
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

    for( int i = 0; i < landModels.size(); ++i )
    {
      if( landModels.get(i).type == unitType )
      {
        um = landModels.get(i);
        break;
      }
    }

    return um;
  }

  public ArrayList<UnitModel> getShoppingList(Terrains buyLocation)
  { // TODO: will eventually need to take in terrainType so it can separate out air/ground/navy
    switch(buyLocation)
    {
      case AIRPORT:
        return airModels;
      case SEAPORT:
        return seaModels;
      default:
        return landModels;
    }
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
