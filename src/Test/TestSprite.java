package Test;

import java.awt.image.BufferedImage;

import UI.Art.SpriteArtist.Sprite;
import UI.Art.SpriteArtist.SpriteLibrary;

public class TestSprite extends TestCase
{
  @Override
  public boolean runTest()
  {
    boolean testPassed = true;
    // Make sure we can create a new Sprite in the way we expect.
    testPassed &= validate(testConstructors(), " Sprite constructor problems found.");
    // Make sure Sprite handles normalizing otherwise-invalid indices correctly.
    testPassed &= validate(testIndexNormalization(), " Sprite is not normalizing indices correctly");
    return testPassed;
  }

  private boolean testConstructors()
  {
    boolean testPassed = true;

    // Create an image from which to create a Sprite.
    BufferedImage image = new BufferedImage(24, 4, BufferedImage.TYPE_INT_ARGB);

    // First constructor: construct a Sprite using image as a single frame.
    Sprite spr1 = new Sprite(image);

    // Make sure the new Sprite has one frame.
    testPassed &= validate(spr1.numFrames() == 1, "  Sprite has " + spr1.numFrames() + " frames instead of 1.");

    // Add a frame, and make sure it now has two.
    spr1.addFrame(image);
    testPassed &= validate(spr1.numFrames() == 2, "  Sprite has " + spr1.numFrames() + " frames instead of 2.");

    // Second constructor: construct a Sprite using the image as a sprite sheet, then 
    //   verify that the Sprite correctly parsed into 24/4 = 6 frames
    spr1 = new Sprite(image, 4, 4);
    testPassed &= validate(spr1.numFrames() == 6, "  Sprite has " + spr1.numFrames() + " frames instead of 6.");
    testPassed &= validate(spr1.getFrame(0).getWidth() == 4, "  Sprite has the wrong width.");
    testPassed &= validate(spr1.getFrame(0).getHeight() == 4, "  Sprite has the wrong height.");

    // Create a new Sprite with images of width 5, and ensure there are 24/5 = 4 frames.
    spr1 = new Sprite(image, 5, 4);
    testPassed &= validate(spr1.numFrames() == 4, "  Sprite has " + spr1.numFrames() + " frames instead of 4.");
    testPassed &= validate(spr1.getFrame(0).getWidth() == 5, "  Sprite has an incorrect width.");
    testPassed &= validate(spr1.getFrame(0).getHeight() == 4, "  Sprite has an incorrect height.");

    // Create a Sprite using a height that is taller than the sprite sheet. Make sure we get a default image of the prescribed size.
    Sprite spr2 = new Sprite(image, 4, 5);
    testPassed &= validate(spr2.numFrames() == 1, "  Sprite has " + spr2.numFrames() + " frames instead of the expected 1.");
    testPassed &= validate(spr2.getFrame(0).getWidth() == 4, "  Sprite 2 has the wrong width.");
    testPassed &= validate(spr2.getFrame(0).getHeight() == 5, "  Sprite 2 has the wrong height.");

    // Test Sprite copy-constructor. Validate dimensions and number of frames.
    spr2 = new Sprite(spr1);
    testPassed &= validate(spr2.numFrames() == 4, "  Sprite has " + spr2.numFrames() + " frames instead of the expected 4.");
    testPassed &= validate(spr2.getFrame(0).getWidth() == 5, "  Sprite 2 has an incorrect width.");
    testPassed &= validate(spr2.getFrame(0).getHeight() == 4, "  Sprite 2 has an incorrect height.");

    // Test construction by passing in null BufferedImage. Sprite should generate a default-sized image instead.
    spr1 = new Sprite((BufferedImage) null);
    testPassed &= validate(spr1.numFrames() == 1, "  Sprite has " + spr1.numFrames() + " frames; it should have 1.");
    testPassed &= validate(spr1.getFrame(0).getWidth() == SpriteLibrary.baseSpriteSize, "  Image has incorrect width.");
    testPassed &= validate(spr1.getFrame(0).getHeight() == SpriteLibrary.baseSpriteSize, "  Image has incorrect height.");

    // Test construction by passing in null Sprite.
    spr1 = new Sprite((Sprite) null);
    testPassed &= validate(spr1.numFrames() == 1, "  Sprite has " + spr1.numFrames() + " frames, when it should have 1.");
    testPassed &= validate(spr1.getFrame(0).getWidth() == SpriteLibrary.baseSpriteSize, "  Sprite has incorrect width.");
    testPassed &= validate(spr1.getFrame(0).getHeight() == SpriteLibrary.baseSpriteSize, "  Sprite has incorrect height.");

    // Test construction by passing in null.
    spr1 = new Sprite(null, 3, 3);
    testPassed &= validate(spr1.numFrames() == 1, "  Sprite has " + spr1.numFrames() + " frames; it should have 1.");
    testPassed &= validate(spr1.getFrame(0).getWidth() == 3, "  Sprite should have width 3.");
    testPassed &= validate(spr1.getFrame(0).getHeight() == 3, "  Sprite 2 should have height 3.");

    return testPassed;
  }

  private boolean testIndexNormalization()
  {
    boolean testPassed = true;

    // Make a few BufferedImages to use as frames in a Sprite.
    BufferedImage frame0 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    BufferedImage frame1 = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
    BufferedImage frame2 = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
    BufferedImage[] frames = {frame0, frame1, frame2};

    // Sprite doesn't require all frames to be the same size, so we can use size to tell them apart.
    Sprite spr = new Sprite(frame0);
    spr.addFrame(frame1);
    spr.addFrame(frame2);
    int numFrames = spr.numFrames();

    int index = 0;
    int realIndex = 0;

    try
    {
      for(; index < 100; ++index, ++realIndex)
      {
        if(realIndex > numFrames-1) realIndex -= numFrames; // Make sure this is always a valid index for our array.
        BufferedImage test = spr.getFrame(index); // Use the increasingly large index, relying on Sprite to correctly wrap.
        // Make sure the Sprite we get back has the dimensions we expect.
        testPassed &= validate( test.getWidth() == frames[realIndex].getWidth(), new StringBuffer("  Wrong frame for index ").append(index).
            append(". Expected frame ").append(realIndex).append(". (Expected width of ").append(frames[realIndex].getWidth()).
            append(", but width of retrieved frame is ").append(test.getWidth()).toString());
      }
    }
    catch( ArrayIndexOutOfBoundsException ex )
    {
      testPassed &= validate(false, "  Sprite is not normalizing large indices correctly.");
    }
    // Perform the test again, but going negative this time.
    try
    {
      index = realIndex = 0;
      for(; index > -100; --index, --realIndex)
      {
        if(realIndex < 0) realIndex += numFrames; // Make sure this is always a valid index for our array.
        BufferedImage test = spr.getFrame(index); // Use the increasingly negative index, relying on Sprite to correctly wrap.
        // Make sure the Sprite we get back is what we expect.
        testPassed &= validate( test.getWidth() == frames[realIndex].getWidth(), new StringBuffer("  Wrong frame for index ").append(index).
            append(". Expected frame ").append(realIndex).append(". (Expected width of ").append(frames[realIndex].getWidth()).
            append(", but width of retrieved frame is ").append(test.getWidth()).toString());
      }
    }
    catch( ArrayIndexOutOfBoundsException ex )
    {
      testPassed &= validate(false, "  Sprite is not normalizing small indices correctly.");
    }

    return testPassed;
  }
}
