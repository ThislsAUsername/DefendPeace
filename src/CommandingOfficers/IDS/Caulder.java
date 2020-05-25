package CommandingOfficers.IDS;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.Unit;

public class Caulder extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Caulder");
      infoPages.add(new InfoPage(
          "--CAULDER--\r\n" +
          "All damaged units are repaired for +"+ D2DREPAIRS +" HP every turn (liable for costs).\r\n" +
          "XXXXX XXXXX\r\n" +
          "SUPREME BOOST: All units gain +"+MEGA_ATK+"/"+MEGA_DEF+" stats, and are repaired for +"+ SupremeBoost.REPAIRS +" HP (liable for costs -- watch your funds!)."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Caulder(rules);
    }
  }

  public static final int D2DREPAIRS = 2;
  public static final int MEGA_ATK = 50;
  public static final int MEGA_DEF = 35;

  public Caulder(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new SupremeBoost(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    GameEventQueue ret = super.initTurn(map);

    for( Unit unit : units )
    {
      int costPerHP = unit.model.getCost() / unit.model.maxHP;

      int affordableHP = this.money / costPerHP;
      int actualRepair = Math.min(D2DREPAIRS, affordableHP);

      int deltaHP = unit.alterHP(actualRepair);
      this.money -= deltaHP * costPerHP;
    }
    
    return ret;
  }

  private static class SupremeBoost extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Supreme Boost";
    private static final int COST = 10;
    private static final int REPAIRS = 3;

    SupremeBoost(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(MEGA_ATK));
      myCommander.addCOModifier(new CODefenseModifier(MEGA_DEF));
      for( Unit unit : myCommander.units )
      {
        int costPerHP = unit.model.getCost() / unit.model.maxHP;

        int affordableHP = myCommander.money / costPerHP;
        int actualRepair = Math.min(REPAIRS, affordableHP);

        int deltaHP = unit.alterHP(actualRepair);
        myCommander.money -= deltaHP * costPerHP;
      }
    }
  }
}

