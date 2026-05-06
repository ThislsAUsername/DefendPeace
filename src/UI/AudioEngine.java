package UI;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import Engine.ConfigUtils;
import Engine.GameInstance;
import Engine.OptionSelector;
import UI.AudioUtils.LoopMusic;
import lombok.var;

public class AudioEngine
{
  // Define global settings.
  private static String[] DEFAULT_SOUND_DEVICES = {"default", "None"};

  // Set up configurable options.
  public static GameOption<String> soundDeviceOption = new GameOption<String>("Sound Device", DEFAULT_SOUND_DEVICES, 0);
  public static GameOption<Integer> volumeOption = new GameOptionInt("Volume", 0, 100, 1, 42);
  public static GameOption<?>[] allOptions = { soundDeviceOption, volumeOption };
  public static OptionSelector highlightedOption = new OptionSelector(allOptions.length);
  public static SlidingValue animHighlightedOption = new SlidingValue(0);

  private static SoundThread soundThread;
  public static void initialize()
  {
    soundDeviceOption.optionList.clear();
    for (var opt : DEFAULT_SOUND_DEVICES)
      soundDeviceOption.optionList.add(opt);
    AudioFormat baseFormat = AudioUtils.loadMenuTheme().af;
    AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
          baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

    // get a line from a mixer in the system with the wanted format
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
    Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    for( var m : mixers )
    {
      var mix = AudioSystem.getMixer(m);
      String name = m.getName();
      //System.out.println("Got " + name);
      if (! mix.isLineSupported(info))
        continue;
      soundDeviceOption.optionList.add(name);
    }
    soundDeviceOption.reset(soundDeviceOption.optionList.size()); // Allow selecting the new options

    // Load saved settings from disk, if they exist.
    loadSettingsFromDisk();

    if( !soundDeviceOption.getSelectedObject().equals("None") )
      soundThread = new SoundThread(soundDeviceOption.getSelectedObject());
  }

  private static GameInstance activeGame = null;
  public static void setActiveGame(GameInstance gi)
  {
    activeGame = gi;
  }

  public static boolean handleOptionsInput(InputHandler.InputAction action)
  {
    boolean exitMenu = false;

    switch (action)
    {
      case SELECT:
        applyConfigOptions();
        break;
      case BACK:
        resetConfigOptions();
        exitMenu = true;
        break;
      case UP:
      case DOWN:
        highlightedOption.handleInput(action);
        animHighlightedOption.set(highlightedOption.getSelectionNormalized());
        break;
      case LEFT:
      case RIGHT:
        allOptions[highlightedOption.getSelectionNormalized()].handleInput(action);
        break;
      case SEEK:
      case VIEWMODE:
        break;
    }

    return exitMenu;
  }

  /**
   * Take the settings currently held in the ConfigOption objects and persist them
   * in the class data.
   */
  public static void applyConfigOptions()
  {
    if( soundDeviceOption.isChanged() )
    {
      if( null != soundThread )
        soundThread.join();
      if( !soundDeviceOption.getSelectedObject().equals("None") )
        soundThread = new SoundThread(soundDeviceOption.getSelectedObject());
    }

    // Persist the values in the GameOption objects.
    for( GameOption<?> go : allOptions )
    {
      go.storeCurrentValue();
    }

    // Store the options locally.
    saveSettingsToDisk();
  }

  /**
   * Set the config options to the values currently stored in the class data.
   */
  private static void resetConfigOptions()
  {
    for( GameOption<?> go : AudioEngine.allOptions )
    {
      go.loseChanges();
    }

    highlightedOption.setSelectedOption(0);
    animHighlightedOption.set(0);
  }

  //////////////////////////////////////////////////////////////////////
  //  File utility functions.
  //////////////////////////////////////////////////////////////////////
  private static final String OPTIONS_FILENAME = Engine.Driver.JAR_DIR + "res/audio_options.txt";

  private static void saveSettingsToDisk()
  {
    if( !ConfigUtils.writeConfigs(OPTIONS_FILENAME, Arrays.asList(allOptions)) )
      System.out.println("Unable to write audio options to file.");
  }

  private static void loadSettingsFromDisk()
  {
    boolean allValid = ConfigUtils.readConfigs(OPTIONS_FILENAME, Arrays.asList(allOptions));
    if( !allValid )
      System.out.println("Unable to read all audio options from file.");
  }

  //////////////////////////////////////////////////////////////////////
  //  Actual sound control.
  //////////////////////////////////////////////////////////////////////
  public static class SoundThread implements Runnable
  {
    private Thread myThread;
    private String mixerName;
    private boolean killThread = false;
    private static enum LoopState { INTRO, PRELOOP, LOOP };
    private LoopMusic loopAudio = null;
    private LoopState loopState = LoopState.INTRO;
    private BufferedInputStream loopStream = null;

    public SoundThread(String pMixerName)
    {
      myThread = new Thread(this);
      myThread.start();
      mixerName = pMixerName;
    }

    private void setStream(boolean increment) throws IOException
    {
      // System.out.println("setStream " + loopState + " inc? " + increment);
      if( increment )
      {
        if( LoopState.LOOP == loopState )
        {
          loopStream.reset();
          return;
        }
        if( LoopState.PRELOOP == loopState )
          loopState = LoopState.LOOP;
        if( LoopState.INTRO == loopState )
          loopState = LoopState.PRELOOP;
      }
      switch (loopState)
      {
        case INTRO:
        {
          var newStream = loopAudio.intro;
          if (null != newStream && newStream != loopStream)
          {
            // System.out.println("Starting intro");
            loopStream = newStream;
            loopStream.reset();
            break;
          }
        } // FALLTHROUGH
        case PRELOOP:
        {
          var newStream = loopAudio.preloop;
          if (null != newStream && newStream != loopStream)
          {
            // System.out.println("Starting preloop");
            loopStream = newStream;
            loopStream.reset();
            break;
          }
        } // FALLTHROUGH
        case LOOP:
        {
          var newStream = loopAudio.loop;
          if (null != newStream && newStream != loopStream)
          {
            // System.out.println("Starting loop");
            loopStream = newStream;
            loopStream.reset();
          }
          break;
        }
      }
    }

    @Override
    public void run()
    {
      byte[] buffer = new byte[4096];
      try // credit: https://github.com/Trilarion/java-vorbis-support/blob/master/README.md
      {
        // Always start with the menu theme 'cause why not?
        loopAudio = AudioUtils.loadMenuTheme();
        loopState = LoopState.INTRO;
        setStream(false);

        // get a line from a mixer in the system with the wanted format
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, loopAudio.af);
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        Mixer mix = null;
        for( var m : mixers )
        {
          if( !m.getName().contains(mixerName) )
            continue;
          var mixTemp = AudioSystem.getMixer(m);
          if( !mixTemp.isLineSupported(info) )
            continue;
          mix = mixTemp;
          break;
        }
        if( null == mix )
          return;

        SourceDataLine line = (SourceDataLine) mix.getLine(info);

        if( null == line )
          return;
        line.open();
        FloatControl volumeKnob = null;
        if (line.isControlSupported(FloatControl.Type.VOLUME))
          volumeKnob = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN))
          volumeKnob = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);

        line.start();
        @SuppressWarnings("unused")
        int nBytesRead = 0, nBytesWritten = 0;
        int failedReadCombo = 0; // The stream reader API arbitrarily returns -1 even when the song isn't over, so I guess we just play the odds here.
        while (!killThread)
        {
          LoopMusic newLoop = null;
          if( null == activeGame || null == activeGame.activeArmy )
            newLoop = AudioUtils.loadMenuTheme();
          else
            newLoop = AudioUtils.loadCommanderTheme(activeGame.activeArmy.cos[0]);
          if( newLoop != loopAudio )
          {
            loopAudio = newLoop;
            loopState = LoopState.INTRO;
            setStream(false);
          }

          if( null != volumeKnob )
          {
            double volume = volumeOption.getSelectedObject() / 100.0;
            double range  = volumeKnob.getMaximum() - volumeKnob.getMinimum();
            var setting   = volume * range + volumeKnob.getMinimum();
            volumeKnob.setValue((float) setting);
          }

          if( line.available() >= buffer.length ) // Avoid blocking on write calls
          {
            nBytesRead = loopStream.read(buffer, 0, buffer.length);
            if( nBytesRead != -1 )
            {
              failedReadCombo = 0;
              nBytesWritten = line.write(buffer, 0, nBytesRead);
            }
            else
              ++failedReadCombo;
          }

          if (nBytesRead == -1 && failedReadCombo > 9)
          {
            failedReadCombo = 0;
            setStream(true); // Current audio file is done, so increment
          }
        }

        line.drain();
        line.stop();
        line.close();

        // playback finished
      }
      catch (IOException | LineUnavailableException e)
      {
        // failed
        e.printStackTrace();
      }
    }

    public void join()
    {
      killThread = true;
      try
      {
        myThread.join();
      }
      catch(InterruptedException ie)
      {
        System.out.println("[SoundThread] Exception! Details:\n  " + ie.toString());
      }
    }
  }

}
