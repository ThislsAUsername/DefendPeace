package CommandingOfficers.Assorted;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.Unit;

public class HealyBoi extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("HealyBoi");
      infoPages.add(new InfoPage(
          "Credit BlueLink for concept, and junkmail/Jay for baiting me into adding it\n\n" +
          "D2D: Units heal +"+ D2DREPAIRS +" HP every turn (Not Liable for costs)\n" +
          "xxxxXXX\n" +
          "COP: +1 HP Mass Heal, +20% Defense, +1 Range\n" +
          "SCOP: +2 HP Mass Heal, +1 Range, +1 Movement\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new HealyBoi(rules);
    }
  }

  public static final int D2DREPAIRS = 1;

  public HealyBoi(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new COP(this));
    addCommanderAbility(new SCOP(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    modifyAbilityPower(42);
    GameEventQueue ret = super.initTurn(map);

    for( Unit unit : units )
    {
      int costPerHP = unit.model.getCost() / unit.model.maxHP;

      int affordableHP = this.money / costPerHP;
      int actualRepair = Math.min(D2DREPAIRS, affordableHP);

      int deltaHP = unit.alterHP(actualRepair);
//      this.money -= deltaHP * costPerHP;
    }
    
    return ret;
  }

  private static class COP extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "COP";
    private static final int COST = 4;
    private static final int VALUE = 1;

    COP(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        unit.alterHP(VALUE);
      }
      myCommander.addCOModifier(new CODefenseModifier(20));
      myCommander.addCOModifier(new IndirectRangeBoostModifier(1));
    }
  }

  private static class SCOP extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "SCOP";
    private static final int COST = 7;
    private static final int VALUE = 2;

    SCOP(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        unit.alterHP(VALUE);
      }
      myCommander.addCOModifier(new IndirectRangeBoostModifier(1));
      myCommander.addCOModifier(new COMovementModifier(1));
    }
  }
}

