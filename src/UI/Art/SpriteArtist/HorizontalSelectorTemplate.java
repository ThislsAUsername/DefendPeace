package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import UI.GameOption;

public class HorizontalSelectorTemplate
{
  public int graphicsOptionWidth = 0; // Set in initialize().
  public int graphicsOptionHeight = 0; // Set in initialize().
  public int horizontalSpacing = 0; // ditto
  public BufferedImage optionNamePanel = null;
  public BufferedImage optionSettingPanel = null;
  public BufferedImage optionSettingPanelChanged = null;
  public BufferedImage optionArrows = null;

  public void initialize(GameOption<?>[] allOptions)
  {
    PixelFont pf = SpriteLibrary.getFontStandard();
    int maxNameLen = 0;
    // Calculate the size of the longest option panel needed.
    for( int i = 0; i < allOptions.length; ++i )
    {
      int nameLen = pf.getWidth(allOptions[i].optionName);
      if( nameLen > maxNameLen )
      {
        maxNameLen = nameLen;
      }
    }
    int maxItemLen = 0;
    // Calculate the size of the longest item panel needed.
    for( int i = 0; i < allOptions.length; ++i )
    {
      ArrayList<?> allItems = allOptions[i].optionList;
      for( int j = 0; j < allItems.size(); ++j )
      {
        int itemLen = pf.getWidth(allItems.get(j).toString());
        if( itemLen > maxItemLen )
        {
          maxItemLen = itemLen;
        }
      }
    }

    // This panel will hold the name of the option.
    int panelHeight = pf.getAscent() + pf.getDescent(); // pf.getHeight() also seems to include line spacing.
    optionNamePanel = generateOptionPanel(maxNameLen, panelHeight, SpriteUIUtils.MENUBGCOLOR);
    // This panel will hold the current setting for the option.
    optionSettingPanel = generateOptionPanel(maxItemLen, panelHeight, SpriteUIUtils.MENUBGCOLOR);
    optionSettingPanelChanged = generateOptionPanel(maxItemLen, panelHeight, SpriteUIUtils.MENUHIGHLIGHTCOLOR);
    int itemWidth = optionSettingPanel.getWidth()+ pf.emSizePx * 2; // dual purpose buffer, also used for the switching arrows

    graphicsOptionWidth = optionNamePanel.getWidth() + itemWidth + pf.emSizePx; // Plus some space for a buffer between panels.
    graphicsOptionHeight = optionNamePanel.getHeight();
    horizontalSpacing = 3 * pf.emSizePx;

    int imageDimension = itemWidth + SpriteArrows.ARROW_SIZE;
    optionArrows = new BufferedImage(imageDimension, 10, BufferedImage.TYPE_INT_ARGB);
    SpriteArrows.draw(optionArrows.getGraphics(), optionArrows.getWidth()/2, optionArrows.getHeight()/2, itemWidth, false, SpriteUIUtils.MENUFRAMECOLOR);
  }

  /**
   * Build an image for a floating panel to hold the specified text length, and return it.
   * @param widthPx The width of the string to be housed, in pixels.
   * @param heightPx The height of the font.
   */
  private BufferedImage generateOptionPanel(int widthPx, int heightPx, Color fgColor)
  {
    PixelFont pf = SpriteLibrary.getFontStandard();
    int textBuffer = pf.getDescent();
    int w = (2 * textBuffer) + widthPx;
    int h = (textBuffer) + heightPx;
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
    PixelFont pf = SpriteLibrary.getFontStandard();
    int drawBuffer = pf.getDescent(); // Use the same buffer above and below the main text mass.

    // Draw the name panel and the name.
    g.drawImage(optionNamePanel, x, y, optionNamePanel.getWidth(), optionNamePanel.getHeight(), null);

    // These debug boxes are being left here commented-out for future use by popular request.
//    g.setColor(Color.GREEN);
//    g.drawRect(x+drawBuffer, y+drawBuffer, pf.emSizePx, pf.getHeight());
//    g.setColor(Color.RED);
//    g.drawRect(x+drawBuffer, y+drawBuffer, pf.getWidth('H'), pf.getAscent());
//    g.setColor(Color.YELLOW);
//    g.drawRect(x+drawBuffer, y+drawBuffer+pf.getAscent(), pf.getWidth('H')*2, pf.getDescent());
//    g.setColor(Color.BLACK);

    SpriteUIUtils.drawText(g, opt.optionName, x + drawBuffer, y+drawBuffer);
    // Draw the setting panel and the setting value.
    x = x + optionNamePanel.getWidth() + horizontalSpacing;
    BufferedImage settingPanel = (opt.isChanged()) ? optionSettingPanelChanged : optionSettingPanel;
    g.drawImage(settingPanel, x, y, settingPanel.getWidth(), settingPanel.getHeight(), null);
    SpriteUIUtils.drawText(g, opt.getCurrentValueText(), x + drawBuffer, y+drawBuffer);
  }
}
