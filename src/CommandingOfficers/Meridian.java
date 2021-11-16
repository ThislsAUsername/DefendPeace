package CommandingOfficers;

import java.util.ArrayList;
import java.util.Collection;
import CommandingOfficers.Modifiers.UnitFightStatModifier;
import CommandingOfficers.Modifiers.UnitInstanceFilter;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
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

  final VehicularCharge myVehicularCharge = new VehicularCharge(this);
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

    addCommanderAbility(new ChangeAndFlow(this));
    addCommanderAbility(myVehicularCharge);
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    return super.initTurn(map);
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
    
    UnitFightStatModifier baseMod = new UnitFightStatModifier(BASIC_BUFF);
    TransformationTracker tracker;

    ChangeAndFlow(Meridian meridian)
    {
      super(meridian, NAME, COST);

      AIFlags = 0; // The AI doesn't know how to use this, so it shouldn't try
    }
    @Override
    public void initForGame(GameInstance game)
    {
      tracker = TransformationTracker.instance(game, TransformationTracker.class);
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(baseMod);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Units that transformed are refreshed and able to move again.
      for( Unit unit : tracker.prevTypeMap.keySet() )
      {
        if( myCommander == unit.CO ) // Consider validating that it actually was an arty-tank transformation?
          unit.isTurnOver = false;
      }
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      for( Unit unit : tracker.prevTypeMap.keySet() )
        if( myCommander == unit.CO )
          output.add(new DamagePopup(
                       new XYCoord(unit.x, unit.y),
                       myCommander.myColor,
                       "Flow"));

      return output;
    }
  }

  @Override
  public char getUnitMarking(Unit unit)
  {
    // If we ever allow COs other than our own to *activate* abilities, then this is gonna have to move to a StateTracker
    if( myVehicularCharge.debuffedUnits.contains(unit) )
      return 'C';

    return super.getUnitMarking(unit);
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
    private final ArrayList<Unit> debuffedUnits = new ArrayList<Unit>();

    UnitFightStatModifier baseMod = new UnitFightStatModifier(BASIC_BUFF);
    UnitFightStatModifier debuffMod = new UnitFightStatModifier(POST_REFRESH_STAT_ADJUSTMENT);

    VehicularCharge(Meridian meridian)
    {
      super(meridian, NAME, COST);

      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(baseMod);
      UnitInstanceFilter uif = new UnitInstanceFilter(debuffMod);
      for( Unit unit : myCommander.units )
      {
        if( shouldRefresh(unit) )
          uif.instances.add(unit);
      }
      modList.add(uif);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Lastly, all land vehicles are refreshed and able to move again.
      for( Unit unit : myCommander.units )
      {
        if( shouldRefresh(unit) )
        {
          debuffedUnits.add(unit);
          unit.isTurnOver = false;
        }
      }
    }
    @Override
    protected void revert(MapMaster gameMap)
    {
      debuffedUnits.clear();
    }
    protected boolean shouldRefresh(Unit unit)
    {
      return unit.model.isAll(UnitModel.TANK) && unit.isTurnOver;
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
