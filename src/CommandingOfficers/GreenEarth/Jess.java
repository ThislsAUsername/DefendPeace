package CommandingOfficers.GreenEarth;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class Jess extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Davis");
      infoPages.add(new InfoPage(
          "Davis (AW2 Jess)\r\n" + 
          "  Ground vehicles gain +10% attack, but all other units (including footsoldiers) lose -10% attack\r\n" +
          "Turbo Charge -- All units resupply; vehicles gain +10% attack and +1 Movement\r\n" +
          "Overdrive -- All units resupply; vehicles gain +30% attack +2 Movement"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jess(rules);
    }
  }

  public Jess(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isAny(UnitModel.TANK) )
      {
        um.modifyDamageRatio(10);
      }
      else
      {
        um.modifyDamageRatio(-10);
      }
    }

    addCommanderAbility(new TurboCharge(this));
    addCommanderAbility(new Overdrive(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class TurboCharge extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Turbo Charge";
    private static final int COST = 3;

    TurboCharge(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for (Unit unit : myCommander.units)
      {
        unit.resupply();
      }
      CODamageModifier landPowerMod = new CODamageModifier(10);
      COMovementModifier landMoveMod = new COMovementModifier(1);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isAny(UnitModel.TANK) )
        {
          landPowerMod.addApplicableUnitModel(um);
          landMoveMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(landPowerMod);
      myCommander.addCOModifier(landMoveMod);
    }
  }

  private static class Overdrive extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Overdrive";
    private static final int COST = 6;

    Overdrive(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for (Unit unit : myCommander.units)
      {
        unit.resupply();
      }
      CODamageModifier landPowerMod = new CODamageModifier(30);
      COMovementModifier landMoveMod = new COMovementModifier(2);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isAny(UnitModel.TANK) )
        {
          landPowerMod.addApplicableUnitModel(um);
          landMoveMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(landPowerMod);
      myCommander.addCOModifier(landMoveMod);
    }
  }
}

