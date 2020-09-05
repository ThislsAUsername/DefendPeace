package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import UI.GameOption;

public class HorizontalSelectorTemplate
{
  public int textBuffer = 4;
  private static int letterWidth = SpriteLibrary.getLettersUppercase().getFrame(0).getWidth();

  public int graphicsOptionWidth = 0; // Set in initialize().
  public int graphicsOptionHeight = 0; // Set in initialize().
  public BufferedImage optionNamePanel = null;
  public BufferedImage optionSettingPanel = null;
  public BufferedImage optionSettingPanelChanged = null;
  public BufferedImage optionArrows = null;

  public void initialize(GameOption<?>[] allOptions)
  {
    int maxNameLen = 0;
    // Calculate the size of the longest option panel needed.
    for( int i = 0; i < allOptions.length; ++i )
    {
      if( allOptions[i].optionName.length() > maxNameLen )
      {
        maxNameLen = allOptions[i].optionName.length();
      }
    }
    int maxItemLen = 0;
    // Calculate the size of the longest item panel needed.
    for( int i = 0; i < allOptions.length; ++i )
    {
      ArrayList<?> allItems = allOptions[i].optionList;
      for( int j = 0; j < allItems.size(); ++j )
      {
        if( allItems.get(j).toString().length() > maxItemLen )
        {
          maxItemLen = allItems.get(j).toString().length();
        }
      }
    }

    // This panel will hold the name of the option.
    optionNamePanel = generateOptionPanel(maxNameLen, SpriteUIUtils.MENUBGCOLOR);
    // This panel will hold the current setting for the option.
    optionSettingPanel = generateOptionPanel(maxItemLen, SpriteUIUtils.MENUBGCOLOR);
    optionSettingPanelChanged = generateOptionPanel(maxItemLen, SpriteUIUtils.MENUHIGHLIGHTCOLOR);
    int itemWidth = optionSettingPanel.getWidth()+ letterWidth * 2; // dual purpose buffer, also used for the switching arrows

    graphicsOptionWidth = optionNamePanel.getWidth() + itemWidth + letterWidth; // Plus some space for a buffer between panels.
    graphicsOptionHeight = optionNamePanel.getHeight();

    // Make points to define the two selection arrows.
    int[] lXPoints = { 0, 5, 5, 0 };
    int[] lYPoints = { 4, -1, 11, 5 };
    int[] rXPoints = { itemWidth, itemWidth+5, itemWidth+5, itemWidth };
    int[] rYPoints = { -1, 4, 5, 10 };
    // Build an image with the selection arrows.
    optionArrows = new BufferedImage(itemWidth+7, 10, BufferedImage.TYPE_INT_ARGB);
    Graphics ag = optionArrows.getGraphics();
    ag.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    ag.fillPolygon(lXPoints, lYPoints, lXPoints.length);
    ag.fillPolygon(rXPoints, rYPoints, rXPoints.length);
  }

  /**
   * Build an image for a floating panel to hold the specified text length, and return it.
   * @param length The max text length intended to be shown on this panel.
   */
  private BufferedImage generateOptionPanel(int length, Color fgColor)
  {
    int w = (2 * textBuffer) + (letterWidth * length);
    int h = (textBuffer) + (SpriteLibrary.getLettersUppercase().getFrame(0).getHeight());
    int sh = 3; // Extra vertical space to fit in the shadow effect.
    int sw = 2;

    BufferedImage panel = new BufferedImage(w + sw, h + sh, BufferedImage.TYPE_INT_ARGB);

    Graphics g = panel.getGraphics();

    // Draw the shadow.
    g.setColor(SpriteUIUtils.MENUFRAMECOLOR);
    g.fillRect(sw, sh, w, h);

    // Draw the writing surface.
    g.setColor(fgColor);
    g.fillRect(0, 0, w, h);

    return panel;
  }

  public void drawGameOption(Graphics g, int x, int y, GameOption<?> opt)
  {
    int drawBuffer = textBuffer;

    // Draw the name panel and the name.
    g.drawImage(optionNamePanel, x, y, optionNamePanel.getWidth(), optionNamePanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.optionName, x + drawBuffer, y + drawBuffer);

    // Draw the setting panel and the setting value.
    x = x + (optionNamePanel.getWidth() + (3 * letterWidth));
    BufferedImage settingPanel = (opt.isChanged()) ? optionSettingPanelChanged : optionSettingPanel;
    g.drawImage(settingPanel, x, y, settingPanel.getWidth(), settingPanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.getCurrentValueText(), x + drawBuffer, y + drawBuffer);
  }
}
