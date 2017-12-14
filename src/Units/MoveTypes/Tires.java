package Units.MoveTypes;

public class Tires extends MoveType
{

  public Tires()
  {
    // format is [weather][terrain]
    int[][] tempCosts = { { 1, 2, 99, 3, 1, 1, 1, 1, 1, 99, 99 }, { 2, 2, 99, 2, 1, 1, 1, 1, 1, 99, 99 },
        { 2, 2, 99, 3, 1, 1, 1, 1, 1, 99, 99 }, { 1, 2, 99, 4, 1, 1, 1, 1, 1, 99, 99 } };

    moveCosts = tempCosts;
  }
}
