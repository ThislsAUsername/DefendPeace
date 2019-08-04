package CommandingOfficers.Assorted;

import Engine.GameScenario;
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

public class Sneakfield extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Sneakfield");
      infoPages.add(new InfoPage(
          "Naval units, copters, and Stealths gain +10% firepower and +30% defense.\n" +
          "xXX\n" +
          "High Command (1): No effect.\n" +
          "Supply Chain (3): Resupply all units"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sneakfield(rules);
    }
  }

  public Sneakfield(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

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

    addCommanderAbility(new Resupply(this, "High Command", 1, false, false));
    addCommanderAbility(new Resupply(this, "Supply Chain", 3, true, true));
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

