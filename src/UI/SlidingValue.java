package UI;


/**
 * Represents an XY-coordinate that slides quickly (but not instantly) into place when its location is set.
 * Allows users to see both the actual XY coordinate (if it did move instantly) as well as the intermediate
 * location that would be used for animating.
 */
public class SlidingValue
{
  private static final long UPDATE_DELAY_MS = 16; // Minimum time to update the intermediate position.
  private static final double SLIDE_FACTOR = 0.7; // Move (1-UPDATE_FACTOR)*distance on each update delay.
  private static final double SNAP_DISTANCE = 0.05;
  private int targetValue;
  private double startValue;
  private long startTime;

  public SlidingValue(int val)
  {
    targetValue = val;
    startValue = val;
    startTime = System.currentTimeMillis();
  }

  /** Set a target/destination for this SlidingPoint. */
  public void set(int val)
  {
    if( val != targetValue )
    {
      startValue = get();
      targetValue = val;
      startTime = System.currentTimeMillis();
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
    startValue = val;
    startTime = System.currentTimeMillis();
  }

  /** Retrieve the "current" location of the point as it slides to its destination. */
  public double get()
  {
    if( targetValue == startValue) // If we are on target, return.
      return targetValue;

    // Otherwise, figure out how far we should be from targetValue given the elapsed time.
    long td = System.currentTimeMillis() - startTime;
    double diff = (targetValue - startValue) * Math.pow(SLIDE_FACTOR, td/UPDATE_DELAY_MS);

    // Once we get close enough, just snap into place.
    if( Math.abs(diff) <= SNAP_DISTANCE )
      startValue = targetValue;

    return targetValue - diff;
  }

  /** Get the "current" value, rounded to the nearest int. **/
  public int geti()
  {
    return (int)Math.round(get());
  }

  /** Retrieve the current destination (the last set() value) of this sliding point. */
  public int getDestination()
  {
    return targetValue;
  }
}
