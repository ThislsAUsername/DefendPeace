package UI.Art.SpriteArtist;

public class SpriteUIUtils
{
  /**
   * Calculate the distance to move an image make it look like it slides quickly into place instead of
   * just snapping at each button-press. Distance moved per frame is proportional to distance from goal location.
   * It is expected that currentNum and targetNum will not differ by more than 1.0.
   * NOTE: currentNum and targetNum correspond to (relative) positions, not to pixels.
   * NOTE: This is calibrated for 60fps, and changing the frame rate will change the closure speed.
   * 
   */
  public static double calculateSlideAmount(double currentNum, int targetNum)
  {
    double animMoveFraction = 0.3; // Movement cap to prevent over-speedy menu movement.
    double animSnapDistance = 0.05; // Minimum distance at which point we just snap into place.
    double slide = 0; // Return value; the distance we actually are going to move.
    double diff = Math.abs(targetNum - currentNum);
    int sign = (targetNum > currentNum)?1:-1; // Since we took abs(), make sure we can correct the sign.
    if( diff < animSnapDistance )
    { // If we are close enough, just move the exact distance.
      slide = diff;
    }
    else
    { // Move a fixed fraction of the remaining distance.
      slide = diff * animMoveFraction;
    }
    
    return slide*sign;
  }
}
