package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import AI.AILibrary;
import AI.AIMaker;
import Engine.IController;
import UI.PlayerSetupAiController;

public class PlayerSetupAiArtist
{
  private static HashMap<Integer, BufferedImage> aiNameplates = new HashMap<Integer, BufferedImage>();
  private static int maxAiNameLength = -99;

  public static void draw(Graphics g, IController controller, Color playerColor)
  {
    PlayerSetupAiController control = (PlayerSetupAiController)controller;
    if( null == control )
    {
      System.out.println("WARNING! PlayerSetupAiController was given the wrong controller!");
    }

    // Define the draw space
    int drawScale = SpriteOptions.getDrawScale();
    Dimension dimensions = SpriteOptions.getScreenDimensions();
    int myWidth = dimensions.width / drawScale;
    int myHeight = dimensions.height / drawScale;
    BufferedImage image = SpriteLibrary.createTransparentSprite(myWidth, myHeight);
    Graphics myG = image.getGraphics();

    /////////////// AI Names //////////////////////
    ArrayList<AIMaker> aiOptions = AILibrary.getAIList();
    if(maxAiNameLength < 0) // Initialize our name tags if we haven't yet.
    {
      // Figure out the longest-length name.
      for( int i = 0; i < aiOptions.size(); ++i )
      {
        if( aiOptions.get(i).getName().length() > maxAiNameLength ) maxAiNameLength = aiOptions.get(i).getName().length();
      }

      // Make a string of all spaces of the required length so we can build nameplates of the same length. Soooo hacky, I know.
      StringBuilder buf = new StringBuilder();
      for( int n = 0; n < maxAiNameLength; ++n ) buf.append(" ");

      // Build and store off our nameplates for posterity.
      for( int i = 0; i < aiOptions.size(); ++i )
      {
        BufferedImage frame = SpriteUIUtils.makeTextFrame(SpriteUIUtils.MENUBGCOLOR, SpriteUIUtils.MENUFRAMECOLOR, buf.toString(), 3, 2);
        SpriteLibrary.drawImageCenteredOnPoint(frame.getGraphics(), SpriteLibrary.getTextAsImage(aiOptions.get(i).getName()), frame.getWidth()/2, frame.getHeight()/2, 1);
        aiNameplates.put(i, frame);
      }
    }

    // Calculate the vertical space each name will consume.
    int nameplateHeight = aiNameplates.get(0).getHeight();
    int nameBuffer = nameplateHeight/2;
    int nameplateYSpace = nameplateHeight+nameBuffer;

    // Find where the zeroth option should be drawn.
    int highlightedAi = control.getSelectedAiIndex();
    int drawY = nameBuffer/2;

    // Draw all of the visible AI names.
    for(int i = 0; i < aiNameplates.size(); i++, drawY += (nameplateYSpace))
    {
      // Only bother to draw it if it is on-screen.
      if( (drawY > -nameplateYSpace/2) && ( drawY < myHeight+(nameplateYSpace/2) ) )
      {
        BufferedImage aiNameplate = aiNameplates.get(i);

        int drawX = nameBuffer;
        myG.drawImage(aiNameplate, drawX, drawY, null);

        // Draw the cursor if this panel is highlighted
        if( highlightedAi == i )
        {
          SpriteUIUtils.drawCursor(myG, drawX, drawY, aiNameplate.getWidth(), aiNameplate.getHeight(), playerColor);
        }
      }
    }

    ///////////////// AI Info ///////////////////////
    int aiNameplateZoneWidth = aiNameplates.get(0).getWidth() + nameplateHeight;
    int infoBuffer = nameplateHeight/2;
    int aiInfoZoneWidth = myWidth - aiNameplateZoneWidth;
    int infoX = aiNameplateZoneWidth + infoBuffer;
    int infoY = infoBuffer;
    int infoW = aiInfoZoneWidth - infoBuffer * 2;
    int infoH = myHeight - infoBuffer*2;
    String text = "If I had anything helpful to say about this AI, I would write it here.\n" +
        "I don't though, so all you get is this useless blurb.";

    // Draw a nice frame and populate it with some useful info about the AI.
    myG.setColor(new Color(255, 255, 255, 80));
    myG.fillRect(infoX, infoY, infoW, infoH);
    myG.setColor(new Color(100, 115, 130));
    myG.drawRect(infoX, infoY, infoW, infoH);

    // Draw the composed image to the window at scale.
    g.drawImage(image, 0, 0, dimensions.width, dimensions.height, null);

    // TODO: Fix drawTextToWidth to not apply scaling automatically.
    BufferedImage infoText = SpriteUIUtils.drawTextToWidth(text, (aiInfoZoneWidth - nameplateHeight*2)*drawScale);
    g.drawImage(infoText, (infoX+infoBuffer)*drawScale, (infoY+infoBuffer)*drawScale, null);
  }
}
