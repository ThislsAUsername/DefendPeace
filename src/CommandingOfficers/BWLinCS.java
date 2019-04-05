package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COVisionModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class BWLinCS extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Lin", new instantiator());
  private static class instantiator extends COMaker
  {
    public instantiator()
    {
      infoPages.add(new InfoPage(
          "--LIN--\r\n" + 
          "Ground units gain +10% firepower.\r\n" + 
          "xxXXX\r\n" + 
          "SCOUT: All ground units get 120/120 stats, +1 vision, and can see into hiding places.\r\n" + 
          "NIGHT VISION: All ground units get 130/130 stats, +2 vision, and can see into hiding places."));
    }
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
      
      myCommander.addCOModifier(new CODefenseModifier(10));
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
      
      myCommander.addCOModifier(new CODamageModifier(10));
      myCommander.addCOModifier(new CODefenseModifier(20));
    }
  }
}
