package CommandingOfficers.AW1.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.IndirectDamageModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Grit extends AW1Commander
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
      super("Grit_1", UIUtils.SourceGames.AW1, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Grit (AW1)\n"
          + "Laid-back style masks dependability. A peerless marksman.\n"
          + "Distance weapons have an extended attack range. However, direct combat units are weak.\n"
          + "(+1 indirect range, -20/0 in direct combat.)"));
      infoPages.add(new InfoPage(new SnipeAttack(null),
            "Increases the range and firepower of distance weapons.\n"
          + "(+2 indirect range, +50/0 (165/110) in indirect combat.)\n"
          + "1.1x/0.9x damage dealt/taken.\n"));
      infoPages.add(new InfoPage(
            "Hit: Cats\n"
          + "Miss: Rats"));
      infoPages.add(AW1_MECHANICS_BLURB);
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

    addCommanderAbility(new SnipeAttack(this));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange < 2 )
      params.attackPower -= 20;
  }
  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( uc.weapon != null && uc.weapon.rangeMax > 1 )
      uc.rangeMax += 1;
  }

  private static class SnipeAttack extends AW1BasicAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Snipe Attack";
    private static final int COST = 6;
    UnitModifier rangeMod;
    UnitModifier atkMod;

    SnipeAttack(Grit commander)
    {
      super(commander, NAME, COST);
      rangeMod = new UnitIndirectRangeModifier(2);
      atkMod   = new IndirectDamageModifier(50);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(rangeMod);
      modList.add(atkMod);
    }
  }

}
