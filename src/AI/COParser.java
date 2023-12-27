package AI;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.Army;
import Units.Unit;

/**
 * Figures out which of your COs have a special sub-type you can glean more info from.
 */
public class COParser
{
  public final ArrayList<DeployableCommander> deployableCOs;
  // Will necessarily be in the above list, but there may be more flavors later
  public final ArrayList<RuinedCommander> dorCOs;

  public COParser(Army army)
  {
    if( null == army )
      throw new NullPointerException();

    deployableCOs = new ArrayList<>();
    for( Commander co : army.cos )
    {
      if( !(co instanceof DeployableCommander) )
        continue;
      deployableCOs.add((DeployableCommander) co);
    }

    dorCOs = new ArrayList<>();
    for( Commander co : army.cos )
    {
      if( !(co instanceof RuinedCommander) )
        continue;
      dorCOs.add((RuinedCommander) co);
    }
  }

  /**
   * @return All COUs in your army
   */
  public ArrayList<Unit> getAllCOUs()
  {
    ArrayList<Unit> cous = new ArrayList<>();
    for( DeployableCommander co : deployableCOs )
    {
      for( Unit u : co.COUs )
        cous.add(u);
      for( Unit u : co.COUsLost )
        cous.remove(u);
    }

    return cous;
  }

}
