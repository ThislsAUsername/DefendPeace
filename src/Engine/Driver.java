package Engine;

import Test.TestMain;

public class Driver
{
  private static final long serialVersionUID = 1L;

  private MainController gameController;

  public Driver()
  {
    gameController = new MainController();
  }

  public static void main(String args[])
  {
    // Run the test cases. If those all pass, launch the primary driver.
    if( !new TestMain().runTest() )
    {
      System.out.println("One or more tests failed!");
      System.exit(0);
    }

    new Driver();
  }
}
