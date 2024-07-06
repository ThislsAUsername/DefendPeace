package CommandingOfficers.AWBW.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.IndirectDefenseModifier;
import Engine.UnitMods.TowerCountMultiplier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Javier extends AWBWCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Javier", UIUtils.SourceGames.AWBW, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Javier (AWBW)\n"
          + "Units gain +20% defense against indirect units. Comtowers grant all units additional +10% defense.\n"
          + "(His bonus per tower is +10/10 regardless of tower type, but the defense is DoR-flavored if it's a DoR tower.)"));
      infoPages.add(new InfoPage(TowerShield(null, new CommanderAbility.CostBasis(CHARGERATIO_FUNDS)),
            "Indirect defense is increased to +40%. Comtower bonuses are doubled.\n"
          + "+10 attack and defense.\n"));
    infoPages.add(new InfoPage(TowerOfPower(null, new CommanderAbility.CostBasis(CHARGERATIO_FUNDS)),
            "Indirect defense is increased to +80%. Comtower bonuses are tripled.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Javier(rules);
    }
  }
  private static CommanderAbility TowerShield(Commander commander, CostBasis cb)
  {
    return new JavierAbility(commander, cb, "Tower Shield", 3, 2, 20);
  }
  private static CommanderAbility TowerOfPower(Commander commander, CostBasis cb)
  {
    return new JavierAbility(commander, cb, "Tower of Power", 6, 3, 60);
  }

  public Javier(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(TowerShield(this, cb));
    addCommanderAbility(TowerOfPower(this, cb));
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.battleRange < 2 )
      params.defenseSubtraction += 20;
  }
  @Override
  public void applyTowerAttack(StrikeParams params)
  {
    UnitContext minion = params.attacker;
    params.attackPower += minion.towerCountDS  * 10;
    params.attackPower += minion.towerCountDoR * 10;
  }
  @Override
  public void applyTowerDefense(BattleParams params)
  {
    UnitContext minion = params.defender;
    params.defenseSubtraction += minion.towerCountDS  * 10;
    params.defenseDivision    += minion.towerCountDoR * 10;
  }


  private static class JavierAbility extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private final int boostDef;
    private final int boostTower;

    JavierAbility(Commander commander, CostBasis cb, String name, int cost, int towerMult, int defense)
    {
      super(commander, name, cost, cb);
      AIFlags = PHASE_TURN_START;
      boostDef = defense;
      boostTower = towerMult;
    }

    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      // Generate mods at activation time since Javier doesn't have an army at construction time.
      modList.add(new IndirectDefenseModifier(boostDef));
      modList.add(new TowerCountMultiplier(boostTower));
    }
  }
}
