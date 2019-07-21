package CommandingOfficers;

import CommandingOfficers.Modifiers.COVisionModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.Combat.BattleInstance.CombatContext;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.Weapons.Weapon;

public class IDSCyrus extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
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
    public Commander create()
    {
      return new IDSCyrus();
    }
  }

  public int terrainDrain = 1;
  public boolean counterFirst = false;

  public IDSCyrus()
  {
    super(coInfo);
    
    for( UnitModel um : unitModels.values() )
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
  public GameEventQueue initTurn(GameMap map)
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
      Weapon myWeapon = instance.defenderWeapon;
      
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
    private static final String NAME = "Courageous";
    private static final int COST = 3;
    IDSCyrus COcast;

    Courageous(IDSCyrus commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.terrainDrain = 2;
      COVisionModifier sightMod = new COVisionModifier(1);
      for(UnitModel um : myCommander.unitModels.values())
      {
        sightMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }

  private static class Retribution extends CommanderAbility
  {
    private static final String NAME = "Fearless";
    private static final int COST = 6;
    IDSCyrus COcast;

    Retribution(IDSCyrus commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.counterFirst = true;
      COcast.terrainDrain = 3;
      COVisionModifier sightMod = new COVisionModifier(2);
      for(UnitModel um : myCommander.unitModels.values())
      {
        sightMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }
  
}

