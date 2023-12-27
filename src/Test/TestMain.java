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
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestVisionMechanics().runTest(), "Vision mechanics test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestTransport().runTest(), "Transport test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCombat().runTest(), "Combat test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCombatMods().runTest(), "Combat modification test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestHealing().runTest(), "Healing test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestSaveLoad().runTest(), "Save/load test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestSprite().runTest(), "Sprite test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestStateTracker().runTest(), "StateTracker test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCountTracker().runTest(), "CountManager test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestInputHandler().runTest(), "InputHandler test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCapture().runTest(), "Capture test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestGameEvent().runTest(), "GameEvent test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestTeleport().runTest(), "Teleport test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCOModifier().runTest(), "COModifier test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCommanderAve().runTest(), "CommanderAve test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCommanderBrenner().runTest(), "Brenner test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestCommanderTech().runTest(), "CommanderTech test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestAIBehavior().runTest(), "AI behavior test failed!");
    if( !testsPassed ) return testsPassed;
    testsPassed &= validate(new TestAIConstraints().runTest(), "AI constraint test failed!");
    System.out.println("All tests completed.");
    return testsPassed;
  }
}
