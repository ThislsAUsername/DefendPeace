package Test;

import Engine.StateTrackers.CountManager;

public class TestCountTracker extends TestCase
{
  @Override
  public boolean runTest()
  {
    boolean testPassed = true;
    testPassed &= validate(testNull(), " CountManager null test failed");
    testPassed &= validate(testStress(), " CountManager stress test failed");
    return testPassed;
  }

  private boolean testNull()
  {
    boolean testPassed = true;

    CountManager<Integer, Double> ct = new CountManager<>();

    try
    {
      ct.resetCountFor(null);
      // Run these operations a few times to check for statefulness
      for( int i = 0; i < 3; ++i )
      {
        ct.hasCountFor(null);
        ct.getCountFor(null);
        ct.getCountFor(null, null);
        ct.getCountFor(0, null);
        ct.getCountFor(null, 0.0);
        ct.incrementCount(null, null);
        ct.incrementCount(0, null);
        ct.incrementCount(null, 0.0);
      }
      ct.resetCountFor(null);
    }
    catch (Exception ex)
    {
      testPassed &= validate(false, "  CountManager barfed on null with " + ex);
    }

    return testPassed;
  }

  private boolean testStress()
  {
    boolean testPassed = true;

    CountManager<Integer, Double> ct = new CountManager<>();

    for(int i = -10; i < 10; ++i)
    {
      testPassed &= validate(!ct.hasCountFor(i), "  CountManager has count entries for root index "+i+" for no good reason.");
      testPassed &= validate(0 == ct.getCountFor(i, 0.0), "  CountManager has count != 0 for index "+i+"-0.0 when it shouldn't.");
      testPassed &= validate(!ct.hasCountFor(i), "  CountManager has count entries for root index "+i+" after being asked for a count.");
      testPassed &= validate(0 == ct.getCountFor(i).size(), "  CountManager has count entries for root index "+i+" when it shouldn't.");
      testPassed &= validate(ct.hasCountFor(i), "  CountManager doesn't have a count entry for root index "+i+" after one was requested.");
      for(double j = 0; j < 10; ++j)
      {
        for(int k = 0; k < 100; ++k)
        {
          testPassed &= validate(k == ct.getCountFor(i, j), "  CountManager reported the wrong count for index "+i+"-"+j+"; "+ct.getCountFor(i, j)+" when it should be "+k);
          ct.incrementCount(i, j);
        }
      }
    }

    return testPassed;
  }
}
