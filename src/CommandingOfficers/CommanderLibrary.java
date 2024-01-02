package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.AW4.BrennerWolves.*;
import CommandingOfficers.DefendPeace.CyanOcean.Ave;
import CommandingOfficers.DefendPeace.CyanOcean.Patch;
import CommandingOfficers.DefendPeace.GreySky.Cinder;
import CommandingOfficers.DefendPeace.RoseThorn.Strong;
import CommandingOfficers.DefendPeace.RoseThorn.Tech;
import CommandingOfficers.DefendPeace.misc.Bear_Bull;
import CommandingOfficers.DefendPeace.misc.DocLight;
import CommandingOfficers.DefendPeace.misc.Meridian;
import CommandingOfficers.DefendPeace.misc.Qis;
import CommandingOfficers.DefendPeace.misc.Venge;
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
    commanderList.add( CommandingOfficers.AW4.BrennerWolves.Will.getInfo() );
    commanderList.add( CommandingOfficers.AW4.BrennerWolves.Isabella.getInfo() );
    commanderList.add( BrennerDoR.getInfo() );
    commanderList.add( CommandingOfficers.AW4.Lazuria.Gage.getInfo() );
    commanderList.add( CommandingOfficers.AW4.Lazuria.Tasha.getInfo() );
    commanderList.add( CommandingOfficers.AW4.Lazuria.Forsythe.getInfo() );
    commanderList.add( CommandingOfficers.AW4.NRA.Waylon.getInfo() );
    commanderList.add( CommandingOfficers.AW4.NRA.Greyfield.getInfo() );
    commanderList.add( CommandingOfficers.AW4.IDS.Caulder.getInfo() );
    commanderList.add( DocLight.getInfo() );
    commanderList.add( NotACO.getInfo() );
    commanderList.add( CommandingOfficers.AW1.OS.Andy.getInfo() );
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
