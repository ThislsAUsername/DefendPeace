package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;

public class CommanderPatch extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Patch", CommanderLibrary.CommanderEnum.PATCH);

  public CommanderPatch()
  {
    super(coInfo);

    // TODO: Patch has a capture bonus of 1 day's income when he first takes a property.
    // TODO: Patch has a special unit - the Pirate ship.
  }

  public void doAbilityMinor()
  {
    // TODO: Patch gets money based on the damage inflicted (25% of the value of what is destroyed?).
  }

  public void doAbilityMajor()
  {
    // TODO: Patch gets a damage boost, and cash based on damage inflicted (40-50%?).
    COModifier dmgMod = new CODamageModifier(25);
    dmgMod.apply(this);
    modifiers.add(dmgMod); // Add to the list so the modifier can be reverted next turn.
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
