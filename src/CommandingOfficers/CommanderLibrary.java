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
    commanderList.add( Andy.getInfo() );
    commanderList.add( Hachi.getInfo() );
    commanderList.add( Jake.getInfo() );
    commanderList.add( Max.getInfo() );
    commanderList.add( Rachel.getInfo() );
    commanderList.add( Sami.getInfo() );
    commanderList.add( Colin.getInfo() );
  }

  public static Commander makeCommander( CommanderInfo info, Color color, String faction )
  {
    Commander co = info.maker.create();
    co.myColor = color;
    co.factionName = faction;

    return co;
  }
}
