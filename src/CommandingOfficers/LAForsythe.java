package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;

public class LAForsythe extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Forsythe", new instantiator());
  private static class instantiator extends COMaker
  {
    public instantiator()
    {
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

    new CODamageModifier(15).apply(this);
    new CODefenseModifier(10).apply(this);

  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
