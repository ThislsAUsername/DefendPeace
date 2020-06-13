package UI.Art.Animation;

import java.awt.Color;
import java.awt.Graphics;

import UI.Art.SpriteArtist.UnitSpriteSet;
import Units.Unit;

public class NobunagaBattleAnimation extends BaseUnitAnimation
{
  int attackerX = -1;
  int attackerY = -1;
  int defenderX = -1;
  int defenderY = -1;

  public NobunagaBattleAnimation(int tileSize, Unit actor, int fromX, int fromY, int toX, int toY)
  {
    super(tileSize, actor, null);
    attackerX = fromX;
    attackerY = fromY;
    defenderX = toX;
    defenderY = toY;
    duration = 600;
  }

  @Override
  public boolean animate(Graphics g)
  {
    long animTime = System.currentTimeMillis() - startTime;

    // Draw the attacker in position.
    drawUnit(g, actor, UnitSpriteSet.AnimState.IDLE, attackerX, attackerY );

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
    return animTime > duration;
  }
}
