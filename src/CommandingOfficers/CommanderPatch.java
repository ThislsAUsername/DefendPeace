package CommandingOfficers;

import Terrain.GameMap;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.Combat.CombatDamageModifier;
import Engine.Combat.CombatEngine;
import Engine.Combat.StrongCombatModifier;

public class CommanderPatch extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Patch", CommanderLibrary.CommanderEnum.PATCH);
  private double lootingYield = 0;

  public CommanderPatch()
  {
    super(coInfo);

    // TODO: Super alpha values. Need balance.
    starsMinor = 2;
    starsMax = 5;
    starsCurrent = 5;
    
    // TODO: Patch has a capture bonus of 1 day's income when he first takes a property.
    // TODO: Patch has a special unit - the Pirate ship.
  }

  public void doAbilityMinor()
  {
    super.doAbilityMinor();
    // TODO: Patch gets money based on the damage inflicted (25% of the value of what is destroyed?).
    lootingYield = 0.25;
  }

  public void doAbilityMajor()
  {
    super.doAbilityMajor();
    // TODO: Patch gets a damage boost, and cash based on damage inflicted (40-50%?).
    lootingYield = 0.42;
    CombatEngine.modifiers.add(new CombatDamageModifier(this,25));
    //COModifier dmgMod = new CODamageModifier(25);
    //dmgMod.apply(this);
    //modifiers.add(dmgMod); // Add to the list so the modifier can be reverted next turn.
  }

  public void addStars(double change)
  {
    super.addStars(change);
    money += change*10000*lootingYield;
  }

  public void initTurn(GameMap map)
  {
    super.initTurn(map);
    lootingYield = 0;
  }
  
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
