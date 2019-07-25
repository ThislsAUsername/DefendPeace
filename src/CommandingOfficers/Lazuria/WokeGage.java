package CommandingOfficers.Lazuria;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class WokeGage extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Woke Gage");
      infoPages.add(new InfoPage(
          "--WOKE GAGE--\r\n" + 
          "Naval units and indirects gain +20% firepower and +10% defense.\r\n" + 
          "xxxxxXXXX\r\n" + 
          "LONG SHOT: All indirects gain +2 range.\r\n" + 
          "LONG BARREL: All indirects gain +4 range."));
    }
    @Override
    public Commander create()
    {
      return new WokeGage();
    }
  }

  public WokeGage()
  {
    super(coInfo);

    for( UnitModel um : unitModels.values() )
    {
      boolean buff = false;
      if( um.chassis == ChassisEnum.SHIP || um.chassis == ChassisEnum.SUBMERGED )
        buff = true;
      if( !buff && um.weaponModels != null )
      {
        for( WeaponModel pewpew : um.weaponModels )
        {
          if( !pewpew.canFireAfterMoving )
          {
            buff = true;
          }
        }
      }
      if( buff )
      {
        um.modifyDamageRatio(20);
        um.modifyDefenseRatio(10);
      }
    }

    addCommanderAbility(new RangeBonus(this, "Long Shot", 5, 2));
    addCommanderAbility(new RangeBonus(this, "Long Barrel", 9, 4));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class RangeBonus extends CommanderAbility
  {
    private int power = 1;

    RangeBonus(Commander commander, String name, int cost, int buff)
    {
      super(commander, name, cost);
      power = buff;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new IndirectRangeBoostModifier(power));
    }
  }
}

