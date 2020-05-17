package CommandingOfficers.YellowComet;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COVisionModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.Combat.BattleInstance.CombatContext;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.WeaponModel;

public class Sonja extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Sonja");
      infoPages.add(new InfoPage(
          "--Sonja--\r\n" +
          "+1 vision in Fog of War, and counterattacks do 1.5x damage. All units have bad luck (up to -10% bad luck).\r\n" +
          "xxxXX\r\n" +
          "Enhanced Vision: +1 vision; can see into hiding places.\r\n" +
          "Counter Break: +2 vision; can see into hiding places; a unit being attacked attacks first even if it would be destroyed."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sonja(rules);
    }
  }

  public boolean counterFirst = false;

  public Sonja(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    
    for( UnitModel um : unitModels )
    {
      um.visionRange += 1;
    }

    addCommanderAbility(new EnhancedVision(this));
    addCommanderAbility(new CounterBreak(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    counterFirst = false;
    return super.initTurn(map);
  }

  @Override
  public void changeCombatContext(CombatContext instance)
  {
    // If we're swapping, and we can counter, and we're on the defensive, do the swap.
    if (counterFirst && instance.canCounter && this == instance.defender.CO )
    {
      // Store our unit. Since defenders don't move, we have defenderX/Y already.
      Unit minion = instance.defender;
      WeaponModel myWeapon = instance.defenderWeapon;

      instance.defender = instance.attacker;
      instance.defenderWeapon = instance.attackerWeapon;
      instance.defenderX = instance.attackerX;
      instance.defenderY = instance.attackerY;

      instance.attacker = minion;
      instance.attackerWeapon = myWeapon;
      instance.attackerX = minion.x;
      instance.attackerY = minion.y;
    }
  }

  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    if( amITheAttacker )
    {
      params.dispersion = 10;
      if( params.isCounter )
        // it's a multiplier according to the damage calc
        params.attackFactor *= 50;
    }
  }

  private static class EnhancedVision extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Enhanced Vision";
    private static final int COST = 3;

    EnhancedVision(Sonja commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COVisionModifier sightMod = new COVisionModifier(1);
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }

  private static class CounterBreak extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Counter Break";
    private static final int COST = 5;
    Sonja COcast;

    CounterBreak(Sonja commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
      AIFlags |= PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.counterFirst = true;
      COVisionModifier sightMod = new COVisionModifier(2);
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }
  
}

