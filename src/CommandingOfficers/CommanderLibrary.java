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
    commanderList.add( CommanderVenge.getInfo() );
    commanderList.add( CommanderBear_Bull.getInfo() );
    commanderList.add( CommanderCinder.getInfo() );
    commanderList.add( CommanderAve.getInfo() );
  }

  public static Commander makeCommander( CommanderInfo info, Color color )
  {
    Commander co = info.maker.create();

    co.myColor = color;

    return co;
  }
}
