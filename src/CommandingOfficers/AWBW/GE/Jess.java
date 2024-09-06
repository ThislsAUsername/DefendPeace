package CommandingOfficers.AWBW.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
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

public class Jess extends AWBWCommander
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
      super("Jess", UIUtils.SourceGames.AWBW, UIUtils.GE);
      infoPages.add(new InfoPage(
          "Jess (AWBW)\n"
        + "(Land) Vehicles gain +10% attack, but all other units (including footsoldiers) lose -10% attack.\n"));
      infoPages.add(new InfoPage(TurboCharge(null, null),
          "(Land) Vehicles gain +1 movement and their attack is increased to +20%. All units resupply fuel and ammo.\n"
        + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(Overdrive(null, null),
          "(Land) Vehicles gain +2 movement and their attack is increased to +40%. All units resupply fuel and ammo.\n"
        + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jess(rules);
    }
  }
  private static CommanderAbility TurboCharge(Commander commander, CostBasis cb)
  {
    return new JessAbility(commander, cb, "Turbo Charge", 3, 1, 10);
  }
  private static CommanderAbility Overdrive(Commander commander, CostBasis cb)
  {
    return new JessAbility(commander, cb, "Overdrive", 6, 2, 30);
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


  private static class JessAbility extends AWBWAbility
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
    protected void perform(MapMaster map)
    {
      super.perform(map);
      for( Unit u : myCommander.army.getUnits() )
        u.resupply();
    }
  }
}
