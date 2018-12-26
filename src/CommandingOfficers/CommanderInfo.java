package CommandingOfficers;

public class CommanderInfo
{
  public final String name;
  public final COMaker maker;

  public CommanderInfo(String name, COMaker whichCo)
  {
    this.name = name;
    maker = whichCo;
  }
}
