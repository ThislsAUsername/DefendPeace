package CommandingOfficers.DefendPeace.BM;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Army;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.CreateUnitEvent.AnimationStyle;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;
import lombok.var;

public class Colin extends AWBWCommander
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
      super("Colin", UIUtils.SourceGames.VARIANTS, UIUtils.BM, "SWM");
      infoPages.add(new InfoPage(
            "Colin (swarm)\n"
          + "AWBW Colin, but with more dudes instead of cheaper dudes.\n"
          + "-10 attack; every fifth build costs double and builds two units.\n"));
      infoPages.add(new InfoPage(new GoldRush(null, null),
            "Funds are multiplied by 1.5x.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new PowerOfMoney(null, null),
            "Unit attack percentage increases by (3 * Funds / 1000)%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Colin(rules);
    }
  }

  public Colin(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new GoldRush(this, cb));
    addCommanderAbility(new PowerOfMoney(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower -= 10;
  }

  public final int DOUBLE_BUILD_RATIO = 5;
  private int doubleBuildCounter = DOUBLE_BUILD_RATIO;
  private ArrayList<Unit> unitsToDouble = new ArrayList<Unit>(), spawnedUnits = new ArrayList<Unit>();
  @Override
  public GameEventQueue receiveCreateUnitEvent(Unit unit)
  {
    if( this != unit.CO )
      return null;
    if( spawnedUnits.remove(unit) )
      return null; // Don't count units I spawned as builds
    XYCoord buildCoords = new XYCoord(unit);
    if( !army.myView.isLocationValid(buildCoords) )
      return null; // We'll just ignore unit-built units for now?

    // Handle rollover + buff
    if( 1 >= doubleBuildCounter )
    {
      // When we double the unit, it's gonna count as building another unit.
      doubleBuildCounter = DOUBLE_BUILD_RATIO;
      unitsToDouble.add(unit);
    }
    else
      --doubleBuildCounter;

    return null;
  }
  // Paint my doublebuild countdown on my HQ
  @Override
  public char getPlaceMarking(XYCoord xyc, Army activeArmy)
  {
    if( army.HQLocations.contains(xyc) )
      return ("" + doubleBuildCounter).charAt(0);
    return super.getPlaceMarking(xyc, activeArmy);
  }
  // Mark units I will double
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    if( unitsToDouble.contains(unit) )
      return 'D';
    return super.getUnitMarking(unit, activeArmy);
  }
  @Override
  public int getBuyCost(UnitModel um, XYCoord coord)
  {
    UnitContext uc = getCostContext(um, coord);
    if( 1 >= doubleBuildCounter )
      uc.costRatio += 100;
    return uc.getCostTotal();
  }
  @Override
  public GameEventQueue receiveMoveEvent(Unit unit, GamePath unitPath)
  {
    if( !unitsToDouble.contains(unit) )
      return null;
    if( unitPath.getPathLength() < 2 )
      return null; // If the unit didn't move, we can't use its starting space.

    return spawnClone(unit, unitPath.getWaypoint(0));
  }
  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer healthBeforeDeath)
  {
    if( !unitsToDouble.contains(victim) )
      return null;

    return spawnClone(victim, grave);
  }
  private GameEventQueue spawnClone(Unit unit, XYCoord spawnXYC)
  {
    unitsToDouble.remove(unit);
    boolean unitIsReady = true;
    boolean allowStomping = false;
    var freeSpawn = new CreateUnitEvent(this, unit.model, spawnXYC, AnimationStyle.NONE, unitIsReady, allowStomping);
    spawnedUnits.add(freeSpawn.myNewUnit);
    var events = new GameEventQueue();
    events.add(freeSpawn);
    return events;
  }


  private static class GoldRush extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Gold Rush";
    private static final int COST = 2;

    GoldRush(Colin commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags |= PHASE_BUY;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEventQueue events = new GameEventQueue();
      int bonusFunds = myCommander.army.money / 2;
      events.add(new ModifyFundsEvent(myCommander.army, bonusFunds));
      return events;
    }
  }

  public static int calcSuperBoost(int funds)
  {
    return 3 * funds / 1000;
  }
  private static class PowerOfMoney extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Power of Money";
    private static final int COST = 6;

    PowerOfMoney(Colin commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      int boost = calcSuperBoost(myCommander.army.money);
      modList.add(new UnitDamageModifier(boost));
    }
  }

}
