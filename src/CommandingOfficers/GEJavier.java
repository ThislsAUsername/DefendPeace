package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;

public class GEJavier extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Javier 1T", new instantiator());
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new GEJavier();
    }
  }
  
  public int indirectDef = 20;

  public GEJavier()
  {
    super(coInfo);

    new CODefenseModifier(10).apply(this);

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
    GEJavier COcast;

    TowerShield(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (GEJavier) commander;
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
    private static final int VALUE = 40;
    GEJavier COcast;

    TowerOfPower(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (GEJavier) commander;
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
