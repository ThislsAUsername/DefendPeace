package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Engine.IView;
import UI.DamageChartController;
import UI.SlidingValue;
import UI.UIUtils;
import Units.UnitModel;
import Units.WeaponModel;

public class DamageChartView implements IView
{
  // Keep track of all the unit images we need to juggle.
  private BufferedImage[] shooterArray, targetArray;
  private final int unitSizePx; // Units are square
  private final int unitBuffer;
  private final int unitSpacingH;
  private final int unitSpacingV;
  private final int gridWidth;
  private final int gridHeight;

  private DamageChartController control;
  private SlidingValue viewX = new SlidingValue(0);
  private SlidingValue viewY = new SlidingValue(0);
  private SpriteCursor spriteCursor = new SpriteCursor(0, 0, SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize, UIUtils.getCOColors()[0]);

  public DamageChartView(DamageChartController control)
  {
    this.control = control;
    shooterArray = new BufferedImage[control.shooterModels.length];
    targetArray = new BufferedImage[control.targetModels.length];

    // Pull in one sprite for a sizing basis
    shooterArray[0] = SpriteLibrary.getMapUnitSpriteSet(control.shooterModels[0].name, control.shooterFac, control.shooterColor).sprites[0].getFrame(0);

    unitSizePx = shooterArray[0].getHeight(); // Units are square.
    unitBuffer = unitSizePx / 3; // Space between options in the grid.
    unitSpacingH = unitSizePx + unitBuffer;
    unitSpacingV = unitSizePx;
    gridHeight = control.shooterModels.length * unitSpacingV;
    gridWidth = control.targetModels.length * unitSpacingH - unitBuffer;
  }

  @Override
  public void render(Graphics g)
  {
    int indexShooter = control.getSelectedShooter();
    int indexTarget = control.getSelectedTarget();

    // Get the draw space
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int myWidth = dimensions.width / drawScale;
    int myHeight = dimensions.height / drawScale;

    // Don't move if we don't have to
    int startX = viewX.geti();
    int startY = viewY.geti();

    // Margins are the amount of screenspace we'd like to be between the cursor and the edge of the screen
    int marginFrac = 5;
    int marginX = myWidth/marginFrac;
    int marginY = myHeight/marginFrac;
    // virtual drawspace coordinate of the selection, relative to the top-left of the grid
    int selX = indexShooter*unitSpacingH;
    int selY = indexTarget*unitSpacingV;

    startX = findViewDestination(selX, gridWidth,  unitSizePx, unitBuffer, startX, myWidth  - unitSpacingH, marginX);
    startY = findViewDestination(selY, gridHeight, unitSizePx, unitBuffer, startY, myHeight - unitSpacingV, marginY);

    viewX.set(startX, false);
    viewY.set(startY, false);

    // Render it in true size, and then we'll scale it to window size as we draw it.
    BufferedImage image = SpriteLibrary.createTransparentSprite(myWidth, myHeight);
    Graphics myG = image.getGraphics();

    // Draw a highlight on the current cursor row/column
    myG.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    myG.fillRect(selX + unitSpacingH - viewX.geti(), 0, unitSizePx, myHeight);
    myG.fillRect(0, selY + unitSpacingV - viewY.geti(), myWidth, unitSizePx);

    // On the very top left, slap down a Q with a bullet icon and/or red X to indicate that you can toggle ammo presence
    BufferedImage tooltip = SpriteUIUtils.makeTextFrame("Q ", 3, 2);
    Graphics2D tooltipG = tooltip.createGraphics();
    BufferedImage ammoIcon = SpriteLibrary.MapIcons.AMMO.getIcon();
    final int ammoIconX = tooltip.getWidth() - ammoIcon.getWidth() + 1;
    final int ammoIconY = tooltip.getHeight() - ammoIcon.getHeight() - 1;
    tooltipG.drawImage(ammoIcon, ammoIconX, ammoIconY, null);
    if( control.outOfAmmo )
      tooltipG.drawImage(SpriteLibrary.getColoredMapTextSprites(Color.RED).get('X'), ammoIconX - 1, ammoIconY, null);
    myG.drawImage(tooltip, 2, 2, null);

    // Draw the top unit "labels"
    for(int i = 0; i < control.targetModels.length; ++i)
    {
      int yOff = 0;
      int xOff = unitSpacingH * (i+1) - viewX.geti();
      if( (xOff > myWidth ) || (xOff < unitSpacingH) )
        continue;
      drawTargetAt(myG, i, xOff, yOff);
    }

    // t = target, s = shooter
    for(int s = 0; s < control.shooterModels.length; ++s)
    {
      int yOff = unitSpacingV * (s+1) - viewY.geti();
      if( (yOff > myHeight) || (yOff < unitSpacingV) )
        continue;

      int xOff = 0;
      // Draw the side unit "label" for this row
      drawShooterAt(myG, s, xOff, yOff);

      for( int t = 0; t < control.targetModels.length; ++t)
      {
        xOff = unitSpacingH * (t+1) + unitSpacingV/2 - viewX.geti();

        // Only draw on-screen options.
        if( (xOff > myWidth ) || (xOff < unitSpacingH) )
          continue;
        if( (yOff > myHeight) || (yOff < unitSpacingV) )
          continue;

        UnitModel shooter = control.shooterModels[s];
        UnitModel target = control.targetModels[t];

        // TODO: offload this onto code that should own this
        // if we have no weapons, we can't hurt things
        if( shooter.weapons == null )
          continue;

        int maxDamage = 0;
        for( WeaponModel weapon : shooter.weapons )
        {
          if( control.outOfAmmo && !weapon.hasInfiniteAmmo )
            continue; // Can't shoot with no bullets.

          int currentDamage = (int) weapon.getDamage(target);
          if( weapon.getDamage(target) > maxDamage )
          {
            maxDamage = currentDamage;
          }

          if( maxDamage == 0 )
            continue;

          BufferedImage damageImage = SpriteUIUtils.makeTextFrame(""+maxDamage, 2, 2);
          SpriteUIUtils.drawImageCenteredOnPoint(myG, damageImage, xOff, yOff+unitSizePx/2, 1);
        }
      }
    }

    // Draw the cursors around the selected dude.
    spriteCursor.set(selX + unitSpacingH - viewX.geti(),
                     selY + unitSpacingV - viewY.geti(),
                     false);
    spriteCursor.set(control.shooterColor);
    spriteCursor.draw(myG);

    // Render our image to the screen at the properly-scaled size.
    g.drawImage(image, 0, 0, myWidth*drawScale, myHeight*drawScale, null);
  }

  public static int findViewDestination(int selectionPoint, int gridDimension,   int entrySize, int entrySpacing,
                                        int viewPoint,      int screenDimension, int margin)
  {
    int output = viewPoint;
    // If the selection is below our lower margin, move the screen in the negative direction
    if( (viewPoint + margin) > selectionPoint )
    {
      output = selectionPoint - margin;
      // ...until we hit the edge, anyway
      output = Math.max(0, output);
    }
    else
    {
      // If the selection is past our upper margin, move the screen in the positive direction
      if( (viewPoint + screenDimension - margin) < selectionPoint )
      {
        output = selectionPoint - screenDimension + margin - entrySpacing;
        // ...until we hit the edge, anyway
        output = Math.min(gridDimension - screenDimension + entrySpacing, output);
      }
      // If we've got a bunch of positive space in-frame beyond the grid,
      //   move the screen in the negative direction
      if( (viewPoint + screenDimension) > gridDimension )
      {
        output = Math.max(0, gridDimension - screenDimension + entrySpacing);
      }
    }
    return output;
  }

  private void drawShooterAt(Graphics g, int unitIndex, int xOff, int yOff)
  {
    if( null == shooterArray[unitIndex] )
      shooterArray[unitIndex] = SpriteLibrary.getMapUnitSpriteSet(control.shooterModels[unitIndex].name, control.shooterFac, control.shooterColor).sprites[0].getFrame(0);
    drawUnitAt(g, shooterArray[unitIndex], xOff, yOff, control.shooterModels[unitIndex].hidden, control.shooterColor);
  }
  private void drawTargetAt(Graphics g, int unitIndex, int xOff, int yOff)
  {
    if( null == targetArray[unitIndex] )
      targetArray[unitIndex] = SpriteLibrary.getMapUnitSpriteSet(control.targetModels[unitIndex].name, control.targetFac, control.targetColor).sprites[0].getFrame(0);
    drawUnitAt(g, targetArray[unitIndex], xOff, yOff, control.targetModels[unitIndex].hidden, control.targetColor);
  }

  private void drawUnitAt(Graphics g, BufferedImage image, int xOff, int yOff, boolean isCloaked, Color iconColor)
  {
    g.drawImage(image, xOff, yOff, null);
    if( isCloaked )
      g.drawImage(SpriteLibrary.getHideIcon(iconColor), xOff + unitSizePx / 2, yOff + unitSizePx / 2, null);
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return SpriteOptions.getScreenDimensions();
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    // Let SpriteOptions know we are changing things.
    SpriteOptions.setScreenDimensions(width, height);
  }

  @Override
  public void cleanup()
  {
  }
}
