package CommandingOfficers.Assorted;

import Engine.GameScenario;
import Engine.UnitActionType;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Assorted.Greyfield.BuildSeaplane;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;

public class Fastfield extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Fastfield");
      infoPages.add(new InfoPage(
          "Stealths are replaced with Seaplanes, which are Stealths with +1 move that cost 15k, but can't hide." +
          "Can build Seaplanes from Carriers" +
          "Naval units, copters, and Seaplanes gain +10% firepower and +30% defense.\n" +
          "xXX\n" +
          "High Command (1): No effect.\n" +
          "Supply Chain (3): Resupply all units"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Fastfield(rules);
    }
  }

  public Fastfield(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels.values() )
    {
      if ( um.type == UnitEnum.STEALTH || um.type == UnitEnum.STEALTH_HIDE)
      {
        um.possibleActions.clear();
        for( UnitActionType action : UnitActionType.COMBAT_VEHICLE_ACTIONS )
        {
          um.possibleActions.add(action);
        }
        um.name = "Seaplane";
        um.hidden = false;
        um.moneyCostAdjustment = -9000;
        um.movePower += 1;
        um.modifyDamageRatio(10);
        um.modifyDefenseRatio(30);
      }
      if ( um.type == UnitEnum.CARRIER)
        um.possibleActions.add(new BuildSeaplane(this));
      
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
    private static final long serialVersionUID = 1L;
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

