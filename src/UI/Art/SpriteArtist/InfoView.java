package UI.Art.SpriteArtist;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.CommanderInfo.InfoPage.PageType;
import Engine.Army;
import Engine.GameEvents.GameEvent;
import Terrain.MapPerspective;
import UI.COStateInfo;
import UI.InfoController;
import UI.MapView;
import UI.Art.SpriteArtist.Backgrounds.DiagonalBlindsBG;

/**
 * InfoView paints the CO's full portrait and a box with info in it.
 * The contents of the info are defined by inputs; current game status or static CO info are both used
 */
public class InfoView extends MapView // Extend MapView for getDrawableMap(). We don't actually draw it, but we need fog info.
{
  private static final int PANE_OUTER_BUFFER = 3; // sets both outer border and frame border size

  private InfoController myControl;
  final int lineHeight = SpriteLibrary.getFontStandard().getHeight() + 1;

  private ArrayList<InfoPage> prevPages = null;
  private BufferedImage pageImage = null;
  private SpriteArrows hArrows = new SpriteArrows();
  private SpriteArrows vArrows = new SpriteArrows(true);

  public InfoView(InfoController control)
  {
    myControl = control;
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return SpriteOptions.getScreenDimensions();
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    prevPages = null; // Have to redraw if we've been resized
    // Let SpriteOptions know we are changing things.
    SpriteOptions.setScreenDimensions(width, height);
  }

  @Override
  public void render(Graphics g)
  {
    DiagonalBlindsBG.draw(g);

    // Get the draw space. We'll draw it all in real size and then scale it when we draw to the window.
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int imageWidth = dimensions.width / drawScale;
    int imageHeight = dimensions.height / drawScale;
    if (1 > imageHeight || 1 > imageWidth)
      return;

    BufferedImage image = SpriteLibrary.createTransparentSprite(imageWidth, imageHeight);
    Graphics myG = image.getGraphics();

    int paneHSize = (int) (imageWidth*0.7) - PANE_OUTER_BUFFER*4; // width of the BG color area
    int paneVSize = (int) (imageHeight   ) - PANE_OUTER_BUFFER*4; // height ""

    // Get the current menu selections.
    Army thisArmy = myControl.getSelectedArmy();
    ArrayList<InfoPage> pages = myControl.getSelectedPages();

    // Draw the relevant Commanders
    final int bodyShiftPerBody = 42;
    final ArrayList<CommanderInfo> coInfos = myControl.getSelectedCOInfoList();
    int currentBodyShift = -1 * bodyShiftPerBody * (coInfos.size() - 1);
    for( int i = coInfos.size() - 1; i >= 0; --i )
    {
      CommanderInfo coi = coInfos.get(i);
      BufferedImage COPic = SpriteLibrary.getCommanderSprites(coi).body;
      // justify bottom/right, but don't cut off the top
      final int bodyVOffset = Math.max(0, imageHeight - COPic.getHeight());
      myG.drawImage(COPic, imageWidth - COPic.getWidth() - currentBodyShift, bodyVOffset, null);
      currentBodyShift += bodyShiftPerBody;
    }

    if (PANE_OUTER_BUFFER > paneHSize || PANE_OUTER_BUFFER > paneVSize)
      return; // Continue no further if we don't have room to actually draw

    if (null != thisArmy)
    {
      MapPerspective drawableMap = getDrawableMap(myControl.getGame());
      boolean fogOn = myControl.getGame().isFogEnabled();
      boolean hideFunds = fogOn && thisArmy.isEnemy(drawableMap.viewer);
      CommanderOverlayArtist.drawCommanderOverlay(myG, hideFunds, thisArmy, false);
    }
    
    // add the actual info
    myG.setColor(SpriteUIUtils.MENUFRAMECOLOR); // outer buffer
    myG.fillRect(  PANE_OUTER_BUFFER,   PANE_OUTER_BUFFER, paneHSize + 2*PANE_OUTER_BUFFER, paneVSize + 2*PANE_OUTER_BUFFER);
    myG.setColor(SpriteUIUtils.MENUBGCOLOR);    // inside of the pane
    myG.fillRect(2*PANE_OUTER_BUFFER, 2*PANE_OUTER_BUFFER, paneHSize                      , paneVSize                      );

    final int drawingWidth  = paneHSize - PANE_OUTER_BUFFER*2;
    final int drawingHeight = paneVSize - PANE_OUTER_BUFFER*2;
    if( pages != prevPages || pages.get(0).pageType == PageType.CO_HEADERS )
      pageImage = renderPage(pages, drawingWidth);
    prevPages = pages;

    final int shiftDown = myControl.getShiftDown() * lineHeight; // User-input shift value
    final int possibleShift = Math.max(0, pageImage.getHeight() - paneVSize); // The maximum shift that can make sense for this page
    final boolean canShift = possibleShift > 0;
    int pixelShift = 0; // Actual pixels to offset the drawn image
    if( canShift )
    {
      // Since shiftDown can be negative, wrap it into range, make sure it's positive, then wrap it again
      pixelShift = (possibleShift + shiftDown % possibleShift) % possibleShift;
      pixelShift -= pixelShift % lineHeight;
    }

    final int contentWidth  = Math.min(Math.abs(drawingWidth ), pageImage.getWidth());
    final int contentHeight = Math.min(Math.abs(drawingHeight), pageImage.getHeight());
    // Draw the cropped page into our pane
    myG.drawImage(pageImage.getSubimage(0, pixelShift, contentWidth, contentHeight), 3 * PANE_OUTER_BUFFER, 3 * PANE_OUTER_BUFFER, null);

    int horizontalArrowVOffset = 2 + 3*PANE_OUTER_BUFFER;
    if( canShift ) // Throw some up/down scrolly arrows on, if relevant
    {
      vArrows.set(paneHSize, drawingHeight/2 + 3*PANE_OUTER_BUFFER, drawingHeight, true);
      vArrows.draw(myG);
      horizontalArrowVOffset += 5;
    }
    if( myControl.getPageListCount() > 1 ) // Add left/right scrolly arrows, if relevant
    {
      hArrows.set(paneHSize, horizontalArrowVOffset, 6, true);
      hArrows.draw(myG);
    }

    // Finally, draw our rendered image onto the window.
    g.drawImage(image, 0, 0, imageWidth*drawScale, imageHeight*drawScale, null);
  }

  private BufferedImage renderPage(ArrayList<InfoPage> pages, int drawingWidth)
  {
    ArrayList<BufferedImage> pageImages = new ArrayList<BufferedImage>();
    MapPerspective drawableMap = getDrawableMap(myControl.getGame());
    for( InfoPage p : pages )
    {
      switch (p.pageType)
      {
        case CO_HEADERS: // Draw all CO image headers and brief status text for each CO
        {
          Army[] armyList = myControl.getGame().armies;

          PixelFont font = SpriteLibrary.getFontStandard();
          final int yCOSize = CommanderOverlayArtist.getCommanderOverlayHeight();
          final int yStatusSpace = font.getHeight() + 6; // Put in some extra for the status text
          int ySpaceNeeded = 0;

          for(Army a : armyList) {
            ySpaceNeeded += yCOSize * a.cos.length;
            ySpaceNeeded += yStatusSpace;
          }

          BufferedImage drawableArea =
              CommanderOverlayArtist.drawAllCommanderOverlays(armyList, drawableMap, drawingWidth, ySpaceNeeded, null);

          pageImages.add(drawableArea);
        }
          break;
        case GAME_STATUS: // Draw detailed status text for only this specific CO
        {
          Army thisArmy = myControl.getSelectedArmy();
          COStateInfo coStatus = new COStateInfo(drawableMap, thisArmy);
          BufferedImage[] statusHalves = new BufferedImage[2];
          // Draw assuming we don't have to wrap the text
          statusHalves[0] = SpriteUIUtils.drawProse(coStatus.getFullStatusLabels());
          statusHalves[1] = SpriteUIUtils.drawProse(coStatus.getFullStatusValues());
          BufferedImage statusText = SpriteUIUtils.joinBufferedImages(statusHalves, 3);

          pageImages.add(statusText);
        }
          break;
        case BASIC:
          pageImages.add(SpriteUIUtils.drawProseToWidth(p.info, drawingWidth));
          break;
      }
    }

    return SpriteUIUtils.stackBufferedImages(pageImages.toArray(new BufferedImage[0]), lineHeight + 1);
  }

  // Vestigial method stubs from MapView
  @Override
  public boolean shouldAnimate(GameEvent toAnimate)
  {
    return false;
  }
  @Override
  public void animate(GameEvent newEvents)
  {
  }
  @Override
  public void cleanup()
  {
  }
}
