package CommandingOfficers.Assorted;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.Weapon;

public class Greyfield extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Greyfield");
      infoPages.add(new InfoPage(
          "Stealths are replaced with Seaplanes, which are Stealths with +1 move that cost 15k, but can't hide." +
          "Can build Seaplanes from Carriers" +
          "Naval units, copters, and Seaplanes gain +10% firepower and +30% defense.\n" +
          "xxxXX\n" +
          "Supply Chain (3): Restore ammo to units\n" +
          "High Command (5): Resupply all units"));
    }
    @Override
    public Commander create()
    {
      return new Greyfield();
    }
  }

  public Greyfield()
  {
    super(coInfo);

    for( UnitModel um : unitModels.values() )
    {
      // TODO: stealths
      if( um.chassis == ChassisEnum.SUBMERGED || um.chassis == ChassisEnum.SHIP ||
          um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(10);
        um.modifyDefenseRatio(30);
      }
    }

    addCommanderAbility(new Resupply(this, "Supply Chain", 3, true, false));
    addCommanderAbility(new Resupply(this, "High Command", 5, true, true));
  }
  
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Resupply extends CommanderAbility
  {
    private boolean ammo, fuel;

    Resupply(Commander commander, String name, int cost, boolean ammo, boolean fuel)
    {
      super(commander, name, cost);
      this.ammo = ammo;
      this.fuel = fuel;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for (Unit unit : myCommander.units)
      {
        if( fuel )
          unit.fuel = unit.model.maxFuel;
        if( ammo )
          for( Weapon w : unit.weapons )
            w.reload();
      }
    }
  }
}

