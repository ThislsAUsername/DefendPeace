package CommandingOfficers.GreenEarth;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;

public class Javier extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Javier 1T");
      infoPages.add(new InfoPage(
          "Javier\r\n" + 
          "  Units gain +20% defense against indirect units. +10% defense\r\n" + 
          "Tower Shield -- Extra defense against indirects (+20%); +10/10 stats\r\n" + 
          "Tower of Power -- Even more defense against indirects (+60%); +20/20 stats"));
    }
    @Override
    public Commander create()
    {
      return new Javier();
    }
  }
  
  public int indirectDef = 20;

  public Javier()
  {
    super(coInfo);

    new CODefenseModifier(10).applyChanges(this);

    addCommanderAbility(new TowerShield(this));
    addCommanderAbility(new TowerOfPower(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    this.indirectDef = 20;
    return super.initTurn(map);
  }

  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    if( params.defender.CO == this )
    {
      if ( params.combatRef.battleRange > 1 )
      {
        params.defenseFactor += indirectDef;
      }
    }
  }

  private static class TowerShield extends CommanderAbility
  {
    private static final String NAME = "Tower Shield";
    private static final int COST = 3;
    private static final int VALUE = 20;
    Javier COcast;

    TowerShield(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Javier) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.indirectDef += VALUE;
      myCommander.addCOModifier(new CODamageModifier(10));
      myCommander.addCOModifier(new CODefenseModifier(10));
    }
  }

  private static class TowerOfPower extends CommanderAbility
  {
    private static final String NAME = "Tower of Power";
    private static final int COST = 6;
    private static final int VALUE = 60;
    Javier COcast;

    TowerOfPower(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Javier) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.indirectDef += VALUE;
      myCommander.addCOModifier(new CODamageModifier(20));
      myCommander.addCOModifier(new CODefenseModifier(20));
    }
  }
}

