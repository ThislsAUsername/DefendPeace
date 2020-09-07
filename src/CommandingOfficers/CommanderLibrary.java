package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Assorted.*;
import CommandingOfficers.BlackHole.*;
import CommandingOfficers.BlueMoon.*;
import CommandingOfficers.BrennersWolves.*;
import CommandingOfficers.GreenEarth.*;
import CommandingOfficers.IDS.*;
import CommandingOfficers.Lazuria.*;
import CommandingOfficers.OrangeStar.*;
import CommandingOfficers.YellowComet.*;

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
    commanderList.add( Tech.getInfo() );
    
    
    
    commanderList.add( Andy.getInfo() );
    commanderList.add( Hachi.getInfo() );
    commanderList.add( YuanDelta.getInfo() );
    commanderList.add( Jake.getInfo() );
    commanderList.add( Max.getInfo() );
    commanderList.add( Rachel.getInfo() );
    commanderList.add( Caroline.getInfo() );
    commanderList.add( Sami.getInfo() );
    
    commanderList.add( Colin.getInfo() );
    commanderList.add( BillyGates.getInfo() );
    commanderList.add( Grit.getInfo() );
    commanderList.add( Rojenski.getInfo() );
    commanderList.add( Olaf.getInfo() );
    commanderList.add( Sasha.getInfo() );
    
    commanderList.add( Eagle.getInfo() );
    commanderList.add( Jess.getInfo() );
    commanderList.add( DSJess.getInfo() );
    commanderList.add( Drake.getInfo() );
    commanderList.add( Javier.getInfo() );
    commanderList.add( Javier1T.getInfo() );
    
    commanderList.add( Grimm.getInfo() );
    commanderList.add( Kanbei.getInfo() );
    commanderList.add( Yamamoto.getInfo() );
    commanderList.add( Spannbei.getInfo() );
    commanderList.add( Sonja.getInfo() );
    commanderList.add( Sensei.getInfo() );
    commanderList.add( Hetler.getInfo() );
    
    commanderList.add( Adder.getInfo() );
    commanderList.add( Koal.getInfo() );
    commanderList.add( Hawke.getInfo() );
    commanderList.add( Kindle.getInfo() );
    commanderList.add( Lash.getInfo() );
    commanderList.add( Sturm.getInfo() );
    commanderList.add( VB.getInfo() );

    commanderList.add( Brenner.getInfo() );
    commanderList.add( Isabella.getInfo() );
    commanderList.add( Link.getInfo() );
    commanderList.add( Will.getInfo() );

    commanderList.add( Forsythe.getInfo() );
    commanderList.add( Gage.getInfo() );
    commanderList.add( Tasha.getInfo() );

    commanderList.add( AllDaWaylon.getInfo() );
    commanderList.add( Fastfield.getInfo() );
    commanderList.add( Sneakfield.getInfo() );

    commanderList.add( Caulder.getInfo() );
    commanderList.add( CaulderAlt.getInfo() );
    commanderList.add( PennyAlt.getInfo() );
    commanderList.add( TabithaBasic.getInfo() );
    commanderList.add( OmegaTabitha.getInfo() );
    commanderList.add( Cyrus.getInfo() );

    commanderList.add( TheBeastSturm.getInfo() );
    commanderList.add( Creed.getInfo() );
    commanderList.add( OmegaKanbei.getInfo() );
    commanderList.add( OmegaKoal.getInfo() );
    commanderList.add( OmegaSturm.getInfo() );
    commanderList.add( OmegaCaulder.getInfo() );

    commanderList.add( HealyBoi.getInfo() );
//    for (CommanderInfo info : commanderList)
//    {
//      System.out.println(info.name);
//    }
  }
}

