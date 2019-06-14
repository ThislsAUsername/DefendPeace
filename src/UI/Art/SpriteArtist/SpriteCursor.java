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
    xPos.snap(y);
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

  public void draw(Graphics g)
  {
    Sprite cursor = SpriteLibrary.getCursorSprites(color);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(0), xPos.geti(), yPos.geti(), 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(1), xPos.geti()+width.geti(), yPos.geti(), 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(2), xPos.geti()+width.geti(), yPos.geti()+height.geti(), 1);
    SpriteLibrary.drawImageCenteredOnPoint(g, cursor.getFrame(3), xPos.geti(), yPos.geti()+height.geti(), 1);
  }
}
