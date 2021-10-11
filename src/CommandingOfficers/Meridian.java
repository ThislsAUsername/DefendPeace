package CommandingOfficers;

import java.util.ArrayList;
import java.util.Collection;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Engine.UnitMods.TransformationTracker;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

/**
 * Commander Meridian focuses on a unique brand of flexibility; she transforms her tanks and artillery into each other 
 */
public class Meridian extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Meridian");
      infoPages.add(new InfoPage(
          "While other commanders may focus on granular tactics, Meridian has taken a different line: her basic tanks and artillery are built from the same parts, and can be re-configured on the field at no cost.\n" +
          "This gives her forces extreme adaptability, and her powers hone this to a razor edge."));
      infoPages.add(new InfoPage(
          "Passive:\n" + 
          "- Tanks and artillery cost the average of their two prices.\n" +
          "- Tanks and artillery may transform into each other at no cost"));
      infoPages.add(new InfoPage(
          "Change and Flow ("+ChangeAndFlow.COST+"):\n" +
          "Gives an attack and defense boost of "+ChangeAndFlow.BASIC_BUFF+"%\n" +
          "Any unit that has transformed this turn is refreshed."));
      infoPages.add(new InfoPage(
          "Vehicular Charge ("+VehicularCharge.COST+"):\n" +
          "Gives an attack and defense boost of "+VehicularCharge.BASIC_BUFF+"%\n" +
          "Refreshes all land vehicles, but refreshed units suffer an attack and defense penalty of "+POST_REFRESH_STAT_ADJUSTMENT+"%\n"));
      infoPages.add(new InfoPage(
          "Meridian concept credit:\n" +
          "@KvoidDragon#6786 Discord ID 542848671809798166"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Meridian(rules);
    }
  }

  /** A list of all the units I've refreshed and need to nerf. */
  final ChangeAndFlow changeAndFlow = new ChangeAndFlow();
  private ArrayList<Unit> toBeNerfed = new ArrayList<Unit>();
  private static final int POST_REFRESH_STAT_ADJUSTMENT = -25;

  public Meridian(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    // Meridian's basic tanks and arty cost the same
    UnitModel tank = getUnitModel(UnitModel.ASSAULT);
    UnitModel arty = getUnitModel(UnitModel.SIEGE);
    int costShift = (tank.getCost() - arty.getCost())/2;
    tank.costShift -= costShift;
    arty.costShift += costShift;
    tank.possibleActions.add(new TransformLifecycle.TransformFactory(arty, "~ARTY"));
    arty.possibleActions.add(new TransformLifecycle.TransformFactory(tank, "~TANK"));

    addCommanderAbility(changeAndFlow);
    addCommanderAbility(new VehicularCharge(this));
  }

  @Override
  public void registerForEvents(GameInstance game)
  {
    super.registerForEvents(game);
    changeAndFlow.init(game);
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    toBeNerfed.clear();
    return super.initTurn(map);
  }

  /**
   * Troops that have been refreshed by Meridian's bigger power get a stat nerf
   */
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( toBeNerfed.contains(params.attacker.unit) )
    {
      params.attackPower += POST_REFRESH_STAT_ADJUSTMENT;
    }
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( toBeNerfed.contains(params.defender.unit) )
    {
      params.defensePower += POST_REFRESH_STAT_ADJUSTMENT;
    }
  }

  /**
   * Change and Flow refreshes units that have transformed this turn
   */
  private static class ChangeAndFlow extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Change and Flow";
    private static final int COST = 4;
    private static final int BASIC_BUFF = 10;
    
    CODamageModifier damageMod = null;
    CODefenseModifier defenseMod = null;
    TransformationTracker tracker;

    ChangeAndFlow()
    {
      super(NAME, COST);

      damageMod = new CODamageModifier(BASIC_BUFF);
      defenseMod = new CODefenseModifier(BASIC_BUFF);
      AIFlags = 0; // The AI doesn't know how to use this, so it shouldn't try
    }
    public void init(GameInstance game)
    {
      tracker = TransformationTracker.initialize(game, TransformationTracker.class);
    }

    @Override
    protected void enqueueCOMods(Commander co, MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(damageMod);
      modList.add(defenseMod);
    }

    @Override
    protected void perform(Commander co, MapMaster gameMap)
    {
      // Units that transformed are refreshed and able to move again.
      for( Unit unit : tracker.prevTypeMap.keySet() )
      {
        if( co == unit.CO ) // Consider validating that it actually was an arty-tank transformation?
          unit.isTurnOver = false;
      }
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(Commander co, GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      for( Unit unit : tracker.prevTypeMap.keySet() )
        if( co == unit.CO )
          output.add(new DamagePopup(
                       new XYCoord(unit.x, unit.y),
                       co.myColor,
                       "Flow"));

      return output;
    }
  }

  /**
   * Vehicular Charge refreshes all ground vehicles, at the cost of a stat penalty to the refreshed units
   */
  private static class VehicularCharge extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Vehicular Charge";
    private static final int COST = 6;
    private static final int BASIC_BUFF = 10;

    CODamageModifier damageMod = null;
    CODefenseModifier defenseMod = null;
    Meridian COcast;

    VehicularCharge(Meridian commander)
    {
      super(NAME, COST);

      damageMod = new CODamageModifier(BASIC_BUFF);
      defenseMod = new CODefenseModifier(BASIC_BUFF);
      COcast = commander;
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void enqueueCOMods(Commander co, MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(damageMod);
      modList.add(defenseMod);
    }

    @Override
    protected void perform(Commander co, MapMaster gameMap)
    {
      // TODO: Handle with a UnitModifier
      // Lastly, all land vehicles are refreshed and able to move again.
      for( Unit unit : COcast.units )
      {
        if( unit.model.isAll(UnitModel.TANK) )
        {
          if (unit.isTurnOver)
            COcast.toBeNerfed.add(unit);
          unit.isTurnOver = false;
        }
      }
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
