package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import CommandingOfficers.CommanderInfo;
import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;
import UI.PlayerSetupCommanderController;
import UI.SlidingValue;
import UI.UIUtils;
import UI.UIUtils.COSpriteSpec;
import UI.UIUtils.SourceGames;

public class PlayerSetupCommanderArtist
{
  private static HashMap<Integer, CommanderPanel> coPanels = new HashMap<Integer, CommanderPanel>();

  private static PlayerSetupCommanderController myControl;
  private static SlidingValue panelOffsetY = new SlidingValue(0);
  private static int panelDrawW = CommanderPanel.eyesWidth + 2;
  private static SlidingValue panelDrawX = new SlidingValue(0);

  private static SlidingValue tagPickerOffsetX = new SlidingValue(0);

  static final BufferedImage kickTip = SpriteUIUtils.makeTextFrame("Q: Kick CO", 3, 2);
  static final BufferedImage toGameTip = SpriteUIUtils.makeTextFrame("Q: To Game", 3, 2);
  static final BufferedImage toArmyTip = SpriteUIUtils.makeTextFrame("Q: To Army", 3, 2);
  static final BufferedImage etip = SpriteUIUtils.makeTextFrame("E: CO Info", 3, 2);

  public static void draw(Graphics g, IController controller, ArrayList<CommanderInfo> infos, Color playerColor)
  {
    PlayerSetupCommanderController control = (PlayerSetupCommanderController)controller;
    if( null == control )
    {
      System.out.println("WARNING! PlayerSetupCommanderController was given the wrong controller!");
    }
    boolean snapCursor = myControl != control;

    myControl = control;
    if( snapCursor )
      initFromControl();

    // Define the draw space
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int myWidth = dimensions.width / drawScale;
    int myHeight = dimensions.height / drawScale;
    BufferedImage image = SpriteLibrary.createTransparentSprite(myWidth, myHeight);
    Graphics myG = image.getGraphics();

    /////////////// Commander Panels //////////////////////
    drawCmdrPickerPanels(myG, myHeight, myWidth, infos, playerColor, snapCursor);

    /////////////// Tag Picker Panels //////////////////////
    if( amPickingTagIndex )
    {
      drawTagPickerPanels(myG, myWidth, infos, playerColor, snapCursor);
    }

    /////////////// Tooltip ////////////////////////////
    myG.drawImage(etip, myWidth - etip.getWidth(), 3, null);
    BufferedImage qtip = kickTip;
    if( !amPickingTagIndex )
      if( sortByGameThenFaction )
        qtip = toGameTip;
      else
        qtip = toArmyTip;
    myG.drawImage(qtip, myWidth - qtip.getWidth(), 5 + qtip.getHeight(), null);

    // Draw the composed image to the window at scale.
    g.drawImage(image, 0, 0, myWidth*drawScale, myHeight*drawScale, null);
  }

  static final int leftMargin = 13;
  public static void drawCmdrPickerPanels(
                       Graphics myG, int myHeight, int myWidth,
                       ArrayList<CommanderInfo> infos, Color playerColor,
                       boolean snapCursor)
  {
    // Selected horizontal bin on the screen
    int binIndex        = cmdrBinSelector.getSelectionNormalized();
    // Index into that bin that's selected
    int coIndex         = cmdrInBinSelector.getSelectionNormalized();
    // Value of that selection; index into the list of CO infos
    int highlightedCmdr = cmdrBins.get(binIndex).get(coIndex);
    // Calculate the vertical space each bin panel will consume.
    int panelBuffer = 3;
    int panelHeight = CommanderPanel.PANEL_HEIGHT+panelBuffer + 1;
    int panelShift  = textToastHeight + panelHeight + panelBuffer;

    // We're drawing the panels to align with the vertically-fixed cursor,
    // so figure out where the zeroth bin panel should be drawn.
    panelOffsetY.set(binIndex*panelShift, snapCursor);
    int drawY = myHeight / 2 - panelOffsetY.geti() - panelHeight + panelBuffer + 1;
    final int startY = drawY;

    // Selected CO's name for drawing later
    String coNameText = "";

    int binToDraw = 0;
    // X offset to start drawing CO faces from
    int baseDrawX = leftMargin + SpriteLibrary.getCursorSprites().getFrame(0).getWidth(); // Make sure we have room to draw the cursor around the frame.

    for(; drawY - CommanderPanel.PANEL_HEIGHT/2 < myHeight
        && binToDraw < cmdrBins.size();
        ++binToDraw )
    {
      int indexInBin = 0;
      int drawX = baseDrawX;

      // Draw the bin panel to go behind the COs
      final COSpriteSpec spriteSpec = binColorSpec.get(binToDraw);
      Color[] palette = UIUtils.defaultMapColors;
      String canonName = "MISC";
      if( Color.LIGHT_GRAY != spriteSpec.color )
      {
        palette = UIUtils.getMapUnitColors(spriteSpec.color).paletteColors;
        if( sortByGameThenFaction )
          canonName = UIUtils.getCanonicalFactionName(spriteSpec);
        else // Our inner category is games, so just pull the hardcoded faction name
          canonName = spriteSpec.faction.name;
      }
      int currentPanelBottomY = drawCmdrBin(myG, canonName, palette[5], palette[3], myWidth, drawY, panelHeight);

      // Actually draw the CO mugs
      ArrayList<Integer> currentBin = cmdrBins.get(binToDraw);
      while (drawX < myWidth && indexInBin < currentBin.size())
      {
        int coToDraw = currentBin.get(indexInBin);
        CommanderInfo coInfo = infos.get(coToDraw);
        Integer key = coToDraw;

        // Get the relevant PlayerPanel.
        if( !coPanels.containsKey(key) )
          coPanels.put(key, new CommanderPanel(coInfo, coInfo.baseFaction.color));
        CommanderPanel panel = coPanels.get(key);

        // Update the PlayerPanel and render it to an image.
        BufferedImage playerImage = panel.update(coInfo, coInfo.baseFaction.color);

        int drawCmdrY = drawY + textToastHeight + txtBuf;
        myG.drawImage(playerImage, drawX, drawCmdrY, null);

        // Set the cursor width.
        if( highlightedCmdr == coToDraw )
        {
          panelDrawX.set(drawX, snapCursor);
          coNameText = coInfo.name;
        }

        ++indexInBin;
        drawX += playerImage.getWidth() + panelBuffer;
      }

      drawY = currentPanelBottomY + panelBuffer;
    }

    // Draw the outer level's side panel
    final COSpriteSpec outerSpriteSpec = outerBinColorSpec.get(outerCategorySelector.getSelectionNormalized());
    Color[] outerPalette = UIUtils.defaultMapColors;
    String outerName = "MISC";
    if( Color.LIGHT_GRAY != outerSpriteSpec.color )
    {
      outerPalette = UIUtils.getMapUnitColors(outerSpriteSpec.color).paletteColors;
      if( !sortByGameThenFaction )
        outerName = UIUtils.getCanonicalFactionName(outerSpriteSpec);
      else // Our inner category is games, so just pull the hardcoded faction name
        outerName = outerSpriteSpec.faction.name;
    }
    drawSideBin(myG, outerName, outerPalette[5], outerPalette[3], startY, drawY-startY-panelBuffer);

    final int cursorY = myHeight / 2 - CommanderPanel.PANEL_HEIGHT / 2;

    // Draw stuff for the selected option.
    if( !amPickingTagIndex )
    {
      BufferedImage coNameFrame = SpriteUIUtils.makeTextFrame(coNameText, 2, 2);
      int drawNameX = panelDrawX.geti() + panelDrawW / 2;
      int drawNameY = cursorY + CommanderPanel.PANEL_HEIGHT + coNameFrame.getHeight() / 2 + 2;
      SpriteUIUtils.drawImageCenteredOnPoint(myG, coNameFrame, drawNameX, drawNameY);

      SpriteCursor.draw(myG, panelDrawX.geti(), cursorY, panelDrawW, CommanderPanel.PANEL_HEIGHT, playerColor);
    }
  }

  static final int textWidth = SpriteLibrary.getLettersSmallCaps().getFrame(0).getWidth();
  static final int textHeight = SpriteLibrary.getLettersSmallCaps().getFrame(0).getHeight();
  static final int txtBuf = 2;
  static final int textToastHeight = textHeight + txtBuf; // upper buffer only
  public static int drawCmdrBin(Graphics g, String label, Color bg,  Color frame, int screenWidth, int y, int bodyHeight)
  {
    int textToastWidth  = textWidth*label.length();
    int drawX = leftMargin + txtBuf;

    // Smooths between the label backing to the CO face holder
    Polygon triangle = new Polygon();
    triangle.addPoint(drawX+textToastWidth                , y);                 // top left
    triangle.addPoint(drawX+textToastWidth                , y+textToastHeight); // bottom left
    triangle.addPoint(drawX+textToastWidth+textToastHeight, y+textToastHeight); // right

    g.setColor(frame);
    g.fillPolygon(triangle);
    g.fillRect(leftMargin, y                 , txtBuf+textToastWidth , bodyHeight); // behind text
    g.fillRect(leftMargin, y+textToastHeight , screenWidth           , bodyHeight); // main body

    g.setColor(bg);
    for( int i = 0; i < 3; ++i )
      triangle.ypoints[i] += 1; // Shift one pixel down to expose the frame
    g.fillPolygon(triangle);
    g.fillRect(leftMargin, y+1                , txtBuf+textToastWidth, bodyHeight-2);
    g.fillRect(leftMargin, y+1+textToastHeight, screenWidth          , bodyHeight-2);

    SpriteUIUtils.drawTextSmallCaps(g, label, drawX, y + txtBuf);

    return y + textToastHeight + bodyHeight;
  }
  /**
   * Draws a little side bin with beveled edges
   *  /
   * |A
   * |W
   * |1
   *  \
   */
  public static int drawSideBin(Graphics g, String label, Color bg,  Color frame, int y, int totalHeight)
  {
    //                    *      - 2 triangles
    int bodyHeight = totalHeight - 2*(leftMargin) + 2;
    final int drawBodyY = y+leftMargin-1;
    // Throw in a +/-adjustment because triangles are weird

    Polygon triangleUp = new Polygon();
    triangleUp.addPoint(leftMargin, y-1);       // top right
    triangleUp.addPoint(0         , drawBodyY); // bottom left
    triangleUp.addPoint(leftMargin, drawBodyY); // bottom right
    Polygon triangleDown = new Polygon();
    triangleDown.addPoint(leftMargin, y+totalHeight-leftMargin); // top right
    triangleDown.addPoint(0         , y+totalHeight-leftMargin); // top left
    triangleDown.addPoint(leftMargin, y+totalHeight);            // bottom right

    g.setColor(frame);
    g.fillPolygon(triangleUp);
    g.fillPolygon(triangleDown);

    g.setColor(bg);
    for( int i = 0; i < 3; ++i )
    {
      triangleUp.ypoints[i]   += 1; // Shift down to expose the frame
      triangleDown.ypoints[i] -= 1; // ditto, up
    }
    g.fillPolygon(triangleUp);
    g.fillPolygon(triangleDown);

    // Draw the rectangle afterwards because the triangles exist only to bring me pain
    g.setColor(frame);
    g.fillRect(0, drawBodyY, leftMargin, bodyHeight);
    g.setColor(bg);
    g.fillRect(1, drawBodyY, leftMargin-1, bodyHeight);

    BufferedImage textScratch = SpriteUIUtils.getTextAsImage(label, true);
    BufferedImage text = SpriteUIUtils.rotateCounterClockwise90(textScratch);
    g.drawImage(text, leftMargin-textHeight-txtBuf, drawBodyY + (bodyHeight-text.getHeight())/2, null);

    return drawBodyY + bodyHeight;
  }

  public static void drawTagPickerPanels(
                       Graphics myG, int myWidth,
                       ArrayList<CommanderInfo> infos, Color playerColor,
                       boolean snapCursor)
  {
    // Calculate the vertical space each player panel will consume.
    final int panelThickness = 1;
    final int panelBuffer = 2*panelThickness;
    final int panelWidth = CommanderPanel.eyesWidth+panelBuffer;
    final int panelHeight = CommanderPanel.eyesHeight+panelBuffer;

    // Take over the top of the screen
    SpriteUIUtils.drawMenuFrame(myG, SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR,
        0, -1, myWidth, panelHeight*3/2, 2);

    final int panelSpacing = 1;
    final int panelXShift = panelWidth + panelSpacing;

    // Draw the list of COs in your tag from left to right
    final int drawY = 4;
    final ArrayList<Integer> taggedCOs = tagCmdrList;
    for( int tagToDraw = 0; tagToDraw < taggedCOs.size(); ++tagToDraw )
    {
      CommanderInfo coInfo = infos.get(taggedCOs.get(tagToDraw));

      BufferedImage playerImage = SpriteLibrary.getCommanderSprites( coInfo.name ).eyes;

      int drawX = 4 + (tagToDraw*panelXShift);
      myG.setColor(Color.BLACK);
      myG.fillRect(drawX, drawY, panelWidth, panelHeight);
      int dx = drawX+panelThickness, dy = drawY+panelThickness;

      myG.setColor(playerColor);
      myG.fillRect(dx, dy, panelWidth-panelThickness-1, panelHeight-panelThickness-1);

      if( tagToDraw+1 == taggedCOs.size() )
      {
        myG.setColor(Color.BLACK);
        // Draw a little plus sign
        myG.drawLine(drawX + 2*panelWidth/7, drawY +   panelHeight/2,
                     drawX + 5*panelWidth/7, drawY +   panelHeight/2);
        myG.drawLine(drawX +   panelWidth/2, drawY + 2*panelHeight/7,
                     drawX +   panelWidth/2, drawY + 5*panelHeight/7);
      }
      else
        myG.drawImage(playerImage, dx, dy, null);
    }

    // Throw in a done button
    BufferedImage readyButton = SpriteUIUtils.makeTextFrame("done", 3, 2);

    int drawX = 4 + (taggedCOs.size()*panelXShift);
    int dx = drawX+panelThickness, dy = drawY+panelThickness;
    // Center it
    dx += (panelWidth  - readyButton.getWidth() ) / 2;
    dy += (panelHeight - readyButton.getHeight()) / 2;
    myG.drawImage(readyButton, dx, dy, null);

    // Draw the cursor over the selected option.
    final int selTagIndex = tagIndexSelector.getSelectionNormalized();
    tagPickerOffsetX.set(4 + (selTagIndex*panelXShift));
    SpriteCursor.draw(myG, tagPickerOffsetX.geti(), drawY, panelWidth, panelHeight, playerColor);
  }

  /**
   * Renders itself into an image like this, with no scaling applied.
   * +----------------+
   * |                |
   * |   Cmdr Eyes    |
   * |                |
   * +----------------+
   */
  private static class CommanderPanel
  {
    // A couple of helper quantities.
    public static int eyesWidth = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getWidth();
    public static int eyesHeight = SpriteLibrary.getCommanderSprites( "STRONG" ).eyes.getHeight();

    // Total vertical panel space, sans scaling.
    public static final int PANEL_HEIGHT = eyesHeight + 2; // Eyes plus 1 above and below.

    // The composed TeamPanel image.
    private BufferedImage myImage;

    // Each frame that makes up the larger panel.
    private SpriteUIUtils.ImageFrame commanderFace;

    // Stored values.
    Color myColor;

    public CommanderPanel(CommanderInfo info, Color color)
    {
      update(info, color);
    }

    public BufferedImage update(CommanderInfo coInfo, Color color)
    {
      if( !color.equals(myColor))
      {
        myColor = color;
        commanderFace = new SpriteUIUtils.ImageFrame(1, 1, eyesWidth, eyesHeight, color,
            color, true, SpriteLibrary.getCommanderSprites( coInfo.name ).eyes);

        // Re-render the panel.
        myImage = SpriteLibrary.createTransparentSprite( commanderFace.width + 2, PANEL_HEIGHT );
        Graphics g = myImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, commanderFace.width + 2, myImage.getHeight());
        commanderFace.render(g);
      }

      return myImage;
    }
  }

 // Input control be beyond here
  public static boolean amPickingTagIndex;
  // Range: [0, tag count], to handle the "done" button.
  public static OptionSelector tagIndexSelector;

  public static boolean sortByGameThenFaction = true;
  public static OptionSelector outerCategorySelector;
  // TODO: unused
  public static OptionSelector innerCategorySelector;

  public static ArrayList<ArrayList<Integer>> cmdrBins;
  public static ArrayList<COSpriteSpec> outerBinColorSpec;
  public static ArrayList<COSpriteSpec> binColorSpec;
  public static OptionSelector cmdrBinSelector;
  public static OptionSelector cmdrInBinSelector;
  public static ArrayList<Integer> tagCmdrList;
  public static int rightGlueColumn;

  public static void initFromControl()
  {
    boolean shouldSelectMultiCO = myControl.shouldSelectMultiCO;
    amPickingTagIndex = shouldSelectMultiCO;

    tagCmdrList = myControl.getInitialCmdrs();

    final int firstCO = tagCmdrList.get(0);

    // Pad with an extra No CO so we can add tag partners
    if( shouldSelectMultiCO && myControl.noCmdr != firstCO )
      tagCmdrList.add(myControl.noCmdr);

    // Clean up state to avoid weirdness
    cmdrBinSelector = null;
    initBins(firstCO);
  }

  /** Set up bins for our newly-selected outer category */
  public static void initBins(final int selectedCO)
  {
    final CommanderInfo selectedInfo = myControl.cmdrInfos.get(selectedCO);

    ArrayList<Integer>[] innerCategoryCOs;
    outerBinColorSpec = new ArrayList<>();
    binColorSpec = new ArrayList<>();
    int outerMax, outerSel, innerMax, innerSel;
    if( sortByGameThenFaction )
    {
      outerMax = UIUtils.SourceGames.values().length;
      // Pivot our *previous* inner selection to be our new outer selection
      if( null == cmdrBinSelector )
        outerSel = selectedInfo.game.ordinal();
      else
        outerSel = cmdrBinSelector.getSelectionNormalized();
      innerMax = myControl.actualFactions.length;
      innerSel = 0;
      for( ; innerSel < myControl.actualFactions.length; ++innerSel )
        if( selectedInfo.baseFaction == myControl.actualFactions[innerSel] )
          break;

      // outer bin = game
      for( SourceGames game : UIUtils.SourceGames.values() )
        outerBinColorSpec.add(game.uiColorSpec);
      // Factions for both
      innerCategoryCOs = myControl.cosByGameFaction[outerSel];
      for( COSpriteSpec spec : myControl.actualFactions )
        binColorSpec.add(spec);
    }
    else
    {
      outerMax = myControl.actualFactions.length;
      outerSel = 0;
      // Pivot our previous *inner* selection to be our new outer selection
      if( null == cmdrBinSelector )
      {
        for( ; outerSel < myControl.actualFactions.length; ++outerSel )
          if( selectedInfo.baseFaction == myControl.actualFactions[outerSel] )
            break;
      }
      else
        outerSel = cmdrBinSelector.getSelectionNormalized();
      innerMax = UIUtils.SourceGames.values().length;
      innerSel = selectedInfo.game.ordinal();

      // outer bin = faction
      for( COSpriteSpec spec : myControl.actualFactions )
        outerBinColorSpec.add(spec);
      // Games for both
      innerCategoryCOs = myControl.cosByFactionGame[outerSel];
      for( SourceGames game : UIUtils.SourceGames.values() )
        binColorSpec.add(game.uiColorSpec);
    }
    outerCategorySelector = new OptionSelector(outerMax);
    outerCategorySelector.setSelectedOption   (outerSel);
    innerCategorySelector = new OptionSelector(innerMax);
    innerCategorySelector.setSelectedOption   (innerSel);
    // TODO: delete
    cmdrBinSelector = new OptionSelector(innerMax);
    cmdrBinSelector.setSelectedOption   (innerSel);

    cmdrBins = new ArrayList<>();

    int startBin = innerSel;

    // Set up our bins - each one contains COs from one canonical group
    for( int innerIdX = 0; innerIdX < innerCategoryCOs.length; ++innerIdX )
    {
      ArrayList<Integer> innerBin = new ArrayList<>(innerCategoryCOs[innerIdX]);
      if( 1 > innerBin.size() )
        // Enable selecting any faction from any game and vice versa
        innerBin.add(myControl.noCmdr);
      cmdrBins.add(innerBin);
    }

    tagIndexSelector = new OptionSelector(1);
    syncTagIndexSelector();

    int startBinIndex = cmdrBins.get(startBin).indexOf(selectedCO);
    rightGlueColumn = startBinIndex;

//    cmdrBinSelector = new OptionSelector(lastBin + 1);
//    cmdrBinSelector.setSelectedOption(startBin);
    cmdrInBinSelector = new OptionSelector(cmdrBins.get(startBin).size());
    cmdrInBinSelector.setSelectedOption(startBinIndex);
  }

  public static boolean handleInput(InputAction action)
  {
    boolean exitMenu = false;
    if( amPickingTagIndex )
      exitMenu = handleTagChoiceInput(action);
    else
      exitMenu = handleCmdrChoiceInput(action);
    return exitMenu;
  }

  private static boolean handleTagChoiceInput(InputAction action)
  {
    boolean done = false;
    final int selTagIndex = tagIndexSelector.getSelectionNormalized();
    switch(action)
    {
      case SELECT:
        amPickingTagIndex = false;

        if( selTagIndex >= tagCmdrList.size() )
        {
          // User says we're done - apply changes and get out.

          // Handle the pesky No CO at the end.
          if( 1 < tagCmdrList.size() )
            tagCmdrList.remove(tagCmdrList.size() - 1);

          myControl.applyCmdrChoices(tagCmdrList);
          done = true;
        }
        break;
      case UP:
      case DOWN:
      case LEFT:
      case RIGHT:
      {
        tagIndexSelector.handleInput(action);
      }
      break;
      case BACK:
        // Cancel: return control without applying changes.
        done = true;
        break;
      case SEEK:
        // Kick out the selected CO
        if( selTagIndex+1 < tagCmdrList.size() )
        {
          tagCmdrList.remove(selTagIndex);
          syncTagIndexSelector();
        }
        break;
      case VIEWMODE:
        int selectedCO = tagCmdrList.get(selTagIndex);
        myControl.startViewingCmdrInfo(selectedCO);
        break;
      default:
        // Do nothing.
    }
    return done;
  } // ~handleTagChoiceInput

  private static boolean handleCmdrChoiceInput(InputAction action)
  {
    boolean done = false;
    final int selectedBin    = cmdrBinSelector.getSelectionNormalized();
    final int selectedColumn = cmdrInBinSelector.getSelectionNormalized();
    // Value of selection; index into the list of CO infos
    final int selectedCO     = cmdrBins.get(selectedBin).get(selectedColumn);
    switch(action)
    {
      case SELECT:
        // handleTagChoiceInput() should ensure this index is in [0, tag count)
        final int selTagIndex = tagIndexSelector.getSelectionNormalized();

        tagCmdrList.set(selTagIndex, selectedCO);
        // Are we bimodal?
        if( !myControl.shouldSelectMultiCO )
        {
          // No; apply change and return control.
          myControl.applyCmdrChoices(tagCmdrList);
          done = true;
        }
        else // Yes
        {
          amPickingTagIndex = true;

          // Add/remove if appropriate
          if( selTagIndex + 1 >= tagCmdrList.size() )
          {
            tagCmdrList.add(myControl.noCmdr); // Extend the list if we just added a new tag partner
            syncTagIndexSelector();
            tagIndexSelector.handleInput(InputAction.DOWN); // Auto-pick the plus again
          }
        }
        break;
      case UP:
      case DOWN:
      {
        final int binPicked = cmdrBinSelector.handleInput(action);
        // TODO
        // If we've gone off the end, jump to the next outer category
//        if( binPicked != cmdrBinSelector.getSelectionAbsolute() )
//        {
//          outerCategorySelector.handleInput(action);
//          cmdrBinSelector.setSelectedOption(binPicked);
//        }
        final int destBinSize = cmdrBins.get(binPicked).size();
        // Selection column clamps to the max for the new bin
        cmdrInBinSelector.reset(destBinSize);
        final int destColumn = Math.min(destBinSize - 1, rightGlueColumn);
        cmdrInBinSelector.setSelectedOption(destColumn);
      }
        break;
      case LEFT:
      case RIGHT:
      {
        rightGlueColumn = cmdrInBinSelector.handleInput(action);
      }
      break;
      case BACK:
        if( !myControl.shouldSelectMultiCO )
        {
          // Cancel: return control without applying changes.
          done = true;
        }
        else
        {
          amPickingTagIndex = true;
        }
        break;
      case SEEK:
        // Flip our filtering around
        sortByGameThenFaction = !sortByGameThenFaction;
        initBins(selectedCO);
        break;
      case VIEWMODE:
        myControl.startViewingCmdrInfo(selectedCO);
        break;
      default:
        // Do nothing.
    }
    return done;
  } // ~handleCmdrChoiceInput


  public static void syncTagIndexSelector()
  {
    final int tagIndex = tagIndexSelector.getSelectionNormalized();
    tagIndexSelector.reset(tagCmdrList.size() + 1);
    tagIndexSelector.setSelectedOption(tagIndex);
  }

}
