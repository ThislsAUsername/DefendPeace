package CommandingOfficers;

import Engine.GameScenario;

import java.util.ArrayList;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.Army;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public abstract class DeployableCommander extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage COU_MECHANICS_BLURB = new InfoPage(
            "CO Unit mechanics:\n"
          + "Deploy your CO Unit via a special action done in place (does not end turn).\n"
          + "You can't deploy a CO Unit that has already been active during your turn (i.e. must wait a turn after deleting it/dying to a counter).\n");
  public ArrayList<Unit> COUs = new ArrayList<Unit>();
  public ArrayList<Unit> COUsLost = new ArrayList<Unit>();
  public final DeployCOUFactory deployAction; // Has to be one per CO, so units get the same instance over multiple action searches (mostly for the AI)
  /** The number of COUs you can have active at once */
  public abstract int getCOUCount();
  public void onCOULost(Unit minion) {};
  public char getCOUMark() {return 'C';};
  public int deployCostPercent = 0;
  /** If the unit type matches any flag in this mask, it can be my COU */
  public long canDeployMask = Long.MAX_VALUE;
  public final boolean canDeployOn(UnitModel type) {return type.isAny(canDeployMask);};

  public boolean resetCOUsEveryTurn = false;

  /** Can I deploy my COU here? Ignored if resetCOUsEveryTurn==true */
  protected boolean eligibleDeployLocation(Unit actor, MapLocation loc)
  {
    if( loc.getOwner() != actor.CO )
      return false; // No COing on neutral factories
    return getShoppingList(loc).contains(actor.model)
        || loc.getEnvironment().terrainType == TerrainType.HEADQUARTERS
        || loc.getEnvironment().terrainType == TerrainType.LAB;
  }


  public DeployableCommander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    deployAction = new DeployCOUFactory(this);
  }

  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    if( COUs.contains(unit) )
      return getCOUMark();

    return super.getUnitMarking(unit, activeArmy);
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    if( resetCOUsEveryTurn )
    {
      this.COUs.clear();
    }
    else
    {
      this.COUs.removeAll(COUsLost);
      this.COUsLost.clear();
    }
    return super.initTurn(map);
  }

  @Override
  public void endTurn()
  {
    super.endTurn();
  }

  @Override
  public void modifyActionList(UnitContext uc)
  {
    if( canDeployOn(uc.model) )
      uc.actionTypes.add(deployAction);
  }

  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer healthBeforeDeath)
  {
    // COUs die when they are killed
    if( COUs.contains(victim) )
    {
      COUsLost.add(victim);
      onCOULost(victim);
    }
    return super.receiveUnitDieEvent(victim, grave, healthBeforeDeath);
  }
  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    // COUs shouldn't vanish into the aether on joining another unit
    if( COUs.contains(join.unitDonor) )
    {
      COUs.remove(join.unitDonor);
      COUs.add(join.unitRecipient);
    }
    return super.receiveUnitJoinEvent(join);
  }

  //////////////////////////////////////////////////////////
  // Action definition happens after this point
  //////////////////////////////////////////////////////////

  public static class DeployCOUFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    final DeployableCommander deployer;
    public DeployCOUFactory(DeployableCommander owner)
    {
      deployer = owner;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( moveLocation.equals(actor.x, actor.y)
          && deployer.COUs.size() < deployer.getCOUCount()
          && (deployer.resetCOUsEveryTurn || deployer.eligibleDeployLocation(actor, map.getLocation(moveLocation))) )
      {
        return new GameActionSet(new DeployCOUAction(this, actor), false);
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      if( deployer.deployCostPercent == 0 )
        return "Deploy COU";
      return "Deploy COU (" + getDeployCost(actor) + ")";
    }

    private int getDeployCost(Unit actor)
    {
      return (deployer.deployCostPercent * deployer.getCost(actor.model)) / 100;
    }
  }

  public static class DeployCOUAction extends GameAction
  {
    final DeployCOUFactory type;
    final Unit actor;
    final XYCoord destination;
    public DeployCOUAction(DeployCOUFactory owner, Unit unit)
    {
      type = owner;
      actor = unit;
      destination = new XYCoord(unit.x, unit.y);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      if( actor.isTurnOver )
        return eventSequence;
      if( type.deployer.army.money > type.getDeployCost(actor) )
        eventSequence.add(new DeployCOUEvent(type, actor));
      return eventSequence;
    }

    @Override
    public String toString()
    {
      return String.format("[Deploy COU on %s in place]", actor.toStringWithLocation());
    }

    @Override
    public Unit getActor()
    {
      return actor;
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return destination;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return destination;
    }
  } // ~DeployCOUAction

  private static class DeployCOUEvent implements GameEvent
  {
    final DeployCOUFactory type;
    private Unit unit;
    private int cost;

    public DeployCOUEvent(DeployCOUFactory type, Unit unit)
    {
      this.type = type;
      this.unit = unit;
      cost = type.getDeployCost(unit);
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return null;
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return listener.receiveDeployCOUEvent(unit, cost);
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      type.deployer.army.money -= cost;
      type.deployer.COUs.add(unit);
    }

    @Override
    public XYCoord getStartPoint()
    {
      return new XYCoord(unit.x, unit.y);
    }

    @Override
    public XYCoord getEndPoint()
    {
      return new XYCoord(unit.x, unit.y);
    }
  } // ~DeployCOUEvent
}
