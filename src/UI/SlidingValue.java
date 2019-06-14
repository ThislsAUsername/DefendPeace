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
  private int startValue, endValue;
  private long startTime;
  private boolean done;

  public SlidingValue(int val)
  {
    endValue = val;
    startValue = val;
    done = false;
    startTime = System.currentTimeMillis();
  }

  /** Set a target/destination for this SlidingPoint. */
  public void set(int val)
  {
    if( val != endValue )
    {
      startValue = (int) get();
      endValue = val;
      done = false;
      startTime = System.currentTimeMillis();
    }
  }

  /** Set a target/destination for this SlidingPoint and move it there instantly. */
  public void snap(int val)
  {
    endValue = val;
    startValue = val;
    done = true;
    startTime = System.currentTimeMillis();
  }

  /** Retrieve the "current" location of the point as it slides to its destination. */
  public double get()
  {
    if (done)
      return endValue;
    long td = System.currentTimeMillis() - startTime;
    double diff = (endValue - startValue) * Math.pow(UPDATE_SCALE_FACTOR, td / UPDATE_DELAY_MS);
    if (Math.abs(diff) <= ANIM_SNAP_DISTANCE)
      done = true;
    return endValue - diff;
  }

  /** Retrieve the current destination (the last set() value) of this sliding point. */
  public int getDestination()
  {
    return endValue;
  }
}
