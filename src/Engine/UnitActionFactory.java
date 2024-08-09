package Engine;

import java.io.Serializable;

import Engine.UnitActionLifecycles.BattleLifecycle;
import Engine.UnitActionLifecycles.CaptureLifecycle;
import Engine.UnitActionLifecycles.DeleteLifecycle;
import Engine.UnitActionLifecycles.JoinLifecycle;
import Engine.UnitActionLifecycles.LaunchLifecycle;
import Engine.UnitActionLifecycles.LoadLifecycle;
import Engine.UnitActionLifecycles.RepairLifecycle;
import Engine.UnitActionLifecycles.ResupplyLifecycle;
import Engine.UnitActionLifecycles.UnloadLifecycle;
import Engine.UnitActionLifecycles.WaitLifecycle;
import Terrain.GameMap;
import Units.Unit;

public abstract class UnitActionFactory implements Serializable
{
  private static final long serialVersionUID = 1L;

  public GameActionSet getGUIActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
  {
    return getPossibleActions(map, movePath, actor, ignoreResident);
  }
  public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor)
  {
    return getPossibleActions(map, movePath, actor, false);
  }
  public abstract GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident);
  public abstract String name(Unit actor);
  public boolean shouldConfirm = false;

  public static final UnitActionFactory ATTACK = new BattleLifecycle.BattleFactory();
  public static final UnitActionFactory UNLOAD = new UnloadLifecycle.UnloadFactory();
  public static final UnitActionFactory LAUNCH = new LaunchLifecycle.LaunchFactory();
  public static final UnitActionFactory CAPTURE = new CaptureLifecycle.CaptureFactory();
  public static final UnitActionFactory RESUPPLY = new ResupplyLifecycle.ResupplyFactory();
  public static final UnitActionFactory REPAIR_UNIT = new RepairLifecycle.RepairFactory();
  public static final UnitActionFactory WAIT = new WaitLifecycle.WaitFactory();
  public static final UnitActionFactory DELETE = new DeleteLifecycle.DeleteFactory();
  public static final UnitActionFactory LOAD = new LoadLifecycle.LoadFactory();
  public static final UnitActionFactory JOIN = new JoinLifecycle.JoinFactory();

  public static final UnitActionFactory[] FOOTSOLDIER_ACTIONS =      { ATTACK, CAPTURE,  WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionFactory[] COMBAT_VEHICLE_ACTIONS =   { ATTACK,           WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionFactory[] COMBAT_TRANSPORT_ACTIONS = { ATTACK, UNLOAD,   WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionFactory[] TRANSPORT_ACTIONS =        { UNLOAD,           WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionFactory[] APC_ACTIONS =              { UNLOAD, RESUPPLY, WAIT, DELETE, LOAD, JOIN };
  public static final UnitActionFactory[] BASIC_ACTIONS =            {                   WAIT, DELETE, LOAD, JOIN };

}
