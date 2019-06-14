package UI;


/**
 * Represents an XY-coordinate that slides quickly (but not instantly) into place when its location is set.
 * Allows users to see both the actual XY coordinate (if it did move instantly) as well as the intermediate
 * location that would be used for animating.
 */
public class SlidingValue
{
  private static final long UPDATE_DELAY_MS = 16; // Minimum time to update the intermediate position.
  private static final double UPDATE_FACTOR = 0.7; // Move (1-UPDATE_FACTOR)*distance on each update delay.
  private static final double SNAP_DISTANCE = 0.05;
  private int targetValue;
  private double currentValue;
  private long lastTime;

  public SlidingValue(int val)
  {
    targetValue = val;
    currentValue = val;
    lastTime = System.currentTimeMillis();
  }

  /** Set a target/destination for this SlidingPoint. */
  public void set(int val)
  {
    if( val != targetValue )
    {
      targetValue = val;
      lastTime = System.currentTimeMillis();
    }
  }

  /** Set a target/destination for this SlidingPoint. If `snap` is true, go instantly to val. */
  public void set(int val, boolean snap)
  {
    if(snap) snap(val);
    else set(val);
  }

  /** Set a target/destination for this SlidingPoint and move it there instantly. */
  public void snap(int val)
  {
    targetValue = val;
    currentValue = val;
    lastTime = System.currentTimeMillis();
  }

  /** Retrieve the "current" location of the point as it slides to its destination. */
  public double get()
  {
    // If we are already basically there, just snap to.
    if( Math.abs(targetValue - currentValue) < SNAP_DISTANCE )
    {
      currentValue = targetValue;
    }
    else // Otherwise, figure out how much closer to get and move in.
    {
      double slide = calculateSlideAmount();
      currentValue += slide;
    }
    return currentValue;
  }

  /** Get the "current" value, rounded to the nearest int. **/
  public int geti()
  {
    return (int)(0.5 + get());
  }

  /** Retrieve the current destination (the last set() value) of this sliding point. */
  public int getDestination()
  {
    return targetValue;
  }

  private double calculateSlideAmount()
  {
    long time = System.currentTimeMillis();

    // Figure out how many UPDATE_DELAY_MS intervals have passed.
    int power = 0;
    for(long temp = time; temp > lastTime; ++power, temp -= UPDATE_DELAY_MS);
    lastTime = time;

    double dist = targetValue - currentValue;
    double slide = dist - (dist * Math.pow(UPDATE_FACTOR, power));
    return slide;
  }
}
