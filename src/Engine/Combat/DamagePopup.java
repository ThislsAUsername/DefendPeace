package Engine.Combat;

import java.awt.Color;

import Engine.XYCoord;

public class DamagePopup
{
  public final XYCoord coords;
  public final Color color;
  public String quantity;

  public DamagePopup(XYCoord coords, Color color, String quantity)
  {
    this.coords = coords;
    this.color = color;
    this.quantity = quantity;
  }
}
