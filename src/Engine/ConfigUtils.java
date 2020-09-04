package Engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import UI.GameOption;

public class ConfigUtils
{
  public static String keyFromOption(GameOption<?> option)
  {
    return option.optionName.replace(" ", "");
  }

  public static boolean writeConfigs(String filename, List<GameOption<?>> options)
  {
    List<Pair<String, Integer>> optAsList =
        options.stream().map(x ->
                         Pair.from(keyFromOption(x), x.getSelectionNormalized()))
                             .collect(Collectors.toList());
    return writeConfigItems(filename, optAsList);
  }

  /** Writes out the objects to a file, using their indices as keys and invoking their toString() methods. */
  public static boolean writeConfigStrings(String filename, Object[] options)
  {
    List<Pair<Integer, String>> optPairs = new ArrayList<Pair<Integer,String>>();

    for( int i = 0; i < options.length; ++i )
      optPairs.add(Pair.from(i, options[i].toString()));

    return writeConfigItems(filename, optPairs);
  }

  public static <K extends Serializable, V extends Serializable>
  boolean writeConfigItems(String filename, Map<K, V> options)
  {
    List<Pair<K, V>> optAsList =
        options.entrySet().stream().map(x ->
                         Pair.from(x.getKey(), x.getValue()))
                             .collect(Collectors.toList());
    return writeConfigItems(filename, optAsList);
  }

  /**
   * Writes a series of key/value pairs to a file in a standard ASCII format
   * @param options The objects to be toString()'d
   * @return True if there were no I/O errors
   */
  public static <K extends Serializable, V extends Serializable>
  boolean writeConfigItems(String filename, List<Pair<K, V>> options)
  {
    boolean success = false;
    try
    {
      File configFile = new File(filename);
      // Ensure the directory exists for us to write to
      new File(configFile.getParent()).mkdirs();
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
   * Reads a series of string/int pairs from a file written in a standard ASCII format
   * @param optionsToPopulate Where to put the keys and values found
   * @return True if there were no errors in parsing
   */
  public static boolean readConfigs(String filename, Iterable<GameOption<?>> optionsToPopulate)
  {
    HashMap<String, Integer> optionMap = new HashMap<String, Integer>();
    boolean allValid =
        readConfigLists(filename,
                       (String s)->s,
                       (Scanner linescan)->linescan.nextInt(),
                        optionMap);

    for( GameOption<?> option : optionsToPopulate )
    {
      String key = keyFromOption(option);
      if( optionMap.containsKey(key) )
        option.setSelectedOption(optionMap.get(key));
      else
        allValid = false;
    }
    return allValid;
  }

  /**
   * Reads a series of key/value pairs from a file written in a standard ASCII format, with caller-defined reading semantics
   * @param keyFinder Turns the key (a single string token as defined by the Scanner class) into the destination type
   * @param valFinder Turns the rest of the line into the destination type
   * @param optionsToPopulate Where to put the keys and values found
   * @return True if there were no errors in parsing
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
