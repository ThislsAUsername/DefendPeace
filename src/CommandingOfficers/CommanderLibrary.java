package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Assorted.AllDaWaylon;
import CommandingOfficers.Assorted.Creed;
import CommandingOfficers.Assorted.Fastfield;
import CommandingOfficers.Assorted.Greyfield;
import CommandingOfficers.Assorted.Sneakfield;
import CommandingOfficers.Assorted.WaylonCS;
import CommandingOfficers.Assorted.TheBeastSturm;
import CommandingOfficers.Assorted.TheWeakBeast;
import CommandingOfficers.BlackHole.Adder;
import CommandingOfficers.BlackHole.Hawke;
import CommandingOfficers.BlackHole.Kindle;
import CommandingOfficers.BlackHole.Lash;
import CommandingOfficers.BlackHole.OmegaSturm;
import CommandingOfficers.BlackHole.Sturm;
import CommandingOfficers.BlackHole.VB;
import CommandingOfficers.BlueMoon.BillyGates;
import CommandingOfficers.BlueMoon.Colin;
import CommandingOfficers.BlueMoon.Grit;
import CommandingOfficers.BlueMoon.Olaf;
import CommandingOfficers.BlueMoon.Rojenski;
import CommandingOfficers.BlueMoon.Sasha;
import CommandingOfficers.BrennersWolves.Adderbella;
import CommandingOfficers.BrennersWolves.Brenner;
import CommandingOfficers.BrennersWolves.Isabella;
import CommandingOfficers.BrennersWolves.LinAlt;
import CommandingOfficers.BrennersWolves.Lin;
import CommandingOfficers.BrennersWolves.Will;
import CommandingOfficers.GreenEarth.DSJess;
import CommandingOfficers.GreenEarth.Drake;
import CommandingOfficers.GreenEarth.Eagle;
import CommandingOfficers.GreenEarth.Javier;
import CommandingOfficers.GreenEarth.Jess;
import CommandingOfficers.GreenEarth.WakJess;
import CommandingOfficers.IDS.CaulderAlt;
import CommandingOfficers.IDS.Caulder;
import CommandingOfficers.IDS.Cyrus;
import CommandingOfficers.IDS.OmegaCaulder;
import CommandingOfficers.IDS.OmegaTabitha;
import CommandingOfficers.IDS.PennyAlt;
import CommandingOfficers.IDS.PennyCS;
import CommandingOfficers.IDS.PennyRNG;
import CommandingOfficers.IDS.PolyTabitha;
import CommandingOfficers.IDS.TabithaBasic;
import CommandingOfficers.IDS.TabithaEngine;
import CommandingOfficers.Lazuria.Davis;
import CommandingOfficers.Lazuria.Forsythe;
import CommandingOfficers.Lazuria.Gage;
import CommandingOfficers.Lazuria.WokeGage;
import CommandingOfficers.Lazuria.Tasha;
import CommandingOfficers.Lazuria.WokeTasha;
import CommandingOfficers.OrangeStar.Andy;
import CommandingOfficers.OrangeStar.Caroline;
import CommandingOfficers.OrangeStar.Hachi;
import CommandingOfficers.OrangeStar.Jake;
import CommandingOfficers.OrangeStar.Max;
import CommandingOfficers.OrangeStar.Rachel;
import CommandingOfficers.OrangeStar.Sami;
import CommandingOfficers.OrangeStar.YuanDelta;
import CommandingOfficers.YellowComet.Grimm;
import CommandingOfficers.YellowComet.Hetler;
import CommandingOfficers.YellowComet.Kanbei;
import CommandingOfficers.YellowComet.OmegaKanbei;
import CommandingOfficers.YellowComet.Sensei;
import CommandingOfficers.YellowComet.Yamamoto;

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
    commanderList.add( WakJess.getInfo() );
    commanderList.add( DSJess.getInfo() );
    commanderList.add( Drake.getInfo() );
    commanderList.add( Javier.getInfo() );
    
    commanderList.add( Grimm.getInfo() );
    commanderList.add( Kanbei.getInfo() );
    commanderList.add( Yamamoto.getInfo() );
    commanderList.add( Sensei.getInfo() );
    commanderList.add( Hetler.getInfo() );
    
    commanderList.add( Adder.getInfo() );
    commanderList.add( Hawke.getInfo() );
    commanderList.add( Kindle.getInfo() );
    commanderList.add( Lash.getInfo() );
    commanderList.add( Sturm.getInfo() );
    commanderList.add( VB.getInfo() );

    commanderList.add( Brenner.getInfo() );
    commanderList.add( Isabella.getInfo() );
    commanderList.add( Adderbella.getInfo() );
    commanderList.add( Lin.getInfo() );
    commanderList.add( LinAlt.getInfo() );
    commanderList.add( Will.getInfo() );

    commanderList.add( Forsythe.getInfo() );
    commanderList.add( Gage.getInfo() );
    commanderList.add( WokeGage.getInfo() );
    commanderList.add( Tasha.getInfo() );
    commanderList.add( WokeTasha.getInfo() );

    commanderList.add( WaylonCS.getInfo() );
    commanderList.add( AllDaWaylon.getInfo() );
    commanderList.add( Greyfield.getInfo() );
    commanderList.add( Fastfield.getInfo() );
    commanderList.add( Sneakfield.getInfo() );
    commanderList.add( Davis.getInfo() );

    commanderList.add( Caulder.getInfo() );
    commanderList.add( CaulderAlt.getInfo() );
    commanderList.add( PennyCS.getInfo() );
    commanderList.add( PennyAlt.getInfo() );
    commanderList.add( PennyRNG.getInfo() );
    commanderList.add( TabithaBasic.getInfo() );
    commanderList.add( PolyTabitha.getInfo() );
    commanderList.add( OmegaTabitha.getInfo() );
    commanderList.add( Cyrus.getInfo() );

    commanderList.add( TheBeastSturm.getInfo() );
    commanderList.add( TheWeakBeast.getInfo() );
    commanderList.add( Creed.getInfo() );
    commanderList.add( OmegaKanbei.getInfo() );
    commanderList.add( OmegaSturm.getInfo() );
    commanderList.add( OmegaCaulder.getInfo() );
    for (CommanderInfo info : commanderList)
    {
//      System.out.println(info.name);
    }
  }
}

