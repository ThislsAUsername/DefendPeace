package Units.MoveTypes;

public class FloatLight extends MoveType
{

  public FloatLight()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 1, 1, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 1, 1, 2 },
        { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 1, 2, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 1, 1, 2 } };

    moveCosts = tempCosts;
  }
}
