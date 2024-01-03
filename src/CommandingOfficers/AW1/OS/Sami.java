package CommandingOfficers.AW1.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.DamageMultiplierDefense;
import Engine.UnitMods.DamageMultiplierOffense;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import UI.UIUtils;
import Units.UnitContext;
import Units.UnitModel;
import Units.MoveTypes.MoveType;

public class Sami extends AW1Commander
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
      super("Sami_1", UIUtils.SourceGames.AW1, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Sami (AW1)\n"
          + "A graduate of special forces training. Has a strong sense of duty.\n"
          + "Infantry and Mech units are superior. Movement range is high for transport units.\n"
          + "(Footsoldiers 1.2x/0.9x damage dealt/taken with no power active, and 1.5x capture rate.)\n"
          + "(Unarmed transports +1 move. -10/0 direct vehicle combat.)\n"));
      infoPages.add(new InfoPage(new DoubleTime(null),
            "Increases movement range (+1) for infantry and mech units. Their movement cost on all terrains is reduced to 1.\n"
          + "Footsoldiers 1.4x/0.8x damage dealt/taken (154/128 total).\n"
          + "1.1x/0.9x damage dealt/taken.\n"));
      infoPages.add(new InfoPage(
            "Hit: Chocolate\n"
          + "Miss: Cowards"));
      infoPages.add(AW1_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sami(rules);
    }
  }

  public Sami(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new DoubleTime(this));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange < 2 && params.attacker.model.isNone(UnitModel.TROOP) )
      params.attackPower -= 10;
    if( null != getActiveAbility() )
      return;
    if( params.attacker.model.isAny(UnitModel.TROOP) )
    {
      params.attackerDamageMultiplier *= 120;
      params.attackerDamageMultiplier /= 100;
    }
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( null != getActiveAbility() )
      return;
    if( params.defender.model.isAny(UnitModel.TROOP) )
    {
      params.defenderDamageMultiplier *=  90;
      params.defenderDamageMultiplier /= 100;
    }
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.model.baseCargoCapacity > 0 && uc.model.weapons.isEmpty() )
      uc.movePower += 1;
  }

  private static class DoubleTime extends AW1BasicAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Double Time";
    private static final int COST = 5;
    UnitTypeFilter moveMod;
    UnitTypeFilter moveTypeMod;
    UnitTypeFilter footAtkMod;
    UnitTypeFilter footDefMod;

    DoubleTime(Sami commander)
    {
      super(commander, NAME, COST);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(1));
      moveMod.noneOf = UnitModel.TROOP;
      moveTypeMod = new UnitTypeFilter(new PerfectMoveModifier());
      moveTypeMod.oneOf = UnitModel.TROOP;

      footAtkMod = new UnitTypeFilter(new DamageMultiplierOffense(140));
      footAtkMod.oneOf = UnitModel.TROOP;
      footDefMod = new UnitTypeFilter(new DamageMultiplierDefense(80));
      footDefMod.oneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(moveTypeMod);
      modList.add(footAtkMod);
      modList.add(footDefMod);
    }
  }

  public static class PerfectMoveModifier implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void modifyMoveType(UnitContext uc)
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        final int moveCost = uc.moveType.getMoveCost(Weathers.CLEAR, terrain);
        if( MoveType.IMPASSABLE > moveCost && moveCost > 1 )
          uc.moveType.setMoveCost(terrain, 1);
      }
    }
  }

}
