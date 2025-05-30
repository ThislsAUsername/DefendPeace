package CommandingOfficers.AWBW.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public class Eagle extends AWBWCommander
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
      super("Eagle", UIUtils.SourceGames.AWBW, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Eagle (AWBW)\n"
          + "Air units gain +15% attack and +10% defense, and consume -2 fuel per day. Naval units lose -30% attack.\n"));
      infoPages.add(new InfoPage(new LightningDrive(null, null),
            "Air units attack and defense are increased to +20%.\n"
          + "+10 attack and defense\n"));
      infoPages.add(new InfoPage(new LightningStrike(null, null),
            "Air units attack and defense are increased to +20%. All non-footsoldier units may move and fire again, even if built this turn (use this power after moving!).\n"
          + "+10 attack and defense\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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
      params.attackPower += 15;
    else if( params.attacker.model.isSeaUnit() )
      params.attackPower -= 30;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isAirUnit() )
      params.defenseSubtraction += 10;
  }
  @Override
  public void modifyIdleFuelBurn(UnitContext uc)
  {
    if( uc.model.isAirUnit() )
      uc.fuelBurnIdle -= 2;
  }

  private static class LightningDrive extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lightning Drive";
    private static final int COST = 3;
    UnitTypeFilter attMod, defMod;

    LightningDrive(Eagle commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = 0; // USELESS
      attMod = new UnitTypeFilter(new UnitDamageModifier(5));
      attMod.allOf = UnitModel.AIR;
      defMod = new UnitTypeFilter(new UnitDefenseModifier(10));
      defMod.allOf = UnitModel.AIR;
    }

    @Override
    public void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(attMod);
      modList.add(defMod);
    }
  }

  private static class LightningStrike extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lightning Strike";
    private static final int COST = 9;
    UnitTypeFilter attMod, defMod;

    LightningStrike(Eagle commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags = PHASE_TURN_END;
      attMod = new UnitTypeFilter(new UnitDamageModifier(5));
      attMod.allOf = UnitModel.AIR;
      defMod = new UnitTypeFilter(new UnitDefenseModifier(10));
      defMod.allOf = UnitModel.AIR;
    }

    @Override
    public void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(attMod);
      modList.add(defMod);
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
