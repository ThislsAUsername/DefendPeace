package CommandingOfficers;

import CommandingOfficers.Modifiers.COVisionModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class BWLinCS extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Lin\nCS", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BWLinCS();
    }
  }

  public BWLinCS()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP)
      {
        um.modifyDamageRatio(10);
      }
    }

    addCommanderAbility(new Scout(this));
    addCommanderAbility(new NightVision(this));
    modifyAbilityPower(40);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Scout extends CommanderAbility
  {
    private static final String NAME = "Scout";
    private static final int COST = 2;

    Scout(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // add vision +1 and piercing vision to land units
      COVisionModifier sightMod = new COVisionModifier(1);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP )
          sightMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }

  private static class NightVision extends CommanderAbility
  {
    private static final String NAME = "Night Vision";
    private static final int COST = 5;

    NightVision(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // add vision +2 and piercing vision to land units
      COVisionModifier sightMod = new COVisionModifier(2);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP )
          sightMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }
}
