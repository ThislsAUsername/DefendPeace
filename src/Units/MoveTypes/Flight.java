package Units.MoveTypes;

public class Flight extends MoveType
{

  public Flight()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 }, { 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };

    moveCosts = tempCosts;
  }
}
