package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;

public class LAForsythe extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Forsythe", new instantiator());
  private static class instantiator extends COMaker
  {
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
