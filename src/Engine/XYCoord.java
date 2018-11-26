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

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + xCoord;
    result = prime * result + yCoord;
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if( this == obj )
      return true;
    if( obj == null )
      return false;
    if( getClass() != obj.getClass() )
      return false;
    XYCoord other = (XYCoord) obj;
    if( xCoord != other.xCoord )
      return false;
    if( yCoord != other.yCoord )
      return false;
    return true;
  }
}
