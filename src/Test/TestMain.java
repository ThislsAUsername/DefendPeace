package Test;

/**
 * Provides a framework for automated regression-testing of game functionality.
 */
public class TestMain extends TestCase
{
  public static void main(String[] args)
  {
    // Run the test cases and print the results.
    if( !new TestMain().runTest() )
    {
      System.out.println("One or more tests failed!");
    }
    else
    {
      System.out.println("All tests passed!");
    }
  }

  @Override
  public boolean runTest()
  {
    boolean testsPassed = true;
    testsPassed &= validate(new TestUnitMovement().runTest(), "Unit movement test failed!");
    testsPassed &= validate(new TestVisionMechanics().runTest(), "Vision mechanics test failed!");
    testsPassed &= validate(new TestTransport().runTest(), "Transport test failed!");
    testsPassed &= validate(new TestCombat().runTest(), "Combat test failed!");
    testsPassed &= validate(new TestCombatMods().runTest(), "Combat modification test failed!");
    testsPassed &= validate(new TestHealing().runTest(), "Healing test failed!");
    testsPassed &= validate(new TestSprite().runTest(), "Sprite test failed!");
    testsPassed &= validate(new TestInputHandler().runTest(), "InputHandler test failed!");
    testsPassed &= validate(new TestCapture().runTest(), "Capture test failed!");
    testsPassed &= validate(new TestGameEvent().runTest(), "GameEvent test failed!");
    testsPassed &= validate(new TestCOModifier().runTest(), "COModifier test failed!");
    testsPassed &= validate(new TestCommanderAve().runTest(), "CommanderAve test failed!");
    System.out.println("All tests completed.");
    return testsPassed;
  }
}
