package UI;


/**
 * Represents an XY-coordinate that slides quickly (but not instantly) into place when its location is set.
 * Allows users to see both the actual XY coordinate (if it did move instantly) as well as the intermediate
 * location that would be used for animating.
 * NOTE: It is currently expected for get() to be called at 60 frames per second for smooth animation.
 */
public class SlidingValue
{
  private int actualValue;
  private double visualValue;

  public SlidingValue(int val)
  {
    actualValue = val;
    visualValue = val;
  }

  /** Set a target/destination for this SlidingPoint. */
  public void set(int val)
  {
    actualValue = val;
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

  /** Retrieve the current destination ("real" position) of this sliding point. */
  public int getActual()
  {
    return actualValue;
  }

  /**
   * Calculate the distance to move so the point slides quickly into place instead of
   * just snapping instantly. Distance moved per frame is proportional to distance from goal location.
   * NOTE: This is calibrated for 60fps, and changing the frame rate will change the closure speed.
   */
  private static double calculateSlideAmount(double currentNum, int targetNum)
  {
    double animMoveFraction = 0.3; // Movement cap to prevent over-speedy menu movement.
    double animSnapDistance = 0.05; // Minimum distance at which point we just snap into place.
    double slide = 0; // Return value; the distance we actually are going to move.
    double diff = Math.abs(targetNum - currentNum);
    int sign = (targetNum > currentNum) ? 1 : -1; // Since we took abs(), make sure we can correct the sign.
    if( diff < animSnapDistance )
    { // If we are close enough, just move the exact distance.
      slide = diff;
    }
    else
    { // Move a fixed fraction of the remaining distance.
      slide = diff * animMoveFraction;
    }

    return slide * sign;
  }
}
