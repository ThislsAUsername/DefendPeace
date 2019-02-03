package CommandingOfficers;

import java.util.ArrayList;

import UI.COSetupInfo;

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
  }

  public static Commander makeCommander( COSetupInfo info )
  {
    Commander co = info.getCurrentCO().maker.create();

    co.myColor = info.getCurrentColor();
    co.factionName = info.getCurrentFaction();
    
    co.team = info.getCurrentTeam();
    
    co.setAIController(info.getCurrentAI().create(co));

    return co;
  }
}
