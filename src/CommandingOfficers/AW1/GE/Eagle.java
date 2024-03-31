package CommandingOfficers.AW1.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.DamageMultiplierDefense;
import Engine.UnitMods.DamageMultiplierOffense;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public class Eagle extends AW1Commander
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
      super("Eagle_1", UIUtils.SourceGames.AW1, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Eagle (AW1)\n"
          + "An ace pilot who’s as tough as nails.\n"
          + "Strongest firepower in the skies. But flounders at sea.\n"
          + "(1.15x/0.9x damage dealt/taken and -2 fuel burn/turn for air, -20 attack for naval)\n"));
      infoPages.add(new InfoPage(new LightningStrike(null),
            "Non-infantry units ordered to wait can now move again that turn. However, their ratings are lower than normal.\n"
          + "(0.8x/1.3x damage dealt/taken, including footsoldiers)\n"));
      infoPages.add(new InfoPage(
            "Hit: Lucky Goggles\n"
          + "Miss: Swimming"));
      infoPages.add(AW1_MECHANICS_BLURB);
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

    addCommanderAbility(new LightningStrike(this));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isSeaUnit() )
      params.attackPower -= 20;
    if( params.attacker.model.isAirUnit() )
    {
      params.attackerDamageMultiplier *= 115;
      params.attackerDamageMultiplier /= 100;
    }
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isAirUnit() )
    {
      params.defenderDamageMultiplier *= 90;
      params.defenderDamageMultiplier /= 100;
    }
  }
  @Override
  public void modifyIdleFuelBurn(UnitContext uc)
  {
    if( uc.model.isAirUnit() )
      uc.fuelBurnIdle -= 2;
  }

  private static class LightningStrike extends AW1Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lightning Strike";
    private static final int COST = 10;
    UnitModifier attMod;
    UnitModifier defMod;

    LightningStrike(Eagle commander)
    {
      super(commander, NAME, COST);
      AIFlags = PHASE_TURN_END;
      attMod = new DamageMultiplierOffense(80);
      defMod = new DamageMultiplierDefense(130);
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(attMod);
      modList.add(defMod);
    }
    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit u : myCommander.army.getUnits() )
      {
        if( u.model.isNone(UnitModel.TROOP) )
          u.isTurnOver = false;
      }
      super.perform(gameMap);
    }
  }

}
