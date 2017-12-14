package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import Terrain.GameMap;
import Terrain.Location;
import Units.AAModel;
import Units.APCModel;
import Units.ArtilleryModel;
import Units.InfantryModel;
import Units.MDTankModel;
import Units.MechModel;
import Units.MissilesModel;
import Units.NeoTankModel;
import Units.ReconModel;
import Units.RocketsModel;
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
  public UnitModel[] unitModels;
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
    unitModels = new UnitModel[11];
    unitModels[0] = new InfantryModel();
    unitModels[1] = new MechModel();
    unitModels[2] = new APCModel();
    unitModels[3] = new ArtilleryModel();
    unitModels[4] = new ReconModel();
    unitModels[5] = new TankModel();
    unitModels[6] = new MDTankModel();
    unitModels[7] = new NeoTankModel();
    unitModels[8] = new RocketsModel();
    unitModels[9] = new AAModel();
    unitModels[10] = new MissilesModel();
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

    for( int i = 0; i < unitModels.length; ++i )
    {
      if( unitModels[i].type == unitType )
      {
        um = unitModels[i];
        break;
      }
    }

    return um;
  }

  public ArrayList<UnitModel> getShoppingList()
  { // TODO: will eventually need to take in terrainType so it can separate out air/ground/navy
    ArrayList<UnitModel> arrList = new ArrayList<UnitModel>();
    for( int i = 0; i < unitModels.length; i++ )
    {
      arrList.add(unitModels[i]);
    }

    return arrList;
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
