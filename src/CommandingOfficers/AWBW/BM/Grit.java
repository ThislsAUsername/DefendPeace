package CommandingOfficers.AWBW.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.IndirectDamageModifier;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;
import Units.UnitModel;

public class Grit extends AWBWCommander
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
      super("Grit", UIUtils.SourceGames.AWBW, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Grit (AWBW)\n"
          + "ndirect units have +1 range and gain +20% attack. Direct units (except footsoldiers) lose 20% attack.\n"));
      infoPages.add(new InfoPage(new SnipeAttack(null, null),
            "Indirect units gain +1 range and their attack is increased to +40%. (150 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new SuperSnipe(null, null),
            "Indirect units gain +2 range and their attack is increased to +40%. (+20, 150 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Grit(rules);
    }
  }

  public Grit(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new SnipeAttack(this, cb));
    addCommanderAbility(new SuperSnipe(this, cb));
  }

  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( uc.weapon != null && uc.weapon.rangeMax() > 1 )
      uc.rangeMax += 1;
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange > 1 )
      params.attackPower += 20;
    if( params.attacker.model.isAny(UnitModel.TROOP) )
      return;
    if( params.battleRange < 2 )
      params.attackPower -= 20;
  }

  private static class SnipeAttack extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Snipe Attack";
    private static final int COST = 3;
    private static final int BUFF = 20;
    UnitModifier statMod, rangeMod;

    SnipeAttack(Grit commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod  = new IndirectDamageModifier(BUFF);
      rangeMod = new UnitIndirectRangeModifier(1);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(rangeMod);
    }
  }

  private static class SuperSnipe extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Super Snipe";
    private static final int COST = 6;
    private static final int BUFF = 20;
    UnitModifier statMod, rangeMod;

    SuperSnipe(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod  = new IndirectDamageModifier(BUFF);
      rangeMod = new UnitIndirectRangeModifier(2);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(rangeMod);
    }
  }

}
