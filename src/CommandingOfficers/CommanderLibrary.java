package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.BrennerWolves.*;
import Engine.GameScenario;
import Engine.GameScenario.GameRules;
import UI.UIUtils;

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
    commanderList.add( Strong.getInfo() );
    commanderList.add( Patch.getInfo() );
    commanderList.add( Venge.getInfo() );
    commanderList.add( Meridian.getInfo() );
    commanderList.add( Bear_Bull.getInfo() );
    commanderList.add( Cinder.getInfo() );
    commanderList.add( Ave.getInfo() );
    commanderList.add( Qis.getInfo() );
    commanderList.add( Tech.getInfo() );
    commanderList.add( BrennerDoR.getInfo() );
    commanderList.add( DocLight.getInfo() );
    commanderList.add( NotACO.getInfo() );
    commanderList.add( CommandingOfficers.AW2.OS.Andy.getInfo() );
    commanderList.add( CommandingOfficers.AW3.OS.Andy.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.OS.Andy.getInfo() );
  }

  public static class NotACO extends Commander
  {
    private static final long serialVersionUID = 1L;

    private static final CommanderInfo coInfo = new instantiator();
    private static class instantiator extends CommanderInfo
    {
      private static final long serialVersionUID = 1L;
      public instantiator()
      {
        super("No CO", UIUtils.SourceGames.AW4, UIUtils.MISC);
        infoPages.add(new InfoPage("The ultimate expression of fair play"));
      }
      @Override
      public Commander create(GameScenario.GameRules rules)
      {
        return new NotACO(rules);
      }
    }

    public static CommanderInfo getInfo()
    {
      return coInfo;
    }

    public NotACO(GameRules rules)
    {
      super(coInfo, rules);
    }
  }
}
