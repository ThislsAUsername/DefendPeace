package CommandingOfficers.Variants.OS;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Engine.StateTrackers.SamiLevelTracker;
import Engine.StateTrackers.SamiLevelTracker.SamiRank;
import Engine.StateTrackers.StateTracker;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import UI.UIUtils;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;
import Units.MoveTypes.MoveType;

public class SamiLevels extends AW1Commander
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
      super("Sami", UIUtils.SourceGames.VARIANTS, UIUtils.OS, "LVL");
      infoPages.add(new InfoPage(
            "Sami (leveling)\n"
          + "AW1 Sami, but with veterancy instead of a COP.\n"
          + "Units gain a level on completing a capture or a kill, up to a maximum of 5.\n"
          + "For each level, footsoldiers gain 1.2x/0.9x damage dealt/taken and +0.5x capture rate.\n"
          + "For each level, non-footsoldiers gain gain 1.1x/0.95x damage dealt/taken.\n"
          + "Max level units gain +1 movement and perfect movement.\n"
          + "Unarmed transports are built at max level.\n"
          + "-10/10 non-footsoldiers.\n"));
      infoPages.add(new InfoPage(
            "Hit: Chocolate\n"
          + "Miss: Cowards"));
      infoPages.add(AW1_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new SamiLevels(rules);
    }
  }

  public SamiLevels(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }

  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    vetTracker = StateTracker.instance(game, SamiLevelTracker.class);
    for( Unit u : units )
      initUnitEXP(u);
  }
  @Override
  public void deInitForGame(GameInstance game)
  {
    super.deInitForGame(game);
  }

  @Override
  public GameEventQueue receiveCreateUnitEvent(Unit unit)
  {
    if( this == unit.CO )
      initUnitEXP(unit);
    return null;
  }
  protected void initUnitEXP(Unit unit)
  {
    if( unit.model.baseCargoCapacity > 0 && unit.model.weapons.isEmpty() )
      vetTracker.addExperience(unit, SamiRank.LEVEL5.exp);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    boolean isTroop = params.attacker.model.isAny(UnitModel.TROOP);
    if( !isTroop )
      params.attackPower -= 10;
    int rankBoost = isTroop ? 120 : 110;
    SamiRank rank = vetTracker.getRank(params.attacker.unit);
    for( int i = 0; i < rank.exp; ++i )
    {
      params.attackerDamageMultiplier *= rankBoost;
      params.attackerDamageMultiplier /= 100;
    }
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    UnitContext minion = params.defender;
    boolean isTroop = params.attacker.model.isAny(UnitModel.TROOP);
    if( !isTroop )
      params.defenseSubtraction -= 10;
    int rankBoost = isTroop ? 90 : 95;
    SamiRank rank = vetTracker.getRank(minion.unit);
    for( int i = 0; i < rank.exp; ++i )
    {
      params.defenderDamageMultiplier *= rankBoost;
      params.defenderDamageMultiplier /= 100;
    }
  }

  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    SamiRank rank = vetTracker.getRank(uc.unit);
    uc.capturePower += 50 * rank.exp;
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    SamiRank rank = vetTracker.getRank(uc.unit);
    if( SamiRank.LEVEL5 == rank )
      uc.movePower += 1;
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    SamiRank rank = vetTracker.getRank(uc.unit);
    if( SamiRank.LEVEL5 != rank )
      return;

    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      final int moveCost = uc.moveType.getMoveCost(Weathers.CLEAR, terrain);
      // Non-impassable, non-teleport tiles
      if( MoveType.IMPASSABLE > moveCost && moveCost > 0 )
        uc.moveType.setMoveCost(terrain, 1);
    }
  }

  SamiLevelTracker vetTracker;
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    char mark = super.getUnitMarking(unit, activeArmy);
    // Prefer non-veterancy marks, like "COU"
    if( '\0' != mark )
      return mark;
    if( this != unit.CO )
      return mark;

    return vetTracker.getRank(unit).mark;
  }

}
