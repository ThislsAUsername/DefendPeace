package Units;

import Engine.UnitActionType;
import Terrain.TerrainType;
import Units.MoveTypes.MoveType;
import Units.Weapons.PipeGun;
import Units.Weapons.WeaponModel;

public class PiperunnerModel extends UnitModel
{
  private static final long serialVersionUID = 1L;
  private static final int UNIT_COST = 20000;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int VISION_RANGE = 4;
  private static final int MOVE_POWER = 9;

  private static final MoveType moveType = new MoveType();
  private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
  private static final WeaponModel[] weapons = { new PipeGun() };

  public PiperunnerModel()
  {
    super("Piperunner", UnitEnum.PIPERUNNER, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType, actions, weapons);
    propulsion.setMoveCost(TerrainType.PILLAR, 1);
    propulsion.setMoveCost(TerrainType.FACTORY, 1);
  }
}
