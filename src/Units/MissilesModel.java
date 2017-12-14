package Units;

import java.util.Vector;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.MoveTypes.Tread;
import Units.Weapons.ArtilleryCannon;
import Units.Weapons.InfantryMGun;
import Units.Weapons.MissileMissiles;
import Units.Weapons.RocketRockets;
import Units.Weapons.WeaponModel;

public class MissilesModel extends UnitModel
{

  private static final MoveType moveType = new Tires();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new MissileMissiles() };

  public MissilesModel()
  {
    super("Missiles", UnitEnum.MISSILES, 12000, 50, 0, 4, moveType, actions, healHabs, weapons);
  }
}
