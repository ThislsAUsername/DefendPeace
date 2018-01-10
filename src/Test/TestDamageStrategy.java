package Test;

import java.util.ArrayList;

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
import Units.UnitModel;
import Units.UnitModel.UnitEnum;
import Units.Weapons.WeaponModel;
import Units.Weapons.Damage.DamageStrategy;
import Units.Weapons.Damage.StandardDamage;

public class TestDamageStrategy extends TestCase
{
  private static DamageStrategy standardDamage = new StandardDamage();
  private static ArrayList<UnitModel> unitModels;
  private static ArrayList<WeaponModel> weaponModels;
  private static int numUnitModels = UnitModel.UnitEnum.values().length;
  private static int numWeaponModels = WeaponModel.WeaponType.values().length;

  /** Make two COs and a GameMap to use with this test case. */
  private void setupTest()
  {
    // TODO Obviously we don't want to hard-code the UnitModel array.
    // order is INFANTRY, MECH, RECON, TANK, MD_TANK, NEOTANK, APC, ARTILLERY, ROCKETS, ANTI_AIR, MOBILESAM, FIGHTER, BOMBER, B_COPTER, T_COPTER, BATTLESHIP, CRUISER, LANDER, SUB
    // this order is needed because that's how the unit and weaponmodel enums are currently set up.
    unitModels = new ArrayList<UnitModel>(19);
    unitModels.add(new InfantryModel());
    unitModels.add(new MechModel());
    unitModels.add(new ReconModel());
    unitModels.add(new TankModel());
    unitModels.add(new MDTankModel());
    unitModels.add(new NeotankModel());
    unitModels.add(new APCModel());
    unitModels.add(new ArtilleryModel());
    unitModels.add(new RocketsModel());
    unitModels.add(new AntiAirModel());
    unitModels.add(new MobileSAMModel());

    unitModels.add(new FighterModel());
    unitModels.add(new BomberModel());
    unitModels.add(new BCopterModel());
    unitModels.add(new TCopterModel());

    unitModels.add(new BattleshipModel());
    unitModels.add(new CruiserModel());
    unitModels.add(new LanderModel());
    unitModels.add(new SubModel());

    weaponModels = new ArrayList<WeaponModel>();
    for( UnitModel unit : unitModels )
    {
      if( null != unit.weaponModels )
      {
        for( WeaponModel weapon : unit.weaponModels )
        {
          weaponModels.add(weapon);
        }
      }
    }

  }

  @Override
  public boolean runTest()
  {
    setupTest();

    boolean testPassed = true;
    testPassed &= validate(true, "  Everything you know is wrong.");
//    System.out.println(getDeltaString(new StandardDamage()));
    return testPassed;
  }

  public int[][] getDeltas(DamageStrategy other)
  {
    int[][] deltas = new int[numWeaponModels][numUnitModels];
    for( int i = 0; i < numWeaponModels; i++ )
    {
      WeaponModel weapon = weaponModels.get(i);
      for( int j = 0; j < numUnitModels; j++ )
      {
        UnitModel target = unitModels.get(j);
        deltas[i][j] = (int) (other.getDamage(weapon, target) - standardDamage.getDamage(weapon, target));
      }
    }
    return deltas;
  }

  public String getDeltaString(DamageStrategy other)
  {
    String out = "Defenders: ";
    int[][] deltas = getDeltas(other);
    for( UnitEnum type : UnitModel.UnitEnum.values() )
    {
      out += type.toString() + ", ";
    }
    out += "\n\n";
    for( int i = 0; i < numWeaponModels; i++ )
    {
      out += WeaponModel.WeaponType.values()[i].toString() + ": ";
      for( int j = 0; j < numUnitModels; j++ )
      {
        out += deltas[i][j] + ", ";
      }
      out += "\n";
    }
    return out;
  }

}
