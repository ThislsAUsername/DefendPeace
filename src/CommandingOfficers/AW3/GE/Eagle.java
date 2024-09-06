package CommandingOfficers.AW3.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public class Eagle extends AW3Commander
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
      super("Eagle", UIUtils.SourceGames.AW3, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Eagle (AW2)\n"
          + "Green Earth's daring pilot hero. Joined the air force to honor his father's legacy.\n"
          + "Air units use less fuel than those of other armies and also have superior firepower. Naval units have weaker firepower.\n"
          + "(+20/0 stats and -2 fuel burn/day for air, -10 attack for naval)\n"));
    infoPages.add(new InfoPage(new LightningDrive(null, null),
            "Able to move non-infantry units twice during a turn, but their firepower is cut in half.\n"
          + "(Vehicles: -60/-50/-45 air/land/sea attack)\n"
          + "+10 attack and defense\n"));
    infoPages.add(new InfoPage(new LightningStrike(null, null),
            "Able to move non-infantry units twice during a turn. No firepower penalty.\n"
          + "+10 attack and defense\n"));
    infoPages.add(new InfoPage(
            "Hit: Lucky goggles\n"
          + "Miss: Swimming"));
      infoPages.add(AW3_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Eagle(rules);
    }
  }

  public Eagle(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new LightningDrive(this, cb));
    addCommanderAbility(new LightningStrike(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAirUnit() )
      params.attackPower += 20;
    else if( params.attacker.model.isSeaUnit() )
      params.attackPower -= 10;
  }
  @Override
  public void modifyIdleFuelBurn(UnitContext uc)
  {
    if( uc.model.isAirUnit() )
      uc.fuelBurnIdle -= 2;
  }

  private static class LightningDrive extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lightning Drive";
    private static final int COST = 3;
    UnitTypeFilter airMod, landMod, seaMod;

    LightningDrive(Eagle commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = PHASE_TURN_END;
      airMod = new UnitTypeFilter(new UnitDamageModifier(-60));
      airMod.allOf = UnitModel.AIR;
      airMod.noneOf = UnitModel.TROOP;
      landMod = new UnitTypeFilter(new UnitDamageModifier(-50));
      landMod.allOf = UnitModel.LAND;
      landMod.noneOf = UnitModel.TROOP;
      seaMod = new UnitTypeFilter(new UnitDamageModifier(-45));
      seaMod.allOf = UnitModel.SEA;
      seaMod.noneOf = UnitModel.TROOP;
    }

    @Override
    public void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(airMod);
      modList.add(landMod);
      modList.add(seaMod);
    }

    @Override
    protected void perform(MapMaster map)
    {
      super.perform(map);
      for( Unit u : myCommander.army.getUnits() )
      {
        if( u.model.isNone(UnitModel.TROOP) )
          u.isTurnOver = false;
      }
    }
  }

  private static class LightningStrike extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lightning Strike";
    private static final int COST = 9;

    LightningStrike(Eagle commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster map)
    {
      super.perform(map);
      for( Unit u : myCommander.army.getUnits() )
      {
        if( u.model.isNone(UnitModel.TROOP) )
          u.isTurnOver = false;
      }
    }
  }

}
