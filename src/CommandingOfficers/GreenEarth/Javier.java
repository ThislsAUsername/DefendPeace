package CommandingOfficers.GreenEarth;

import Engine.GameScenario;
import Engine.Combat.StrikeParams.BattleParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;

public class Javier extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Javier");
      infoPages.add(new InfoPage(
          "Javier\n" +
          "  Units gain +20% defense against indirect units.\n" +
          "  Units gain defense equal to all offense gained via comm tower\n" +
          "Tower Shield -- Double comm tower effects. Extra defense against indirects (+20%)\n" +
          "Tower of Power -- Triple comm tower effects. Even more defense against indirects (+60%)"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Javier(rules);
    }
  }
  
  public int indirectDef = 20;
  public int commPowerMult = 1;

  public Javier(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new TowerShield(this));
    addCommanderAbility(new TowerOfPower(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    this.indirectDef = 20;
    this.commPowerMult = 1;
    return super.initTurn(map);
  }

  public int getTowerBoost() {
    return super.getTowerBoost() * commPowerMult;
  }
  public int getTowerDefBoost() {
    return getTowerBoost();
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if ( params.battleRange > 1 )
    {
      params.defensePower += indirectDef;
    }
  }

  private static class TowerShield extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
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
      COcast.commPowerMult = 2;
    }
  }

  private static class TowerOfPower extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
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
      COcast.commPowerMult = 3;
    }
  }
}

