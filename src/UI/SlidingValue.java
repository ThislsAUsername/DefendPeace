package UI;


/**
 * Represents an XY-coordinate that slides quickly (but not instantly) into place when its location is set.
 * Allows users to see both the actual XY coordinate (if it did move instantly) as well as the intermediate
 * location that would be used for animating.
 */
public class SlidingValue
{
  private static final long UPDATE_DELAY_MS = 16; // Minimum time to update the intermediate position.
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
    double snapDistance = 0.05;
    if( Math.abs(targetValue - currentValue) < snapDistance )
    {
      currentValue = targetValue;
    }

    // Otherwise, figure out how much closer to get and move in.
    if( currentValue != targetValue )
    {
      double slide = calculateSlideAmount();
      currentValue += slide;
    }
    return currentValue;
  }

  /** Retrieve the current destination (the last set() value) of this sliding point. */
  public int getDestination()
  {
    return targetValue;
  }

  private double calculateSlideAmount()
  {
    double moveFraction = 0.3;

    long time = System.currentTimeMillis();

    // Figure out how many UPDATE_DELAY_MS intervals have passed.
    int power = 0;
    for(long temp = time; temp > lastTime; ++power, temp -= UPDATE_DELAY_MS);
    lastTime = time;

    double dist = targetValue - currentValue;
    double slide = dist - (dist * Math.pow(1-moveFraction, power));
    return slide;
  }
}
