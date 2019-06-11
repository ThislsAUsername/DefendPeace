package UI;


/**
 * Represents an XY-coordinate that slides quickly (but not instantly) into place when its location is set.
 * Allows users to see both the actual XY coordinate (if it did move instantly) as well as the intermediate
 * location that would be used for animating.
 */
public class SlidingValue
{
  private static final long UPDATE_DELAY_MS = 16; // Minimum time to update the intermediate position.
  private int actualValue;
  private double visualValue;
  private long lastTime;
  private long unusedTime;

  public SlidingValue(int val)
  {
    actualValue = val;
    visualValue = val;
    lastTime = System.currentTimeMillis();
  }

  /** Set a target/destination for this SlidingPoint. */
  public void set(int val)
  {
    if( val != actualValue )
    {
      actualValue = val;
      lastTime = System.currentTimeMillis();
    }
  }

  /** Set a target/destination for this SlidingPoint and move it there instantly. */
  public void snap(int val)
  {
    actualValue = val;
    visualValue = val;
    lastTime = System.currentTimeMillis();
  }

  /** Retrieve the "current" location of the point as it slides to its destination. */
  public double get()
  {
    if( visualValue != actualValue )
    {
      double slide = calculateSlideAmount(visualValue, actualValue);
      visualValue += slide;
    }
    return visualValue;
  }

  /** Retrieve the current destination (the last set() value) of this sliding point. */
  public int getDestination()
  {
    return actualValue;
  }

  /**
   * Calculate the distance to move so the point slides quickly into place instead of
   * just snapping instantly. Distance moved per frame is proportional to distance from goal location.
   * NOTE: This is calibrated for 60fps, and changing the frame rate will change the closure speed.
   */
  private double calculateSlideAmount(double currentNum, int targetNum)
  {
    long time = System.currentTimeMillis();
    long td = time - lastTime + unusedTime;
    lastTime = time;

    double slide = 0;
    while( td > UPDATE_DELAY_MS )
    {
      td -= UPDATE_DELAY_MS;
      double animMoveFraction = 0.3; // Movement amount per cycle.
      double animSnapDistance = 0.05; // Minimum distance at which point we just snap into place.
      double diff = Math.abs(targetNum - currentNum);
      int sign = (targetNum > currentNum) ? 1 : -1; // Since we took abs(), make sure we can correct the sign.
      if( diff < animSnapDistance )
      { // If we are close enough, just move the exact distance.
        slide = diff * sign;
      }
      else
      { // Move a fixed fraction of the remaining distance.
        slide += diff * animMoveFraction * sign;
      }
    }
    unusedTime = td;

    return slide;
  }
}
