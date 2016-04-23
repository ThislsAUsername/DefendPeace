package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import CommandingOfficers.Commander;
import Units.Unit;

public class UnitSpriteSet {

	Sprite sprites[] = new Sprite[6];
	
	public final int ACTION_IDLE = 0;
	public final int ACTION_MOVENORTH = 1;
	public final int ACTION_MOVEEAST = 2;
	public final int ACTION_MOVESOUTH = 3;
	public final int ACTION_MOVEWEST = 4;
	public final int ACTION_DIE = 5;
	
	Sprite turnDone;
	
	/**
	 * UnitSpritSet constructor - parse the provided sprite sheet. Sprites are expected to 
	 * be arranged in rows by animation type, in the following order:
	 * idle
	 * Move North
	 * Move East
	 * Move South
	 * Move West
	 * Death
	 * 
	 * @param spriteSheet The image containing the frames to be used in this sprite set.
	 * @param width The width of each frame in the sprite.
	 * @param height The height of each frame in the sprite.
	 */
	public UnitSpriteSet( BufferedImage spriteSheet, int width, int height, ColorPalette coColors )
	{
		int h = 0;
		int action = ACTION_IDLE; // This variable relies on the ordering of the ACTION variables defined above.
		
		try
		{
			while( height < spriteSheet.getHeight() && action <= ACTION_DIE )
			{
				Sprite spr = new Sprite( spriteSheet.getSubimage(0, h, spriteSheet.getWidth(), height), width, height);
				sprites[action] = spr;
				++action;
			}
			if( action < ACTION_DIE )
			{ // We didn't load all animations we wanted - our sprite set is incomplete. Default the rest to IDLE.
				sprites[action] = sprites[ACTION_IDLE];
				++action;
			}
		}
		catch(RasterFormatException rfe)
		{
			// Something went wrong. Just make something up and hope nobody notices.
			// Use the IDLE action if it exists, otherwise we are going with a black rectangle.
			Sprite defaultSprite = (action > ACTION_IDLE)? sprites[ACTION_IDLE]:new Sprite(null, width, height);
			
			System.out.println("WARNING: RasterFormatException in UnitSpriteSet constructor.");
			System.out.println("WARNING:   Attempting to continue.");
			for(; action <= ACTION_DIE; ++action)
			{
				sprites[action] = defaultSprite;
			}
		}
		
		colorize(SpriteLibrary.defaultMapColors, coColors.paletteColors);
		if(action > 0) // We at least got the IDLE sprites. Use those as the basis for the "already moved" sprites.
		{
			turnDone = new Sprite(sprites[0]); // Duplicate the IDLE sprite to make the TurnDone sprite.

			// Get my color presets.
			Color tiredColor = new Color(128,128,128,160);
			
			// Shade each frame so the unit can be grayed-out after moving.
			for(int f = 0; f < turnDone.numFrames(); ++f)
			{
				BufferedImage frame = turnDone.getFrame(f);
				Graphics g = frame.getGraphics();
				g.setColor(tiredColor);

				// Loop through each pixel and shade the non-transparent ones.
				for(int y = 0; y < frame.getHeight(); ++y)
				{
					for(int x = 0; x < frame.getWidth(); ++x)
					{
						// Only shade pixels that are are not transparent.
						if(frame.getRGB(x,y) != 0 )
						{
							// Yes, one pixel at a time.
							g.fillRect(x, y, 1, 1);
						}
					}
				}
			}
		}
	}

    private void colorize(Color[] oldColors, Color[] newColors)
    {
        for(Sprite s : sprites)
        {
            s.colorize(oldColors, newColors);
        }
    }
    
    public void drawUnit( Graphics g, Commander activeCO, Unit u, /* int action,*/ int imageIndex, int drawX, int drawY, int drawScale )
    {
    	BufferedImage frame = null;
    	
    	// Retrieve the correct subimage.
    	if(u.isTurnOver && u.CO == activeCO)
    	{
    		frame = turnDone.getFrame(imageIndex);
    	}
    	else
    	{
    		frame = sprites[ACTION_IDLE/*action*/].getFrame(imageIndex);
    	}

    	// Draw it.
		g.drawImage(frame, drawX, drawY, frame.getWidth()*drawScale, frame.getHeight()*drawScale, null);
    }
}
