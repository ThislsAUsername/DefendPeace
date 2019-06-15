package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;

import UI.SlidingValue;

public class SpriteCursor
{
  private SlidingValue xPos;
  private SlidingValue yPos;
  private SlidingValue width;
  private SlidingValue height;
  private Color color;

  // Variables to control the cursor animations
  private static final long ANIM_CYCLE_TIME_MS = 800;
  private static final long NUM_CYCLE_PHASES = 4;
  private static final long TIME_PER_CYCLE_MS = ANIM_CYCLE_TIME_MS / NUM_CYCLE_PHASES;

  public SpriteCursor(int x, int y, int w, int h, Color c)
  {
    xPos = new SlidingValue(x);
    yPos = new SlidingValue(y);
    width = new SlidingValue(w);
    height = new SlidingValue(h);
    color = c;
  }

  public void set(Color c)
  {
    color = c;
  }

  public void set(int x, int y)
  {
    xPos.set(x);
    yPos.set(y);
  }

  public void snap(int x, int y)
  {
    xPos.snap(x);
    yPos.snap(y);
  }

  public void set(int x, int y, boolean snap)
  {
    if(snap) snap(x, y);
    else set(x, y);
  }

  public void set(int x, int y, int w, int h)
  {
    xPos.set(x);
    yPos.set(y);
    width.set(w);
    height.set(h);
  }

  public void snap(int x, int y, int w, int h)
  {
    xPos.snap(x);
    yPos.snap(y);
    width.snap(w);
    height.snap(h);
  }

  public void set(int x, int y, int w, int h, boolean snap)
  {
    if(snap) snap(x, y, w, h);
    else set(x, y, w, h);
  }

  public void draw(Graphics g)
  {
    Sprite cursor = SpriteLibrary.getCursorSprites(color);
    int[] offs = getSpriteOffsets();
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(0), xPos.geti()+offs[0], yPos.geti()+offs[1], 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(1), xPos.geti()+width.geti()+offs[2], yPos.geti()+offs[1], 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(2), xPos.geti()+width.geti()+offs[2], yPos.geti()+height.geti()+offs[3], 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(3), xPos.geti()+offs[0], yPos.geti()+height.geti()+offs[3], 1);
  }

  private static int[] getSpriteOffsets()
  {
    long td = System.currentTimeMillis() % ANIM_CYCLE_TIME_MS;
    int phase = 0;
    for( ; td > TIME_PER_CYCLE_MS; td -= TIME_PER_CYCLE_MS, phase++ );

    int[] offs = {0, 0, 0, 0};
    switch(phase)
    {
      case 0:
      case 1:
      case 2:
        // Mostly we hang out in standard position.
        break;
      case 3:
        // Every so often, bump in a bit.
        offs[0] = 1;  // x
        offs[1] = 1;  // y
        offs[2] = -1; // w
        offs[3] = -1; // h
        break;
        default:
          System.out.println("Unknown animation phase in Cursor!");
    }
    return offs;
  }

  public static void draw(Graphics g, int x, int y, int w, int h, Color color)
  {
    // Draw the arrows around the focused player attribute.
    Sprite cursor = SpriteLibrary.getCursorSprites(color);
    int[] offs = getSpriteOffsets();
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(0), x+offs[0], y+offs[1], 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(1), x+w+offs[2], y+offs[1], 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(2), x+w+offs[2], y+h+offs[3], 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(3), x+offs[0], y+h+offs[3], 1);
  }
}
