package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.Modifiers.COModifier;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.UnitDieEvent;
import Terrain.MapMaster;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

/*
 * Cinder is based on getting an edge in the action economy, at the cost of unit health.
 */
public class CommanderCinder extends Commander
{
  private static final long serialVersionUID = 4938851182463546620L;

  private static final CommanderInfo coInfo = new CommanderInfo("Cinder", new instantiator());  
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new CommanderCinder();
    }
  }

  private static final double COST_MOD_PER_BUILD = 1.2;

  private HashMap<XYCoord, Integer> buildCounts = new HashMap<>();

  private GameEventQueue pollEvents = new GameEventQueue();

  public CommanderCinder()
  {
    super(coInfo);

    addCommanderAbility(new SearAbility(this));
    addCommanderAbility(new WitchFireAbility(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  /** Get the list of units this commander can build from the given property type. */
  public ArrayList<UnitModel> getShoppingList(Location buyLocation)
  {
    setPrices(buildCounts.get(buyLocation.getCoordinates()));
    return super.getShoppingList(buyLocation);
  }

  /*
   * Cinder builds units at 8HP ready to act.
   * To compensate for the ability to continue producing units in a single turn,
   * the cost of units increases exponentially for repeated purchases from a single property.
   */
  public void receiveCreateUnitEvent(Unit unit)
  {
    if( this == unit.CO )
    {
      XYCoord buildCoords = new XYCoord(unit.x, unit.y);
      buildCounts.put(buildCoords, buildCounts.get(buildCoords) + 1);
      setPrices(0);

      unit.alterHP(-2);
      unit.isTurnOver = false;
    }
  }

  /*
   * Purchase price problems reset at the start of turn.
   */
  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    // If we haven't initialized our buildable locations yet, do so.
    if( buildCounts.size() < 1 )
    {
      for( int x = 0; x < map.mapWidth; ++x )
        for( int y = 0; y < map.mapHeight; ++y )
        {
          Location loc = map.getLocation(x, y);
          if( loc.isCaptureable() ) // if we can't capture it, we can't build from it
            buildCounts.put(loc.getCoordinates(), 0);
        }
    }
    else // If we're initialized already, just reset our build counts
      for( XYCoord xyc : buildCounts.keySet() )
      {
        buildCounts.put(xyc, 0);
      }

    setPrices(0);
    return super.initTurn(map);
  }

  @Override
  public void endTurn()
  {
    setPrices(0);
    super.endTurn();
  }

  @Override
  public void pollForEvents(GameEventQueue eventsOut)
  {
    eventsOut.addAll(pollEvents);
    pollEvents.clear();
  }

  public void setPrices(int repetitons)
  {
    for( UnitModel um : unitModels )
    {
      um.COcost = Math.pow(COST_MOD_PER_BUILD, repetitons);
    }
  }

  /*
   * Sear causes 1 mass damage to Cinder's own troops, in exchange for refreshing them.
   */
  private static class SearAbility extends CommanderAbility
  {
    private static final String SEAR_NAME = "Sear";
    private static final int SEAR_COST = 5;
    private static final int SEAR_WOUND = -1;

    SearAbility(Commander commander)
    {
      super(commander, SEAR_NAME, SEAR_COST);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        if( unit.isTurnOver )
        {
          unit.resupply(); // the missing HP has to go somewhere...
        }
        unit.alterHP(SEAR_WOUND);
        unit.isTurnOver = false;
      }
    }
  }

  /*
   * Witchfire causes Cinder's troops to automatically refresh after attacking, at the cost of 1 HP
   */
  private static class WitchFireAbility extends CommanderAbility implements COModifier
  {
    private static final String WITCHFIRE_NAME = "Witchfire";
    private static final int WITCHFIRE_COST = 9;
    private static final int WITCHFIRE_HP_COST = 1;
    private WitchFireListener listener;

    WitchFireAbility(Commander commander)
    {
      super(commander, WITCHFIRE_NAME, WITCHFIRE_COST);
      listener = new WitchFireListener(commander, WITCHFIRE_HP_COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(this);
    }

    @Override // COModifier interface.
    public void apply(Commander commander)
    {
      GameEventListener.registerEventListener(listener);
    }

    @Override
    public void revert(Commander commander)
    {
      GameEventListener.unregisterEventListener(listener);
    }
  }

  private static class WitchFireListener extends GameEventListener
  {
    private Commander myCommander = null;
    private final int refreshCost;

    public WitchFireListener(Commander myCo, int HPCost)
    {
      myCommander = myCo;
      refreshCost = HPCost;
    }

    @Override
    public void receiveBattleEvent(BattleSummary battleInfo)
    {
      // Determine if we were part of this fight. If so, refresh at our own expense.
      Unit minion = battleInfo.attacker;
      if( minion.CO == myCommander )
      {
        int hp = minion.getHP();
        if( hp > refreshCost )
        {
          minion.alterHP(-refreshCost);
          minion.isTurnOver = false;
        }
        else
        {
          // Guess he's not gonna make it.
          // TODO: Maybe add a debuff event/animation here as well.
          UnitDieEvent event = new UnitDieEvent(minion);
          CommanderCinder Cinder = (CommanderCinder)myCommander;
          Cinder.pollEvents.add(event);
        }
      }
    }
  }
}
