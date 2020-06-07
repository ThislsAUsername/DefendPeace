package UI.Art.Animation;

import java.awt.Color;
import java.awt.Graphics;

public class NobunagaBattleAnimation implements GameAnimation
{
  long startTime = 0;

  private long endTime = 600;
  private final int tileSize;

  int attackerX = -1;
  int attackerY = -1;
  int defenderX = -1;
  int defenderY = -1;

  public NobunagaBattleAnimation(int tileSize, int fromX, int fromY, int toX, int toY)
  {
    attackerX = fromX;
    attackerY = fromY;
    defenderX = toX;
    defenderY = toY;
    startTime = System.currentTimeMillis();
    this.tileSize = tileSize;
  }

  @Override
  public boolean animate(Graphics g)
  {
    long animTime = System.currentTimeMillis() - startTime;

    if( animTime > 500 )
    {
      // No flashes
    }
    else if( animTime > 400 )
    {
      // Flash 2 over defender
      g.setColor(Color.WHITE);
      g.fillRect(defenderX * tileSize, defenderY * tileSize, tileSize, tileSize);
    }
    else if( animTime > 300 )
    {
      // Flash 4 over attacker.
      g.setColor(Color.WHITE);
      g.fillRect(attackerX * tileSize, attackerY * tileSize, tileSize, tileSize);
    }
    else if( animTime > 200 )
    {
      // Flash 1 over defender
      g.setColor(Color.WHITE);
      g.fillRect(defenderX * tileSize, defenderY * tileSize, tileSize, tileSize);
    }
    else if( animTime > 100 )
    {
      // Flash 3 over attacker
      g.setColor(Color.WHITE);
      g.fillRect(attackerX * tileSize, attackerY * tileSize, tileSize, tileSize);
    }
    return animTime > endTime;
  }

  @Override
  public void cancel()
  {
    endTime = 0;
  }
}
