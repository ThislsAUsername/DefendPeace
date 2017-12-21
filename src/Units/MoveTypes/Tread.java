package Units.MoveTypes;

public class Tread extends MoveType
{

  public Tread()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 1, 2, 99, 2, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99 }, { 2, 3, 99, 1, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99 },
        { 2, 2, 99, 2, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99 }, { 1, 2, 99, 3, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99 } };

    moveCosts = tempCosts;
  }
}
