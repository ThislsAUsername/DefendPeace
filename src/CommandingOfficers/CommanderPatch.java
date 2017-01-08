package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameInstance;

public class CommanderPatch extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Patch", CommanderLibrary.CommanderEnum.PATCH);

  private static final String PLUNDER_NAME = "Plunder";
  private static final String PILLAGE_NAME = "Pillage";
  private static final int PILLAGE_COST = 0;

  public CommanderPatch()
  {
    super(coInfo);

    // Passive - Loot
    // TODO: Patch has a capture bonus of 1 day's income when he first takes a property.
    // TODO: Patch has a special unit - the Pirate ship.
  }

  @Override
  public void doAbility( String abilityName, GameInstance game )
  {
    if( abilityName == PLUNDER_NAME )
    {
      // TODO: Patch gets money based on the damage inflicted (25% of the value of what is destroyed?).
    }
    else if( abilityName == PILLAGE_NAME )
    {
      // TODO: Patch gets a damage boost, and cash based on damage inflicted (40-50%?).
      COModifier dmgMod = new CODamageModifier(25);
      dmgMod.apply(this);
      modifiers.add(dmgMod); // Add to the list so the modifier can be reverted next turn.
    }
  }

  @Override
  public ArrayList<String> getReadyAbilities()
  {
    ArrayList<String> abs = new ArrayList<String>();
    abs.add(PILLAGE_NAME);
    return abs;
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
