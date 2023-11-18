package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

public class FEMoveTypes
{
  public static class FEBoat extends MoveTypeSea
  {
    private static final long serialVersionUID = 1L;

    public FEBoat()
    {
      setMoveCost(TerrainType.SHOAL, 2);
      setMoveCost(TerrainType.RIVER, 2);
      setMoveCost(TerrainType.SEA, 2);
      setMoveCost(TerrainType.REEF, 3);
    }
  }

  public static class FEFlight extends Flight
  {
    private static final long serialVersionUID = 1L;

    public FEFlight()
    {
      moveCosts.get(Weathers.RAIN).setAllMovementCosts(2);
    }
  }

  public static class FEFoot extends MoveTypeLand
  {
    private static final long serialVersionUID = 1L;

    public FEFoot()
    {
      setMoveCost(TerrainType.FOREST, 2);
      setMoveCost(TerrainType.DUNES, 2);
      setMoveCost(TerrainType.MOUNTAIN, 4); // Treating these as hills, not "peaks"
      setMoveCost(TerrainType.RIVER, 5);

      setMoveCost(Weathers.SNOW, TerrainType.GRASS, 2);
      setMoveCost(Weathers.RAIN, TerrainType.GRASS, 2);

      setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 3);
    }
  }
  public static class FEFootArmor extends FEFoot
  {
    private static final long serialVersionUID = 1L;

    public FEFootArmor()
    {
      setMoveCost(TerrainType.DUNES, 3);
      setMoveCost(TerrainType.MOUNTAIN, MoveType.IMPASSABLE);
      setMoveCost(TerrainType.RIVER, MoveType.IMPASSABLE);

      setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 4);
    }
  }
  public static class FEFootAxe extends FEFoot
  {
    private static final long serialVersionUID = 1L;

    public FEFootAxe()
    {
      setMoveCost(TerrainType.DUNES, 3);
      setMoveCost(TerrainType.MOUNTAIN, 3);
      setMoveCost(TerrainType.RIVER, MoveType.IMPASSABLE);

      setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 4);
    }
  }
  public static class FEFootMage extends FEFoot
  {
    private static final long serialVersionUID = 1L;

    public FEFootMage()
    {
      setMoveCost(TerrainType.DUNES, 1);
      setMoveCost(TerrainType.RIVER, MoveType.IMPASSABLE);

      setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 2);
    }
  }
  public static class FEFootPirate extends FEFootAxe
  {
    private static final long serialVersionUID = 1L;

    public FEFootPirate()
    {
      setMoveCost(TerrainType.RIVER, 2);
      setMoveCost(TerrainType.SEA, 2);
      setMoveCost(TerrainType.REEF, 2);
    }
  }

  public static class FEHoof extends MoveTypeLand
  {
    private static final long serialVersionUID = 1L;

    public FEHoof()
    {
      setMoveCost(TerrainType.FOREST, 3);
      setMoveCost(TerrainType.DUNES, 4);
      setMoveCost(TerrainType.MOUNTAIN, MoveType.IMPASSABLE);
      setMoveCost(TerrainType.RIVER, MoveType.IMPASSABLE);

      setMoveCost(Weathers.SNOW, TerrainType.GRASS, 3);
      setMoveCost(Weathers.RAIN, TerrainType.GRASS, 3);

      setMoveCost(Weathers.SANDSTORM, TerrainType.DUNES, 5);
    }
  }
  public static class FEHoofPromoted extends FEHoof
  {
    private static final long serialVersionUID = 1L;

    public FEHoofPromoted()
    {
      setMoveCost(TerrainType.MOUNTAIN, 6);
    }
  }
  public static class FEHoofNomad extends FEHoof
  {
    private static final long serialVersionUID = 1L;

    public FEHoofNomad()
    {
      setMoveCost(TerrainType.FOREST, 2);
    }
  }
  public static class FEHoofNomadPromoted extends FEHoofNomad
  {
    private static final long serialVersionUID = 1L;

    public FEHoofNomadPromoted()
    {
      setMoveCost(TerrainType.RIVER, 5);
      setMoveCost(TerrainType.MOUNTAIN, 5);
    }
  }
}
