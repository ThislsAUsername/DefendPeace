package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;

import UI.SlidingValue;
import UI.UIUtils;

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
  private static final long ALT_RARITY = 8;

  public SpriteCursor()
  {
    this(0, 0, 0, 0, UIUtils.getCOColors()[0]);
  }

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
    draw(g, xPos.geti(), yPos.geti(), width.geti(), height.geti(), color);
  }

  private static boolean[] getSpriteOffsets()
  {
    long currentTime = System.currentTimeMillis();
    long td = currentTime % ANIM_CYCLE_TIME_MS;
    int phase = 0;
    boolean altCycle = currentTime % (ANIM_CYCLE_TIME_MS*ALT_RARITY) < ANIM_CYCLE_TIME_MS;
    for( ; td > TIME_PER_CYCLE_MS; td -= TIME_PER_CYCLE_MS, phase++ );

    boolean[] offs = {false, false, false, false};
    if( phase == 0 && !altCycle )
    {
      // Every so often, bump in a bit.
      offs[0] = offs[1] = offs[2] = offs[3] = true;
    }
    else if( altCycle )
    {
      // Alternate animation cycle. More rarely, bump in each corner one at a time.
      offs[0] = (phase == 0); // top-left
      offs[1] = (phase == 1); // top-right
      offs[2] = (phase == 2); // bot-right
      offs[3] = (phase == 3); // bot-left
    }
    return offs;
  }

  public static void draw(Graphics g, int x, int y, int w, int h, Color color)
  {
    // Draw the arrows around the focused player attribute.
    Sprite cursor = SpriteLibrary.getCursorSprites(color);
    boolean[] offs = getSpriteOffsets();
    if( offs[0] )
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(0), x+1, y+1);
    else
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(0), x, y);

    if( offs[1] )
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(1), x+w-1, y+1);
    else
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(1), x+w, y);

    if( offs[2] )
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(2), x+w-1, y+h-1);
    else
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(2), x+w, y+h);

    if( offs[3] )
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(3), x+1, y+h-1);
    else
      SpriteUIUtils.drawImageCenteredOnPoint(g, cursor.getFrame(3), x, y+h);
  }
}
