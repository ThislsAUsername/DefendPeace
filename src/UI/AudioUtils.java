package UI;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import UI.UIUtils.SourceGames;
import lombok.var;

public class AudioUtils
{
  private static final String MENU_THEME_FORMAT = "res/audio/unused/Advance-Wars%s.ogg";
  private static final String UNKNOWN_CO_FORMAT = "res/audio/aw1/unused/credits%s.ogg"; // Planning to allow selecting your theme, so a hardcoded default seems fine.
  private static HashMap<String, LoopMusic> allThemes = new HashMap<>(); // Map from music format string to the audio streams
  private static HashMap<UIUtils.SourceGames, String> sourceGameToCategory;
  static
  {
    sourceGameToCategory = new HashMap<>();
    sourceGameToCategory.put(UIUtils.SourceGames.AW1, "aw1/");
    sourceGameToCategory.put(UIUtils.SourceGames.AW2, "aw2/");
    sourceGameToCategory.put(UIUtils.SourceGames.AW3, "aw-ds/");
    sourceGameToCategory.put(UIUtils.SourceGames.AW4, "aw-dor/");
    sourceGameToCategory.put(UIUtils.SourceGames.AWBW, "aw-bw/");
    allThemes.put(MENU_THEME_FORMAT, new LoopMusic(MENU_THEME_FORMAT));
    allThemes.put(UNKNOWN_CO_FORMAT, new LoopMusic(UNKNOWN_CO_FORMAT));
    allThemes.put(null, allThemes.get(UNKNOWN_CO_FORMAT));
  }

  public static LoopMusic getMenuTheme()
  {
    return allThemes.get(MENU_THEME_FORMAT);
  }

  public static LoopMusic getCommanderTheme(Commander co)
  {
    if(co.themePathFormats.isEmpty())
      setCommanderThemePathFormats(co);

    var themeFormat = co.themePathFormats.getOrDefault(co.getActiveAbility(), null);
    if( allThemes.containsKey(themeFormat) )
      return allThemes.get(themeFormat); // handles null
    if( !canRead(themeFormat) )
      return allThemes.get(UNKNOWN_CO_FORMAT);

    // We don't have it, so we need to load it.
    allThemes.put(themeFormat, new LoopMusic(themeFormat));
    return allThemes.get(themeFormat);
  }

  ///////////////////////////////////////////////////////////////////
  //  Code for loading music
  //  Note: as these paths are stored in the savefile, the paths should be relative to the game directory and not absolute.
  ///////////////////////////////////////////////////////////////////
  public static class WrappedAudioStream
  {
    AudioInputStream dataIn  = null;
    AudioFormat targetFormat = null;
    BufferedInputStream bis  = null;
    WrappedAudioStream(String filePath)
    {
      AudioInputStream innerStream = null;
      long fileSize = 0;
      try
      {
        File file = new File(filePath);
        if( file.canRead() )
          innerStream = AudioSystem.getAudioInputStream(file);
        fileSize = file.length();
      }
      catch (UnsupportedAudioFileException | IOException e)
      {
        // Oh well, we tried.
      }
      if( null == innerStream )
        return;

      AudioFormat baseFormat = innerStream.getFormat();

      targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
                     baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

      dataIn = AudioSystem.getAudioInputStream(targetFormat, innerStream);

      if( null == dataIn )
        return;
      bis = new BufferedInputStream(dataIn, (int) fileSize);
      bis.mark(Integer.MAX_VALUE); // Mark the start of the stream, and keep it valid for the maximum duration.
    }
  }

  public static class LoopMusic
  {
    public final String path;
    public final WrappedAudioStream intro; // non-loop intro
    public final WrappedAudioStream preloop; // first loop
    public final WrappedAudioStream loop;

    public LoopMusic(String nameFormat)
    {
      path    = Engine.Driver.JAR_DIR + String.format(nameFormat, "");
      intro   = new WrappedAudioStream(Engine.Driver.JAR_DIR + String.format(nameFormat, "-intro"));
      preloop = new WrappedAudioStream(Engine.Driver.JAR_DIR + String.format(nameFormat, "-preloop"));
      loop    = new WrappedAudioStream(path);
    }
  }
  public static boolean canRead(String nameFormat)
  {
    return new File(Engine.Driver.JAR_DIR + String.format(nameFormat, "")).canRead();
  }

  public static void setCommanderThemePathFormats(Commander co)
  {
    CommanderInfo whichCO = co.coInfo;
    String nameFormat = "res/audio/%st-%s%s.ogg"; // subfolder, name, song part
    String coName     = whichCO.name.toLowerCase();

    String passiveTheme = UNKNOWN_CO_FORMAT;
    String[] passiveNamesToTry = {
      String.format(nameFormat, sourceGameToCategory.getOrDefault(whichCO.game, ""), coName, "%s"),
      String.format(nameFormat, sourceGameToCategory.get(SourceGames.AW3), coName, "%s"),
      String.format(nameFormat, sourceGameToCategory.get(SourceGames.AW2), coName, "%s"),
      String.format(nameFormat, sourceGameToCategory.get(SourceGames.AW1), coName, "%s"),
    };
    for( String ntt : passiveNamesToTry )
      if( canRead(ntt) )
      {
        passiveTheme = ntt;
        break;
      }
    co.themePathFormats.put(null, passiveTheme);

    String allyOrBH = "ally"; // AW2/DS have different power themes for BH and BH's enemies, but not for individual COs.
    if( whichCO.baseFaction.faction.name.equalsIgnoreCase(UIUtils.BH.faction.name) )
      allyOrBH = "bh";
    String genericCOPFormat  = String.format("res/audio/%st-%s-co-power%s.ogg", "%s", allyOrBH, "%s"); // subfolder, song part
    String genericSCOPFormat = String.format("res/audio/%st-%s-co-power%s.ogg", "%s", allyOrBH+"-super", "%s"); // subfolder, song part

    // AW2/DS logic: separate theme for each power
    if( co.myAbilities.size() == 2 )
    {
      co.themePathFormats.put(co.myAbilities.get(0), passiveTheme);
      String[] copNamesToTry = {
        String.format(passiveTheme, "-cop%s"),
        String.format(genericCOPFormat, sourceGameToCategory.getOrDefault(whichCO.game, ""), "%s"),
      };
      for( String ntt : copNamesToTry )
        if( new File(String.format(ntt, "")).canRead() )
        {
          co.themePathFormats.put(co.myAbilities.get(0), ntt);
          break;
        }

      co.themePathFormats.put(co.myAbilities.get(1), passiveTheme);
      String[] scopNamesToTry = {
        String.format(passiveTheme, "-scop%s"),
        String.format(passiveTheme, "-cop%s"),
        String.format(genericSCOPFormat, sourceGameToCategory.getOrDefault(whichCO.game, ""), "%s"),
      };
      for( String ntt : scopNamesToTry )
        if( canRead(ntt) )
        {
          co.themePathFormats.put(co.myAbilities.get(1), ntt);
          break;
        }
    }
    else // I dunno what's going on, so just use the same theme on all powers
    {
      for( var ability : co.myAbilities )
        co.themePathFormats.put(ability, passiveTheme);
      String[] copNamesToTry = {
        String.format(passiveTheme, "-cop%s"),
        String.format(genericSCOPFormat, sourceGameToCategory.getOrDefault(whichCO.game, ""), "%s"), // Default to SCOP since that's what Sturm uses.
        String.format(genericCOPFormat,  sourceGameToCategory.getOrDefault(whichCO.game, ""), "%s"),
      };
      for( String ntt : copNamesToTry )
        if( canRead(ntt) )
        {
          for( var ability : co.myAbilities )
            co.themePathFormats.put(ability, ntt);
          break;
        }
    }
  }

}
