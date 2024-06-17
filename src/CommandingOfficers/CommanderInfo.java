package CommandingOfficers;

import java.io.Serializable;
import java.util.ArrayList;

import Engine.GameScenario;
import UI.UIUtils.COSpriteSpec;
import UI.UIUtils.SourceGames;
import Units.UnitModelScheme.GameReadyModels;

public abstract class CommanderInfo implements Serializable
{
  private static final long serialVersionUID = 1L;
  public final String name;
  public final SourceGames game;
  public final COSpriteSpec baseFaction;
  public final String discriminator;
  public ArrayList<InfoPage> infoPages;

  public CommanderInfo(String name, SourceGames game, COSpriteSpec f)
  {
    this(name, game, f, game.discriminator);
  }
  public CommanderInfo(String name, SourceGames game, COSpriteSpec f, String discriminator)
  {
    this.name = name;
    this.game = game;
    this.baseFaction = f;
    this.discriminator = discriminator;
    infoPages = new ArrayList<InfoPage>();
  }

  public abstract Commander create(GameScenario.GameRules rules);

  public void injectUnits(GameReadyModels grms)
  {
  }

  public String getFullName()
  {
    if( discriminator.length() > 0 )
      return name + " (" + discriminator + ")";
    return name;
  }

  public static class InfoPage implements Serializable
  {
    private static final long serialVersionUID = 1L;

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
    public InfoPage(CommanderAbility ability, String textContents)
    {
      this(PageType.BASIC, abilityHeader(ability) + textContents);
    }
    public static String abilityHeader(CommanderAbility ability)
    {
      return ability+" ("+ability.baseStars+"):\n";
    }
  }
}
