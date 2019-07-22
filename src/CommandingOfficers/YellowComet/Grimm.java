package CommandingOfficers.YellowComet;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;

public class Grimm extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Grimm");
      infoPages.add(new InfoPage(
          "Grimm\r\n" + 
          "  Units gain +30% attack, but lose -20% defense\r\n" + 
          "Knucklebuster -- Offensive boost by +20%\r\n" + 
          "Haymaker -- Offensive Boost by +50%"));
    }
    @Override
    public Commander create()
    {
      return new Grimm();
    }
  }

  public Grimm()
  {
    super(coInfo);
    
    new CODamageModifier(30).applyChanges(this);
    new CODefenseModifier(-20).applyChanges(this);

    addCommanderAbility(new Knuckleduster(this));
    addCommanderAbility(new Haymaker(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Knuckleduster extends CommanderAbility
  {
    private static final String NAME = "Knuckleduster";
    private static final int COST = 3;
    private static final int VALUE = 20;

    Knuckleduster(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(VALUE));
    }
  }

  private static class Haymaker extends CommanderAbility
  {
    private static final String NAME = "Haymaker";
    private static final int COST = 6;
    private static final int VALUE = 50;

    Haymaker(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(VALUE));
    }
  }
}

