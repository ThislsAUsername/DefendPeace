package CommandingOfficers;

import java.io.Serializable;

public class CommanderInfo implements Serializable
{
  public final String name;
  public final COMaker maker;

  public CommanderInfo(String name, COMaker whichCo)
  {
    this.name = name;
    maker = whichCo;
  }
}
