package Units.MoveTypes;

public class Ship extends MoveType
{

  public Ship()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 99, 99, 99, 99, 99, 99, 99, 99, 99, 1, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 99, 1, 2 },
        { 99, 99, 99, 99, 99, 99, 99, 99, 99, 2, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 99, 1, 2 } };

    moveCosts = tempCosts;
  }
}
