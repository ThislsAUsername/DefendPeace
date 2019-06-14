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
  private static double ANIM_SNAP_DISTANCE = 0.5; // Minimum distance, at which point we just snap into place.
  private int startValue, targetValue;
  private long startTime;
  private boolean done;

  public SlidingValue(int val)
  {
    targetValue = val;
    startValue = val;
    done = false;
    startTime = System.currentTimeMillis();
  }

  /** Set a target/destination for this SlidingPoint. */
  public void set(int val)
  {
    if( val != targetValue )
    {
      startValue = (int) get();
      targetValue = val;
      done = false;
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
    done = true;
    startTime = System.currentTimeMillis();
  }

  /** Retrieve the "current" location of the point as it slides to its destination. */
  public double get()
  {
    if (done)
      return targetValue;
    long td = System.currentTimeMillis() - startTime;
    double diff = (targetValue - startValue) * Math.pow(UPDATE_SCALE_FACTOR, td / UPDATE_DELAY_MS);
    if (Math.abs(diff) <= ANIM_SNAP_DISTANCE)
      done = true;
    return targetValue - diff;
  }

  /** Get the "current" location of the point, rounded to the nearest int. **/
  public int geti()
  {
    return (int)(0.5 + get());
  }

  /** Retrieve the current destination (the last set() value) of this sliding point. */
  public int getDestination()
  {
    return targetValue;
  }
}
