package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import Units.APCModel;
import Units.InfantryModel;
import Units.MechModel;
import Units.Unit;
import Units.UnitModel;
import CommandingOfficers.Modifiers.COModifier;
import Units.UnitModel.UnitEnum;

public class Commander
{
  public final CommanderInfo coInfo;
  public ArrayList<Unit> units;
  public UnitModel[] unitModels;
  public ArrayList<COModifier> modifiers;
  public Color myColor;
  public int starsMinor;
  public int starsMax;
  public double starsCurrent = 0;
  public static final int DEFAULTSTARTINGMONEY = 10000;
  public int money = 0;
  public int incomePerCity = 1000;
  public boolean isDefeated = false;
  public boolean powerActive = false;
  public Location HQLocation = null;

  public void doAbilityMinor()
  {}
  public void doAbilityMajor()
  {}
  
  public void addStars(double change)
  {
    starsCurrent += change;
    // clamp it to the valid range
    starsCurrent = Math.max(0, Math.min(starsMax, starsCurrent));
    // TODO: get a UI element for the CO power meter. And probably rename the variables
    System.out.print(coInfo.name + " stars: " + starsCurrent);
  }

  public Commander(CommanderInfo info)
  {
    coInfo = info;
    // TODO Obviously we don't want to hard-code the UnitModel array.
    unitModels = new UnitModel[3];
    unitModels[0] = new InfantryModel();
    unitModels[1] = new MechModel();
    unitModels[2] = new APCModel();
    modifiers = new ArrayList<COModifier>();
    units = new ArrayList<Unit>();
    money = DEFAULTSTARTINGMONEY;
    starsMinor = -1;
    starsMax = -1;
  }

  public void initTurn(GameMap map)
  {
    // Accrue income for each city under your control.
    int turnIncome = 0;
    powerActive = false;
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

  public UnitEnum[] getShoppingList()
  { // TODO: will eventually need to take in terrainType so it can separate out air/ground/navy
    ArrayList<UnitEnum> arrList = new ArrayList<UnitEnum>();
    for( int i = 0; i < unitModels.length; i++ )
    {
      arrList.add(unitModels[i].type);
    }
    UnitEnum[] returned = new UnitEnum[0];
    return arrList.toArray(returned);
  }
}
