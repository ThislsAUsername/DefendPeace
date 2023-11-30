package Engine;

import java.io.Serializable;

import Units.Unit;

public class XYCoord implements Serializable
{
  private static final long serialVersionUID = 1L;
  public final int x;
  public final int y;

  public XYCoord(int x, int y)
  {
    this.x = x;
    this.y = y;
  }
  public XYCoord(Unit u)
  {
    this(u.x, u.y);
  }

  public boolean equals(int x, int y)
  {
    return x == this.x && y == this.y;
  }
  
  public int getDistance(XYCoord other)
  {
    return getDistance(other.x, other.y);
  }
  public int getDistance(Unit unit)
  {
    return getDistance(unit.x, unit.y);
  }
  public int getDistance(int x, int y)
  {
    return Math.abs(x - x) + Math.abs(y - y);
  }

  @Override
  public String toString()
  {
    return "(" + x + ", " + y + ")";
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + x;
    result = prime * result + y;
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
    if( x != other.x )
      return false;
    if( y != other.y )
      return false;
    return true;
  }

  public XYCoord up()
  {
    return new XYCoord(x, y-1);
  }
  public XYCoord down()
  {
    return new XYCoord(x, y+1);
  }
  public XYCoord left()
  {
    return new XYCoord(x-1, y);
  }
  public XYCoord right()
  {
    return new XYCoord(x+1, y);
  }
}
