package Units.MoveTypes;

public class Lander extends MoveType
{

  public Lander()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 99, 99, 99, 99, 99, 99, 99, 99, 1, 1, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 1, 2 },
        { 99, 99, 99, 99, 99, 99, 99, 99, 1, 2, 2 }, { 99, 99, 99, 99, 99, 99, 99, 99, 1, 1, 2 } };

    moveCosts = tempCosts;
  }
}
