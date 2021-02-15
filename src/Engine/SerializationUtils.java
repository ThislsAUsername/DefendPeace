package Engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationUtils
{
  public static String getSaveWarnings(String filename)
  {
    System.out.println(String.format("Checking compatibility of save %s", filename));

    try (FileInputStream file = new FileInputStream(filename); ObjectInputStream in = new ObjectInputStream(file);)
    {
      return GameInstance.getSaveWarnings(in);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    return "!?";
  }

  public static GameInstance loadSave(String filename)
  {
    System.out.println(String.format("Deserializing game data from %s", filename));

    GameInstance load = null;
    try (FileInputStream file = new FileInputStream(filename); ObjectInputStream in = new ObjectInputStream(file);)
    {
      in.readObject(); // Pull out and discard our version info

      load = (GameInstance) in.readObject();
    }
    catch (Exception ex)
    {
      System.out.println(ex.toString());
    }

    return load;
  }

  public static String writeSave(GameInstance game, boolean endCurrentTurn)
  {
    String filename = "save/" + game.saveFile;
    new File("save/").mkdirs(); // make sure we don't freak out if the directory's not there

    System.out.println(String.format("Now saving to %s", filename));
    try (FileOutputStream file = new FileOutputStream(filename); ObjectOutputStream out = new ObjectOutputStream(file);)
    {
      game.writeSave(out, endCurrentTurn);
    }
    catch (IOException ex)
    {
      System.out.println(ex.toString());
    }

    return filename;
  }
}
