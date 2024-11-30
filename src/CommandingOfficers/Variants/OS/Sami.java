package CommandingOfficers.DefendPeace.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW1.OS.Sami.PerfectMoveModifier;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.UIUtils;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;
import lombok.var;

public class Sami extends AWBWCommander
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
      super("Sami", UIUtils.SourceGames.VARIANTS, UIUtils.OS, "VM");
      infoPages.add(new InfoPage(
            "Sami (victory)\n"
          + "AW1 Sami, but with instant captures and AWBW rules.\n"
          + "Footsoldiers +20/10 stats.\n"
          + "20x capture rate, lose 5 HP on capture.\n"
          + "Cities do not give income if there is any unit (friendly or not) on them.\n"
          + "Unarmed transports +1 move. -10/0 direct vehicle combat.\n"));
      infoPages.add(new InfoPage(new DoubleTime(null, null),
            "Increases movement range (+1) for infantry and mech units. Their movement cost on all terrains is reduced to 1.\n"
          + "Footsoldiers +20/10 stats.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Chocolate\n"
          + "Miss: Cowards"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new DoubleTime(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange < 2 && params.attacker.model.isNone(UnitModel.TROOP) )
      params.attackPower -= 10;
    if( params.attacker.model.isAny(UnitModel.TROOP) )
      params.attackPower += 20;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isAny(UnitModel.TROOP) )
      params.defenseSubtraction += 10;
  }
  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    uc.capturePower += 1900;
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.model.baseCargoCapacity > 0 && uc.model.weapons.isEmpty() )
      uc.movePower += 1;
  }

  @Override
  public GameEventQueue receiveCaptureEvent(Unit unit, Commander prevOwner, MapLocation location)
  {
    GameEventQueue returnEvents = new GameEventQueue();
    if( unit.CO == this )
    {
      var victim = new ArrayList<Unit>();
      victim.add(unit);
      returnEvents.add(new MassDamageEvent(this, victim, 50, false));
    }
    return returnEvents;
  }
  @Override
  public int getIncomePerTurn()
  {
    int count = 0;
    for( XYCoord coord : ownedProperties )
    {
      // Re-check ownership just because.
      MapLocation loc = army.myView.getLocation(coord);
      boolean canProfit = loc.isProfitable();
      canProfit &= loc.getOwner() == this;
      if( canProfit )
      {
        boolean occupiedAndCity = null != loc.getResident() && TerrainType.CITY == loc.getEnvironment().terrainType;
        if( !occupiedAndCity )
          ++count;
      }
    }
    return count * (gameRules.incomePerCity + incomeAdjustment);
  }

  private static class DoubleTime extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Double Time";
    private static final int COST = 3;
    UnitTypeFilter moveMod, moveTypeMod, footAtkMod, footDefMod;

    DoubleTime(Sami commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(1));
      moveMod.oneOf = UnitModel.TROOP;
      moveTypeMod = new UnitTypeFilter(new PerfectMoveModifier());
      moveTypeMod.oneOf = UnitModel.TROOP;

      footAtkMod = new UnitTypeFilter(new UnitDamageModifier(20));
      footAtkMod.oneOf = UnitModel.TROOP;
      footDefMod = new UnitTypeFilter(new UnitDefenseModifier(10));
      footDefMod.oneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(moveTypeMod);
      modList.add(footAtkMod);
      modList.add(footDefMod);
    }
  }
}
