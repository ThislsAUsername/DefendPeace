package UI;

import java.awt.Color;

import Units.Unit;

public interface UnitMarker
{
  /**
   * Returns a character to be displayed on the unit.
   * Primary usage should be pieces of info that aren't otherwise immediately apparent from the map.
   * Our rendering only supports alphanumeric values at this time.
   */
  public default char getUnitMarking(Unit unit)
  {
    // We don't have anything useful to print, so don't.
    return '\0';
  }
  public default Color getMarkingColor(Unit unit)
  {
    return Color.white;
  }
}
