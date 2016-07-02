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
    super();

    // Set Cmdr Strong up with a base damage buff and long-range APCs. These COModifiers are
    // not added to the modifers collection so they will not be reverted.
    COModifier strongMod = new CODamageModifier(20); // Give us a nice base power boost.
    strongMod.apply(this);

    COMovementModifier moveMod = new COMovementModifier();
    moveMod.addApplicableUnitType(UnitModel.UnitEnum.APC);
    moveMod.apply(this);
  }

  public void doAbilityMinor()
  {
    COModifier strongerMod = new CODamageModifier(20); // Another 20% with the minor ability active.
    strongerMod.apply(this);
    modifiers.add(strongerMod); // Add to the list so the modifier can be reverted next turn.
  }

  public void doAbilityMajor()
  {
    COModifier strongestMod = new CODamageModifier(40); // An extra 40% with the major ability active.
    strongestMod.apply(this);
    modifiers.add(strongestMod); // Add to the list so the modifier can be reverted next turn.
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
