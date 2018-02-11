package Engine;

public class XYCoord
{

  public final int xCoord;
  public final int yCoord;

  public XYCoord(int x, int y)
  {
    xCoord = x;
    yCoord = y;
  }

  public boolean equals(int x, int y)
  {
    return x == xCoord && y == yCoord;
  }

  @Override
  public String toString()
  {
    return "(" + xCoord + ", " + yCoord + ")";
  }
}
