package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;

public class LAForsythe extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Forsythe");
      infoPages.add(new InfoPage(
          "--FORSYTHE--\r\n" + 
          "Units gain +15% firepower and +10% defense.\r\n" + 
          "NO CO POWERS"));
    }
    @Override
    public Commander create()
    {
      return new LAForsythe();
    }
  }

  public LAForsythe()
  {
    super(coInfo);

    new CODamageModifier(15).applyChanges(this);
    new CODefenseModifier(10).applyChanges(this);

  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}

