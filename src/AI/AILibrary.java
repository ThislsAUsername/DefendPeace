package AI;

import java.util.ArrayList;

import CommandingOfficers.Commander;

public class AILibrary
{  
  private static ArrayList<AIMaker> AIList = null;

  public static ArrayList<AIMaker> getAIList()
  {
    if( null == AIList )
    {
      buildAIInfoList();
    }
    return AIList;
  }

  private static void buildAIInfoList()
  {
    AIList = new ArrayList<AIMaker>();
    AIList.add( new NotAnAI() );
    AIList.add( InfantrySpamAI.info );
    AIList.add( SpenderAI.info );
    AIList.add( WallyAI.info );
    AIList.add( Muriel.info );
  }

  public static class NotAnAI implements AIMaker
  {
    @Override
    public AIController create(Commander co)
    {
      return null;
    }

    @Override
    public String getName()
    {
      return "Human";
    }

    @Override
    public String getDescription()
    {
      return "Human Player. This is you.\n\nGood luck!";
    }
  }
}
