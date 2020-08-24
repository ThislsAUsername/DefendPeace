package Engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import CommandingOfficers.Commander;

public class PasswordManager
{
  private static final String PASSFILE_NAME = "res/passfile";

  /**
   * If the passed-in commander has no password set, then combine the local passfile
   * with the Commander's salt and set its password. If no passfile exists, create it first.
   *
   * If `cmdr` already has a password assigned, confirm that the passfile corresponds to the
   * stored password.
   *
   * @param cmdr The Commander who's turn we want to verify.
   * @return true if a password is generated or matches, false if the passfile and stored password mismatch.
   */
  public static boolean validateAccess(Commander cmdr)
  {
    boolean granted = false;
    String storedPass = readPassfile();
    if( !cmdr.hasPassword() )
    {
      // Generate a new salt/passfile and set cmdr's password.
      long salt = new Random().nextLong();
      if( storedPass.isEmpty() )
      {
        System.out.println("Generating new password");
        storedPass = UUID.randomUUID().toString();
        writePassfile(storedPass);
      }
      System.out.println("Setting password.");
      cmdr.setPassword(salt, storedPass);
      granted = true;
    }
    else
    {
      System.out.println("Reading password");
      granted = cmdr.checkPassword(storedPass);
      System.out.println("password matches? " + granted);
    }
    return granted;
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
    if( keyFile.exists() )
    {
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
    } // ~if file exists

    return pass;
  }
}
