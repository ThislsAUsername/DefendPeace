package Units.MoveTypes;

public class FloatHeavy extends MoveType
{

  public FloatHeavy()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 1, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 1, 2 },
        { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 2, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 1, 2 } };

    moveCosts = tempCosts;
  }
}
