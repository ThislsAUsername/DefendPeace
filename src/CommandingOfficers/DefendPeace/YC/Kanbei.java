package CommandingOfficers.DefendPeace.YC;

import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Army;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.CounterMultiplierModifier;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitFightStatModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Units.Unit;

public class Kanbei extends AWBWCommander
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
      super("Kanbei", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.YC, "FEW");
      infoPages.add(new InfoPage(
            "Kanbei (reduced unit count)\n"
          + "DS Kanbei, with AWBW rules and normal prices.\n"
          + "+20/20 stats; roughly every other unit from a given property is stunned when built.\n"));
      infoPages.add(new InfoPage(new MoraleBoost(null, null),
            "+40/10 stats, total 160/130\n"));
      infoPages.add(new InfoPage(new SamuraiSpirit(null, null),
            "+40/40 stats, total 160/160; 2.0x damage on counterattack\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Kanbei(rules);
    }
  }

  public Kanbei(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new MoraleBoost(this, cb));
    addCommanderAbility(new SamuraiSpirit(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 20;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction += 20;
  }

  public final int STARTING_STUN_COUNT = 2;
  private int firstBuildStunCount = STARTING_STUN_COUNT; // Stagger the first stun on each prop to make earlygame a little less punishing?
  private HashMap<XYCoord, Integer> stunCounters = new HashMap<>();
  @Override // Stop tracking/marking properties we don't own anymore
  public GameEventQueue receiveCaptureEvent(Unit unit, Commander prevOwner, MapLocation location){ stunCounters.remove(location.getCoordinates()); return null; };
  @Override
  public GameEventQueue receiveCreateUnitEvent(Unit unit)
  {
    if( this != unit.CO )
      return null;
    XYCoord buildCoords = new XYCoord(unit);
    if( !army.myView.isLocationValid(buildCoords) )
      return null; // We'll just ignore unit-built units for now?

    if( stunCounters.containsKey(buildCoords) )
      stunCounters.put(buildCoords, stunCounters.get(buildCoords) - 1);
    else
    {
      stunCounters.put(buildCoords, firstBuildStunCount - 1);
      // ++firstBuildStunCount;
    }
    // Handle rollover + stun
    if( 0 >= stunCounters.get(buildCoords) )
    {
      stunCounters.put(buildCoords, STARTING_STUN_COUNT);
      unit.isStunned = true;
    }

    return null;
  }
  // Paint each tile's stun countdown
  @Override
  public char getPlaceMarking(XYCoord xyc, Army activeArmy)
  {
    if( !stunCounters.containsKey(xyc) )
      return super.getPlaceMarking(xyc, activeArmy);
    int count = stunCounters.get(xyc);

    return ("" + count).charAt(0);
  }

  private static class MoraleBoost extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Morale Boost";
    private static final int COST = 4;
    UnitModifier statMod;

    MoraleBoost(Kanbei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitDamageModifier(30);
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
    }
  }

  private static class SamuraiSpirit extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Samurai Spirit";
    private static final int COST = 7;
    UnitModifier statMod, counterMod;

    SamuraiSpirit(Kanbei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitFightStatModifier(30);
      counterMod = new CounterMultiplierModifier(200);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(counterMod);
    }
  }

}
