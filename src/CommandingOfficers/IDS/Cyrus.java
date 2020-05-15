package CommandingOfficers.IDS;

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

public class Cyrus extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Cyrus");
      infoPages.add(new InfoPage(
          "--CYRUS--\r\n" +
          "+1 vision in Fog of War, and all enemy units lose one terrain star. All units have hidden HP, but can have bad luck (up to -5% bad luck).\r\n" +
          "xxxXX\r\n" +
          "COURAGEOUS: +1 vision; can see into hiding places; all enemy units lose one additional terrain star.\r\n" +
          "FEARLESS: +2 vision; can see into hiding places; all enemy units lose two additional terrain stars; a unit being attacked attacks first even if it would be destroyed."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Cyrus(rules);
    }
  }

  public int terrainDrain = 1;
  public boolean counterFirst = false;

  public Cyrus(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    
    for( UnitModel um : unitModels )
    {
      um.visionRange += 1;
    }

    addCommanderAbility(new Courageous(this));
    addCommanderAbility(new Retribution(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    terrainDrain = 1;
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
      params.terrainDefense = Math.max(0, params.terrainDefense-terrainDrain);
      params.dispersion = 5;
    }
  }

  private static class Courageous extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Courageous";
    private static final int COST = 3;
    Cyrus COcast;

    Courageous(Cyrus commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.terrainDrain = 2;
      COVisionModifier sightMod = new COVisionModifier(1);
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }

  private static class Retribution extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Fearless";
    private static final int COST = 6;
    Cyrus COcast;

    Retribution(Cyrus commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
      AIFlags |= PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.counterFirst = true;
      COcast.terrainDrain = 3;
      COVisionModifier sightMod = new COVisionModifier(2);
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }
  
}

