package CommandingOfficers.Assorted;

import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.UnitActionLifecycles.UnitProduceLifecycle;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

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

  private static final long SEAPLANE_ROLE =
        UnitModel.AIR_TO_SURFACE
      | UnitModel.AIR_TO_AIR
      | UnitModel.ASSAULT
      | UnitModel.JET
      | UnitModel.AIR_HIGH;

  public Fastfield(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if ( um.name.equalsIgnoreCase("stealth") )
      {
        um.possibleActions.clear();
        for( UnitActionFactory action : UnitActionFactory.COMBAT_VEHICLE_ACTIONS )
        {
          um.possibleActions.add(action);
        }
        um.name = "Seaplane";
        um.hidden = false;
        um.moneyCostAdjustment = -9000;
        um.movePower += 1;
      }

      boolean canBuild = false;
      for( UnitActionFactory action : um.possibleActions )
        if( action.name().contains("BUILD") )
          canBuild = true;
      if( (um.carryableMask & UnitModel.AIR_HIGH) > 0
          && !canBuild )
        um.possibleActions.add(0, new UnitProduceLifecycle.UnitProduceFactory(getUnitModel(SEAPLANE_ROLE)));

      if ( um.isAll(SEAPLANE_ROLE) )
      {
        um.modifyDamageRatio(10);
        um.modifyDefenseRatio(30);
      }
      if ( um.isSeaUnit()
           || um.isAll(UnitModel.AIR_LOW) )
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
        unit.materials = unit.model.maxMaterials;
        if( fuel )
          unit.fuel = unit.model.maxFuel;
        if( ammo )
          unit.ammo = unit.model.maxAmmo;
      }
    }
  }
}

