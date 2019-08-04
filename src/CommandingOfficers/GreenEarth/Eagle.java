package CommandingOfficers.GreenEarth;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class Eagle extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Eagle");
      infoPages.add(new InfoPage(
          "Eagle\r\n" +
          "  Air units gain +15% attack, +10% defense and consume -2 fuel per day. Naval units lose -30% attack\r\n" +
          "Lightning Drive -- Boosts Air unit attack by 5% and defense by 10%\r\n" +
          "Lightning Strike -- Boosts Air unit attack by 5% and defense by 10%. All non-footsoldier units may move and fire again even if built this turn (use this power after moving!)"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Eagle(rules);
    }
  }

  public Eagle(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels.values() )
    {
      if( um.chassis == ChassisEnum.AIR_HIGH || um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(15);
        um.modifyDefenseRatio(10);
        um.idleFuelBurn -= 2;
      }
      if( um.chassis == ChassisEnum.SHIP || um.chassis == ChassisEnum.SUBMERGED )
      {
        um.modifyDamageRatio(-30);
      }
    }

    addCommanderAbility(new LightningDrive(this));
    addCommanderAbility(new LightningStrike(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class LightningDrive extends CommanderAbility
  {
    private static final String NAME = "Lightning Drive";
    private static final int COST = 3;

    LightningDrive(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      CODamageModifier airPowerMod = new CODamageModifier(5);
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.B_COPTER));
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.T_COPTER));
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.FIGHTER));
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.BOMBER));
      myCommander.addCOModifier(airPowerMod);
      CODefenseModifier airDefMod = new CODefenseModifier(10);
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.B_COPTER));
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.T_COPTER));
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.FIGHTER));
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.BOMBER));
      myCommander.addCOModifier(airDefMod);
    }
  }

  private static class LightningStrike extends CommanderAbility
  {
    private static final String NAME = "Lightning Strike";
    private static final int COST = 9;

    LightningStrike(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        if( unit.model.chassis != ChassisEnum.TROOP ) 
        {
          unit.isTurnOver = false;
        }
      }
      CODamageModifier airPowerMod = new CODamageModifier(5);
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.B_COPTER));
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.T_COPTER));
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.FIGHTER));
      airPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.BOMBER));
      myCommander.addCOModifier(airPowerMod);
      CODefenseModifier airDefMod = new CODefenseModifier(10);
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.B_COPTER));
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.T_COPTER));
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.FIGHTER));
      airDefMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.BOMBER));
      myCommander.addCOModifier(airDefMod);
    }
  }
}

