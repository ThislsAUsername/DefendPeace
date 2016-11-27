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

}
