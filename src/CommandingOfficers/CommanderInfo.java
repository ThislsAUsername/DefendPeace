package CommandingOfficers;

public class CommanderInfo
{
  public final String name;
  public final CommanderLibrary.CommanderEnum cmdrEnum;

  public CommanderInfo(String name, CommanderLibrary.CommanderEnum whichCo)
  {
    this.name = name;
    cmdrEnum = whichCo;
  }
}
