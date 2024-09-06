package CommandingOfficers.AW3.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW3.AW3Commander;
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

public class Jess extends AW3Commander
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
      super("Jess", UIUtils.SourceGames.AW3, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Jess (AW3)\n"
          + "A gallant tank-driving CO who excels at analysing information. A hero of the last war.\n"
          + "Vehicular (land) units have superior firepower (+20). Air and naval units are comparatively weak (-10).\n"));
      infoPages.add(new InfoPage(TurboCharge(null, null),
            "Movement of vehicular (land) units goes up by one space. Attack increases (+20, 150 total), and fuel and ammo supplies are also replenished.\n"
          + "+10 attack and defense.\n"));
    infoPages.add(new InfoPage(Overdrive(null, null),
            "Movement of vehicular (land) units goes up by two spaces. Attack dramatically increases (+40, 170 total), and fuel and ammo supplies are also replenished.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Dandelions\n"
          + "Miss: Unfit COs"));
      infoPages.add(AW3_MECHANICS_BLURB);
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
    if( params.attacker.model.isAny(UnitModel.TROOP) )
      return;
    if( params.attacker.model.isAny(UnitModel.LAND) )
      params.attackPower += 20;
    if( params.attacker.model.isAny(UnitModel.AIR | UnitModel.SEA) )
      params.attackPower -= 10;
  }


  private static class JessAbility extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter moveMod, attMod;

    JessAbility(Commander commander, CostBasis cb, String name, int cost, int tankMove, int tankAttack)
    {
      super(commander, name, cost, cb);
      AIFlags = PHASE_TURN_START;
      moveMod = new UnitTypeFilter(new UnitMovementModifier(tankMove));
      moveMod.allOf = UnitModel.LAND;
      moveMod.noneOf = UnitModel.TROOP;
      attMod  = new UnitTypeFilter(new UnitDamageModifier(tankAttack));
      attMod.allOf = UnitModel.LAND;
      attMod.noneOf = UnitModel.TROOP;
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
