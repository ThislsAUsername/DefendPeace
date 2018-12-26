package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

public class CommanderLibrary
{
  public enum CommanderEnum { STRONG, LION, PATCH, NOONE };
  
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
  }

  public static Commander makeCommander( CommanderInfo info, Color color, String faction )
  {
    Commander co = null;
    switch (info.cmdrEnum)
    {
      case STRONG:
        co = new CommanderStrong();
        break;
      case LION: // TODO
        break;
      case PATCH:
        co = new CommanderPatch(); // TODO
        break;
      case NOONE:
        default:
          // Don't build a CO at all.
    }

    co.myColor = color;
    co.factionName = faction;

    return co;
  }
}
