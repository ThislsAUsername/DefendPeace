package CommandingOfficers.Modifiers;

import java.io.Serializable;
import java.util.ArrayList;
import CommandingOfficers.Commander;
import Units.UnitModel;

public class COMovementModifier implements Serializable
{
  private static final long serialVersionUID = 1L;
  private int rangeChange;

  public COMovementModifier()
  {
    this(1);
  }

  public COMovementModifier(int range)
  {
    rangeChange = range;
  }

  protected final void modifyUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      um.movePower = um.movePower + rangeChange;
    }
  }

  protected final void restoreUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      um.movePower = um.movePower - rangeChange;
    }
  }

}
