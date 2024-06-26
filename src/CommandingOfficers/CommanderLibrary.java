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
    commanderList.add( CommandingOfficers.AW4.BrennerWolves.Lin.getInfo() );
    commanderList.add( CommandingOfficers.AW4.BrennerWolves.Isabella.getInfo() );
    commanderList.add( BrennerDoR.getInfo() );
    commanderList.add( CommandingOfficers.AW4.Lazuria.Gage.getInfo() );
    commanderList.add( CommandingOfficers.AW4.Lazuria.Tasha.getInfo() );
    commanderList.add( CommandingOfficers.AW4.Lazuria.Forsythe.getInfo() );
    commanderList.add( CommandingOfficers.AW4.NRA.Waylon.getInfo() );
    commanderList.add( CommandingOfficers.AW4.NRA.Greyfield.getInfo() );
    commanderList.add( CommandingOfficers.AW4.IDS.Penny.getInfo() );
    commanderList.add( CommandingOfficers.AW4.IDS.Tabitha.getInfo() );
    commanderList.add( CommandingOfficers.AW4.IDS.Caulder.getInfo() );
    commanderList.add( DocLight.getInfo() );
    commanderList.add( NotACO.getInfo() );
    commanderList.add( CommandingOfficers.AW1.OS.Andy.getInfo() );
    commanderList.add( CommandingOfficers.AW1.OS.Max.getInfo() );
    commanderList.add( CommandingOfficers.AW1.OS.Sami.getInfo() );
    commanderList.add( CommandingOfficers.AW1.OS.Nell.getInfo() );
    commanderList.add( CommandingOfficers.AW1.BM.Grit.getInfo() );
    commanderList.add( CommandingOfficers.AW1.BM.Olaf.getInfo() );
    commanderList.add( CommandingOfficers.AW1.GE.Drake.getInfo() );
    commanderList.add( CommandingOfficers.AW1.GE.Eagle.getInfo() );
    commanderList.add( CommandingOfficers.AW1.YC.Sonja.getInfo() );
    commanderList.add( CommandingOfficers.AW1.YC.Kanbei.getInfo() );
    commanderList.add( CommandingOfficers.AW1.BH.Sturm.getInfo() );
    commanderList.add( CommandingOfficers.AW1.BH.SturmVS.getInfo() );
    commanderList.add( CommandingOfficers.AW2.OS.Andy.getInfo() );
    commanderList.add( CommandingOfficers.AW2.BM.Olaf.getInfo() );
    commanderList.add( CommandingOfficers.AW2.GE.Drake.getInfo() );
    commanderList.add( CommandingOfficers.AW2.BH.Sturm.getInfo() );
    commanderList.add( CommandingOfficers.AW3.OS.Andy.getInfo() );
    commanderList.add( CommandingOfficers.AW3.OS.Rachel.getInfo() );
    commanderList.add( CommandingOfficers.AW3.BM.Olaf.getInfo() );
    commanderList.add( CommandingOfficers.AW3.GE.Drake.getInfo() );
    commanderList.add( CommandingOfficers.AW3.GE.Javier.getInfo() );
    commanderList.add( CommandingOfficers.AW3.BH.VonBolt.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.OS.Andy.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.OS.Rachel.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.GE.Javier.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.BH.Sturm.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.BH.VonBolt.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.BrennerWolves.LinBW.getInfo() );
    commanderList.add( CommandingOfficers.AWBW.IDS.TabithaBW.getInfo() );
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
