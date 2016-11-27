package Units.MoveTypes;

public class FootMech extends MoveType
{

  public FootMech()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99 }, { 1, 1, 2, 1, 1, 1, 1, 1, 1, 99, 99 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 99, 99 } };

    moveCosts = tempCosts;
  }
}
