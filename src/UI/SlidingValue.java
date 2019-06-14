package UI;


/**
 * Represents an XY-coordinate that slides quickly (but not instantly) into place when its location is set.
 * Allows users to see both the actual XY coordinate (if it did move instantly) as well as the intermediate
 * location that would be used for animating.
 */
public class SlidingValue
{
  private static final long UPDATE_DELAY_MS = 16; // Minimum time to update the intermediate position.
  private static double UPDATE_SCALE_FACTOR = 0.7; // What fraction of the difference we'll take up per UPDATE_DELAY
  private int startValue, endValue;
  private long startTime;

  public SlidingValue(int val)
  {
    endValue = val;
    startValue = val;
    startTime = System.currentTimeMillis();
  }

  /** Set a target/destination for this SlidingPoint. */
  public void set(int val)
  {
    if( val != endValue )
    {
      startValue = (int) get();
      endValue = val;
      startTime = System.currentTimeMillis();
    }
  }

  /** Set a target/destination for this SlidingPoint and move it there instantly. */
  public void snap(int val)
  {
    endValue = val;
    startValue = val;
    startTime = System.currentTimeMillis();
  }

  /** Retrieve the "current" location of the point as it slides to its destination. */
  public double get()
  {
    long td = System.currentTimeMillis() - startTime;
    return endValue - (endValue - startValue) * Math.pow(UPDATE_SCALE_FACTOR, td / UPDATE_DELAY_MS);
  }

  /** Retrieve the current destination (the last set() value) of this sliding point. */
  public int getDestination()
  {
    return endValue;
  }
}
