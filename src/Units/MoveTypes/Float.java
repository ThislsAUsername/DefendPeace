package Units.MoveTypes;

public class Float extends MoveType
{

  public Float()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 1, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 1, 2 },
        { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 2, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 99, 99, 99, 1, 2 } };

    moveCosts = tempCosts;
  }
}
