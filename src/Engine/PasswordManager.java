package Engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class PasswordManager
{
  private static final String PASSFILE_NAME = Engine.Driver.JAR_DIR + "res/passfile";

  /**
   * Create a new passfile if there isn't one yet, and then associate the passfile with `cmdr`.
   */
  public static void setPass(Army army)
  {
    String storedPass = readPassfile();
    // Generate a new salt/passfile and set cmdr's password.
    long salt = new Random().nextLong();
    if( storedPass.isEmpty() )
    {
      System.out.println("Generating new password");
      storedPass = UuidGenerator.randomUuid().toString();
      writePassfile(storedPass);
    }
    System.out.println("Setting password.");
    army.setPassword(salt, storedPass);
  }

  /**
   * Evaluate the current army against the stored passfile.
   * @return true if the passfile matches, false if it doesn't.
   */
  public static boolean validateAccess(Army army)
  {
    if( !army.hasPassword() ) return true; // No password, no problem.

    String storedPass = readPassfile();
    boolean matches = army.checkPassword(storedPass);
    return matches;
  }

  private static void writePassfile(String pass)
  {
    try
    {
      File keyFile = new File(PASSFILE_NAME);
      FileWriter writer = new FileWriter(keyFile, false);

      writer.write(pass);
      writer.close();
    }
    catch( IOException ioe )
    {
      System.out.println("Error! Failed to save passfile!.\n  " + ioe.toString());
    }
  }

  private static String readPassfile()
  {
    String pass = "";

    // Load keys file if it exists
    File keyFile = new File(PASSFILE_NAME);
    if( !keyFile.exists() )
      return pass;

    try
    {
      Scanner scanner = new Scanner(keyFile);
      while(scanner.hasNextLine())
      {
        Scanner linescan = new Scanner(scanner.nextLine());
        pass = linescan.next();
        linescan.close();
      }
      scanner.close();
    }
    catch(FileNotFoundException fnfe)
    {
      System.out.println("Somehow we failed to find the passfile after checking that it exists!");
    }
    catch( InputMismatchException ime )
    {
      System.out.println("Encountered an error while parsing passfile!");
    }

    return pass;
  }
}
