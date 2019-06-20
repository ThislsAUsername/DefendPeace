package CommandingOfficers;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class CommanderInfo implements Serializable
{
  public final String name;
  public ArrayList<InfoPage> infoPages;

  public CommanderInfo(String name)
  {
    this.name = name;
    infoPages = new ArrayList<InfoPage>();
  }

  public abstract Commander create();

  public static class InfoPage implements Serializable
  {
    public enum PageType {
      CO_HEADERS, GAME_STATUS, BASIC
    }

    public final PageType pageType;
    public final String info;

    public InfoPage(PageType type)
    {
      this(type, "");
    }
    public InfoPage(String textContents)
    {
      this(PageType.BASIC, textContents);
    }
    public InfoPage(PageType type, String textContents)
    {
      pageType = type;
      info = textContents;
    }
  }
}
