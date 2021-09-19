package Units;

import java.io.Serializable;

import CommandingOfficers.Commander;

public class UnitDelta implements Serializable
{
  private static final long serialVersionUID = 1L;

  public final UnitContext before, after;
  public final int deltaHP, deltaAmmo, deltaFuel, deltaMaterials;
  public final double deltaPreciseHP;

  public final UnitModel model;
  public final Unit unit;
  public final Commander CO;

  public UnitDelta(UnitContext start, UnitContext end)
  {
    super();
    before = start;
    after = end;
    deltaHP = after.getHP() - before.getHP();
    deltaPreciseHP = after.getPreciseHP() - before.getPreciseHP();
    deltaAmmo = after.ammo - before.ammo;
    deltaFuel = after.fuel - before.fuel;
    deltaMaterials = after.materials - before.materials;

    model = after.model;
    unit = after.unit;
    CO = after.CO;
  }
}
