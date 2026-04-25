package UI;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import Engine.ConfigUtils;
import Engine.OptionSelector;
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
    try
    {
      AudioInputStream in = AudioSystem.getAudioInputStream(new File(Engine.Driver.JAR_DIR + "res/music/t-hachi.ogg"));
      AudioFormat baseFormat = in.getFormat();
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
    }
    catch (UnsupportedAudioFileException | IOException e)
    {
      // failed
      e.printStackTrace();
    }
    soundDeviceOption.reset(soundDeviceOption.optionList.size()); // Allow selecting the new options

    // Load saved settings from disk, if they exist.
    loadSettingsFromDisk();

    if( !soundDeviceOption.getSelectedObject().equals("None") )
      soundThread = new SoundThread(soundDeviceOption.getSelectedObject());
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

    public SoundThread(String pMixerName)
    {
      myThread = new Thread(this);
      myThread.start();
      mixerName = pMixerName;
    }

    @Override
    public void run()
    {
      try // credit: https://github.com/Trilarion/java-vorbis-support/blob/master/README.md
      {
        AudioInputStream in = AudioSystem.getAudioInputStream(new File(Engine.Driver.JAR_DIR + "res/music/t-hachi.ogg"));
        if( null == in )
          return;

        AudioFormat baseFormat = in.getFormat();

        AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
            baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

        AudioInputStream dataIn = AudioSystem.getAudioInputStream(targetFormat, in);

        byte[] buffer = new byte[4096];

        // get a line from a mixer in the system with the wanted format
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat);
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
        while (nBytesRead != -1 && !killThread)
        {
          if( null != volumeKnob )
          {
            double volume = Math.pow(volumeOption.getSelectedObject() / 100.0, 2); // Square the ratio for finer adjustments at lower volume.
            double range  = volumeKnob.getMaximum() - volumeKnob.getMinimum();
            var setting   = volume * range + volumeKnob.getMinimum();
            volumeKnob.setValue((float) setting);
          }
          if( line.available() >= buffer.length ) // Avoid blocking on write calls
          {
            nBytesRead = dataIn.read(buffer, 0, buffer.length);
            if( nBytesRead != -1 )
            {
              nBytesWritten = line.write(buffer, 0, nBytesRead);
            }
          }
        }

        line.drain();
        line.stop();
        line.close();

        dataIn.close();

        in.close();
        // playback finished
      }
      catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
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
        System.out.println("[InfantrySpritePreloader] Exception! Details:\n  " + ie.toString());
      }
    }
  }

}
