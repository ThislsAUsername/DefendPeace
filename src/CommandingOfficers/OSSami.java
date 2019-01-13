package CommandingOfficers;

import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.UnitTypeDamageModifier;
import Engine.GameEvents.GameEventListener;
import Terrain.MapMaster;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class OSSami extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Sami", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new OSSami();
    }
  }

  public OSSami()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.TROOP )
      {
        um.modifyDamageRatio(30);
      }
      else // if you're not a footsoldier and you have direct attacks, get debuffed
      {
        if( um.weaponModels != null )
        {
          boolean debuff = false;
          for( WeaponModel pewpew : um.weaponModels )
          {
            if( pewpew.maxRange == 1 )
            {
              debuff = true;
            }
          }
          if( debuff )
            um.modifyDamageRatio(-10);
        }
      }
    }

    COMovementModifier moveMod = new COMovementModifier();
    moveMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.APC));
    moveMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.T_COPTER));
    moveMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.LANDER));
    moveMod.apply(this);

    addCommanderAbility(new DoubleTime(this));
    addCommanderAbility(new VictoryMarch(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public void receiveCaptureEvent(Unit unit, Location location)
  {
    if( this == unit.CO && this != location.getOwner() )
    {
      double halfHP = unit.getPreciseHP() / 2;
      unit.damageHP(halfHP);
      unit.capture(location);
      unit.damageHP(-halfHP);
    }
  }

  private static class DoubleTime extends CommanderAbility
  {
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
      UnitTypeDamageModifier infPowerMod = new UnitTypeDamageModifier(POWER);
      infPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));
      infPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
      myCommander.addCOModifier(infPowerMod);
      COMovementModifier infmoveMod = new COMovementModifier(1);
      infmoveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));
      infmoveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
      myCommander.addCOModifier(infmoveMod);
    }
  }

  private static class VictoryMarch extends CommanderAbility implements COModifier
  {
    private static final String NAME = "Victory March";
    private static final int COST = 8;
    private static final int POWER = 40;
    private InstantCapListener listener;

    VictoryMarch(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
      listener = new InstantCapListener(commander);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(this);
      // Grant foot-soldiers additional firepower.
      UnitTypeDamageModifier infPowerMod = new UnitTypeDamageModifier(POWER);
      infPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));
      infPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
      myCommander.addCOModifier(infPowerMod);
      COMovementModifier infmoveMod = new COMovementModifier(2);
      infmoveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));
      infmoveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
      myCommander.addCOModifier(infmoveMod);
    }

    @Override // COModifier interface.
    public void apply(Commander commander)
    {
      GameEventListener.registerEventListener(listener);
    }

    @Override
    public void revert(Commander commander)
    {
      GameEventListener.unregisterEventListener(listener);
    }
  }

  private static class InstantCapListener extends GameEventListener
  {
    private Commander myCommander = null;

    public InstantCapListener(Commander myCo)
    {
      myCommander = myCo;
    }

    @Override
    public void receiveCaptureEvent(Unit unit, Location location)
    {
      if( myCommander == unit.CO && myCommander != location.getOwner() )
      {
        location.setOwner(myCommander);
        unit.stopCapturing();
      }
    }
  }
}
