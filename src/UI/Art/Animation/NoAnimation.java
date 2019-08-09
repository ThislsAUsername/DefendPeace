package UI.Art.Animation;

import java.awt.Graphics;

public class NoAnimation implements GameAnimation
{

  public NoAnimation()
  {
  }

  @Override
  public boolean animate(Graphics g)
  {
    return true;
  }

  @Override
  public void cancel()
  {
  }
}
