package CommandingOfficers.AW4.IDS;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.*;

public class Caulder extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Caulder", UIUtils.SourceGames.AW4, UIUtils.IDS);
      infoPages.add(new InfoPage(
          "The head of the private military contractor IDS. He seeks a world where he is free to carry out his terrible experiments.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: 3\n"
          + "Zone Boost: All units +"+POWER+"/"+DEFENSE+", repair +"+D2DREPAIRS+" HP.\n"));
      infoPages.add(new InfoPage(
          "No CO power (and cannot expand his zone)\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Caulder(rules);
    }
  }
  public static final int D2DREPAIRS = 5;
  public static final int RADIUS  = 3;
  public static final int POWER   = 50;
  public static final int DEFENSE = 50;

  public Caulder(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  protected void onTurnInit(MapMaster map, GameEventQueue events)
  {
    if( COUs.isEmpty() )
      return;

    Unit cou = COUs.get(0);
    XYCoord center = new XYCoord(cou);
    final int repairs = D2DREPAIRS*10;

    // Bounding box to limit wasted iterations
    final int minX = Math.max(cou.x - zoneRadius, 0);
    final int minY = Math.max(cou.y - zoneRadius, 0);
    final int maxX = Math.min(cou.x + zoneRadius, map.mapWidth  - 1);
    final int maxY = Math.min(cou.y + zoneRadius, map.mapHeight - 1);

    for( int y = minY; y <= maxY; y++ ) // Top to bottom, left to right
    {
      for( int x = minX; x <= maxX; x++ )
      {
        if( center.getDistance(x, y) > zoneRadius )
          continue;
        Unit resi = map.getResident(x, y);
        if( resi != null && this.army == resi.CO.army )
        {
          events.add(new HealUnitEvent(resi, repairs, this.army)); // Event handles cost logic
        }
      }
    }
  }

}
