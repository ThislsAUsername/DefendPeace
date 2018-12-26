package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

public class CommanderLibrary
{  
  private static ArrayList<CommanderInfo> commanderList = null;

  public static ArrayList<CommanderInfo> getCommanderList()
  {
    if( null == commanderList )
    {
      buildCommanderList();
    }
    return commanderList;
  }

  private static void buildCommanderList()
  {
    commanderList = new ArrayList<CommanderInfo>();
    commanderList.add( CommanderStrong.getInfo() );
    commanderList.add( CommanderPatch.getInfo() );
    commanderList.add( CommanderBear_Bull.getInfo() );
    commanderList.add( CommanderCinder.getInfo() );
    commanderList.add( OSAndy.getInfo() );
    commanderList.add( OSHachi.getInfo() );
    commanderList.add( OSJake.getInfo() );
    commanderList.add( OSMax.getInfo() );
    commanderList.add( OSRachel.getInfo() );
    commanderList.add( OSSami.getInfo() );
    commanderList.add( BMColin.getInfo() );
    commanderList.add( BMGrit.getInfo() );
    commanderList.add( BMOlaf.getInfo() );
    commanderList.add( BMSasha.getInfo() );
  }

  public static Commander makeCommander( CommanderInfo info, Color color, String faction )
  {
    Commander co = info.maker.create();
    co.myColor = color;
    co.factionName = faction;

    return co;
  }
}
