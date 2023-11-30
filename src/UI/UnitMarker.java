package UI;

import java.awt.Color;

import Engine.Army;
import Engine.XYCoord;
import Units.Unit;

public interface UnitMarker
{
  /**
   * Returns a character to be displayed on the unit.
   * Primary usage should be pieces of info that aren't otherwise immediately apparent from the map.
   * Our rendering only supports alphanumeric values at this time.
   */
  public default char getUnitMarking(Unit unit, Army activeArmy)
  {
    // We don't have anything useful to print, so don't.
    return '\0';
  }
  public default Color getMarkingColor(Unit unit)
  {
    return Color.white;
  }

  public default char getPlaceMarking(XYCoord coord, Army activeArmy)
  {
    // We don't have anything useful to print, so don't.
    return '\0';
  }
  public default Color getMarkingColor(XYCoord coord)
  {
    return Color.white;
  }

  public static class MarkData
  {
    public char mark;
    public Color color;
    public MarkData(char mark, Color color)
    {
      this.mark = mark;
      this.color = color;
    }
  }

  public static class CustomStatData
  {
    public char mark;
    public Color markColor;
    public Color textColor;
    public String text;
    public CustomStatData(char mark, Color markColor, Color textColor, String text)
    {
      this.mark      = mark;
      this.markColor = markColor;
      this.textColor = textColor;
      this.text      = text;
    }
  }
  public default CustomStatData getCustomStat(Unit unit)
  {
    return null;
  }
}
