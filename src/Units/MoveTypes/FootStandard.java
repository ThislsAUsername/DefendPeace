package Units.MoveTypes;

public class FootStandard extends MoveType
{

  public FootStandard()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 1, 1, 2, 1, 1, 1, 1, 1, 1, 99, 99 }, { 1, 1, 3, 2, 1, 1, 1, 1, 1, 99, 99 },
        { 2, 2, 4, 1, 1, 1, 1, 1, 1, 99, 99 }, { 1, 1, 2, 3, 1, 1, 1, 1, 2, 99, 99 } };

    moveCosts = tempCosts;
  }
}
