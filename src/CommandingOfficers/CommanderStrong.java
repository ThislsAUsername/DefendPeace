package CommandingOfficers;

import Units.UnitModel;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;

public class CommanderStrong extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Strong", CommanderLibrary.CommanderEnum.STRONG);

  public CommanderStrong()
  {
    super(coInfo);

    // Set Cmdr Strong up with a base damage buff and long-range APCs. These COModifiers are
    // not added to the modifers collection so they will not be reverted.
    COModifier strongMod = new CODamageModifier(this, 20); // Give us a nice base power boost.
    strongMod.apply();

    COMovementModifier moveMod = new COMovementModifier(this);
    moveMod.addApplicableUnitType(UnitModel.UnitEnum.APC);
    moveMod.apply();
    // both modifiers are permanent and don't require CombatEngine interaction, so we can just drop them
  }

  public void doAbilityMinor()
  {
    COModifier strongerMod = new CODamageModifier(this, 20); // Another 20% with the minor ability active.
    strongerMod.apply();
    modifiers.add(strongerMod); // Add to the list
  }

  public void doAbilityMajor()
  {
    COModifier strongestMod = new CODamageModifier(this, 40); // An extra 40% with the major ability active.
    strongestMod.apply();
    modifiers.add(strongestMod); // Add to the list
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
