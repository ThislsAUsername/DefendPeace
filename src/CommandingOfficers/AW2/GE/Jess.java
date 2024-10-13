package CommandingOfficers.AW2.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW2.AW2Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class Jess extends AW2Commander
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
      super("Jess", UIUtils.SourceGames.AW2, UIUtils.GE);
      infoPages.add(new InfoPage(
          "Jess (AW2)\n"
        + "A gallant tank driving CO who fought her way to the top. Often argues with Eagle.\n"
        + "Vehicular (land) units have superior attack power (+10). Infantry, air and naval units are comparatively weak (-10)."));
      infoPages.add(new InfoPage(TurboCharge(null, null),
          "Movement range of (land) vehicles increases by 1 space. Firepower increases (+20), and fuel and ammo supplies are also replenished.\n"
        + "+10 defense. Also restores materials.\n"));
      infoPages.add(new InfoPage(Overdrive(null, null),
          "Increase in the attack strength of vehicular (land) units (+40) and 2-space increase in movement range. Also restores fuel and ammo supplies.\n"
        + "+10 defense. Also restores materials.\n"));
      infoPages.add(new InfoPage(
            "Hit: Dandelions\n"
          + "Miss: Unfit COs"));
      infoPages.add(AW2_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jess(rules);
    }
  }
  private static CommanderAbility TurboCharge(Commander commander, CostBasis cb)
  {
    return new JessAbility(commander, cb, "Turbo Charge", 3, 1, 20);
  }
  private static CommanderAbility Overdrive(Commander commander, CostBasis cb)
  {
    return new JessAbility(commander, cb, "Overdrive", 6, 2, 40);
  }

  public Jess(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(TurboCharge(this, cb));
    addCommanderAbility(Overdrive(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(UnitModel.TANK) )
      params.attackPower += 10;
    else
      params.attackPower -= 10;
  }


  private static class JessAbility extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter moveMod, attMod;

    JessAbility(Commander commander, CostBasis cb, String name, int cost, int tankMove, int tankAttack)
    {
      super(commander, name, cost, cb);
      AIFlags = PHASE_TURN_START;
      moveMod = new UnitTypeFilter(new UnitMovementModifier(tankMove));
      moveMod.allOf = UnitModel.TANK;
      attMod  = new UnitTypeFilter(new UnitDamageModifier(tankAttack));
      attMod.allOf = UnitModel.TANK;
    }

    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(attMod);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      for( Unit unit : myCommander.army.getUnits() )
      {
        unit.resupply();
        unit.materials = unit.model.maxMaterials;
      }
    }
  }
}
