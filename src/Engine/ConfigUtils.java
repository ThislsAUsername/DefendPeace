package Engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import UI.GameOption;

public class ConfigUtils
{
  public static <T> boolean writeConfigs(String filename, List<GameOption<T>> options)
  {
    List<Pair<String, String>> optAsList =
        options.stream().map(x ->
                         Pair.from(x.optionName, x.getSelectedObject().toString()))
                             .collect(Collectors.toList());
    return writeConfigItems(filename, optAsList);
  }

  /**
   * Writes a series of key/value pairs to a file in a standard ASCII format
   * @param options The objects to be toString()'d
   * @return Whether there were any I/O errors
   */
  public static <K extends Serializable, V extends Serializable>
  boolean writeConfigItems(String filename, List<Pair<K, V>> options)
  {
    boolean success = false;
    try
    {
      File configFile = new File(filename);
      FileWriter writer = new FileWriter(configFile, false);
      for( Pair<K, V> op: options )
      {
        writer.write(op.key.toString());
        writer.write(" " + op.val);
        writer.write('\n');
      }
      writer.close();
      success = true;
    }
    catch( IOException ioe )
    {
      System.out.println("Error! Failed to save "+filename+"!.\n" + ioe.toString());
    }
    return success;
  }

  /**
   * Reads a series of key/value pairs from a file written in a standard ASCII format, with caller-defined reading semantics
   * @param keyFinder Turns the key (a single string token as defined by the Scanner class) into the destination type
   * @param valFinder Turns the rest of the line into the destination type
   * @param optionsToPopulate Where to put the keys and values found
   * @return Whether there were any errors during parsing
   */
  public static <K,V> boolean readConfigLists(String filename,
                                              Function<String, K> keyFinder,
                                              Function<Scanner, V> valFinder,
                                              Map<K, V> optionsToPopulate)
  {
    File configFile = new File(filename);
    if( !configFile.exists() )
      return false;

    boolean allValid = true;
    try
    {
      Scanner scanner = new Scanner(configFile);
      while (scanner.hasNextLine())
      {
        String strKey = "KEY_FAILED";
        try
        {
          Scanner linescan = new Scanner(scanner.nextLine());
          strKey = linescan.next();
          K actionType = keyFinder.apply(strKey);
          optionsToPopulate.put(actionType, valFinder.apply(linescan));
          linescan.close();
        }
        catch (Exception exc)
        {
          allValid = false;
          System.out.println("Error! Failure when reading key " + strKey + " from save " + filename + "!.\n" + exc.toString());
        }
      }
      scanner.close();
    }
    catch (FileNotFoundException fnfe)
    {
      allValid = false;
      System.out.println("Somehow we failed to find "+filename+" after checking that it exists!");
    }
    catch (InputMismatchException ime)
    {
      allValid = false;
      System.out.println("Encountered an error while parsing "+filename+"!");
    }

    return allValid;
  }
}
