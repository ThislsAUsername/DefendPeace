package CommandingOfficers;

import java.awt.Color;
import java.util.ArrayList;

public class CommanderLibrary
{
  public enum CommanderEnum { STRONG, LION, BEAR_BULL, PATCH, CINDER, NOONE };
  
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
      case BEAR_BULL:
        co = new CommanderBear_Bull();
        break;
      case PATCH:
        co = new CommanderPatch(); // TODO
        break;
      case CINDER:
        co = new CommanderCinder();
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
