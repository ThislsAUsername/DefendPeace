package CommandingOfficers.OrangeStar;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.UnitModel;

public class Sami extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Sami");
      infoPages.add(new InfoPage(
          "Sami\r\n" + 
          "  Footsoldiers gain +30% attack and capture buildings 50% faster (rounded down), other direct units lose -10% attack. Transports gain +1 movement\r\n" + 
          "Double Time -- Footsoldiers gain +20% power and +1 Movement\r\n" + 
          "Victory March -- Footsoldiers gain +40% power, +2 Movement, and capture any building in one turn (even with 1 HP)"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sami(rules);
    }
  }

  public Sami(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    COMovementModifier moveMod = new COMovementModifier();

    for( UnitModel um : unitModels )
    {
      if( um.isAny(UnitModel.TROOP) )
      {
        um.modifyDamageRatio(30);
      }
      else // if you're not a footsoldier and you have direct attacks, get debuffed
      {
        if( um.hasDirectFireWeapon() )
        {
          um.modifyDamageRatio(-10);
        }
      }

      if (um.isAny(UnitModel.TRANSPORT))
        moveMod.addApplicableUnitModel(um);
    }

    moveMod.applyChanges(this);

    addCommanderAbility(new DoubleTime(this));
    addCommanderAbility(new VictoryMarch(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  public double captureMultiplierDefault = 1.5,
                captureMultiplier = captureMultiplierDefault;
  @Override
  public double getCaptureMult()
  {
    return captureMultiplier;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    captureMultiplier = captureMultiplierDefault;
    return super.initTurn(map);
  }

  private static class DoubleTime extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Double Time";
    private static final int COST = 3;
    private static final int POWER = 20;

    DoubleTime(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Grant foot-soldiers additional firepower.
      CODamageModifier infPowerMod = new CODamageModifier(POWER);
      COMovementModifier infmoveMod = new COMovementModifier(1);

      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isAny(UnitModel.TROOP) )
        {
          infPowerMod.addApplicableUnitModel(um);
          infmoveMod. addApplicableUnitModel(um);
        }
      }

      myCommander.addCOModifier(infPowerMod);
      myCommander.addCOModifier(infmoveMod);
    }
  }

  private static class VictoryMarch extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Victory March";
    private static final int COST = 8;
    private static final int POWER = 40;
    private Sami coCast;

    VictoryMarch(Sami commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
      coCast = commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      coCast.captureMultiplier = 99;
      // Grant foot-soldiers additional firepower.
      CODamageModifier infPowerMod = new CODamageModifier(POWER);
      COMovementModifier infmoveMod = new COMovementModifier(2);

      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isAny(UnitModel.TROOP) )
        {
          infPowerMod.addApplicableUnitModel(um);
          infmoveMod. addApplicableUnitModel(um);
        }
      }

      myCommander.addCOModifier(infPowerMod);
      myCommander.addCOModifier(infmoveMod);
    }
  }
}

