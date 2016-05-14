package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Holds a collection of related images, e.g. an animation sequence.
 */
public class Sprite
{
	private ArrayList<BufferedImage> spriteImages;
	
	public Sprite(BufferedImage baseSprite)
	{
		spriteImages = new ArrayList<BufferedImage>();
		spriteImages.add(baseSprite);
	}
	
	/**
	 * Parses the provided sprite sheet into individual, like-sized frames, with the assumption
	 * that the sprite images are arranged horizontally.
	 * If the sprite sheet is taller than the provided height, the extra image space is ignored.
	 * If the sprite sheet length does not divide evenly, then the final (too-short) section
	 * of the image will NOT be included as a separate frame.
	 * @param spriteSheet The image to parse into frames.
	 * @param spriteWidthPx The width of each frame in pixels.
	 * @param spriteHeightPx The height of each frame in pixels.
	 */
	public Sprite(BufferedImage spriteSheet, int spriteWidthPx, int spriteHeightPx)
	{
		spriteImages = new ArrayList<BufferedImage>();

		if( null == spriteSheet )
		{
			System.out.println("WARNING! [Sprite()] Continuing with placeholder image.");
			// Just make a single blank frame of the specified size.
			spriteImages.add(SpriteLibrary.createDefaultBlankSprite(spriteWidthPx, spriteHeightPx));
		}
		else
		{
			// Cut the sprite-sheet up.
			int xOffset = 0;
			int yOffset = 0;
			int spriteNum = 0;

			// Loop until we get as many sprites as we expect or run out of runway.
			while( (spriteNum+1)*spriteWidthPx <= spriteSheet.getWidth() )
			{
				spriteImages.add(spriteSheet.getSubimage(xOffset, yOffset, spriteWidthPx, spriteHeightPx));
				xOffset += spriteWidthPx;
				spriteNum++;
			}
		}
	}

	/**
	 * Sprite copy-constructor. Perform a deep-copy on each of the other sprite's frames.
	 * @param other
	 */
	public Sprite(Sprite other)
	{
		spriteImages = new ArrayList<BufferedImage>();

		if( null == other )
		{
			System.out.println("WARNING! [Sprite()] Continuing with placeholder image.");
			// Just make a single blank frame of the specified size.
			spriteImages.add(SpriteLibrary.createDefaultBlankSprite(SpriteLibrary.baseSpriteSize, SpriteLibrary.baseSpriteSize));
		}
		else
		{
			for(int i = 0; i < other.numFrames(); ++i)
			{
				BufferedImage aFrame = other.getFrame(i);
				BufferedImage myFrame = new BufferedImage(aFrame.getWidth(), aFrame.getHeight(), BufferedImage.TYPE_INT_ARGB);
				myFrame.getGraphics().drawImage(aFrame, 0, 0, null);
				spriteImages.add(myFrame);
			}
		}
	}

	public int numFrames()
	{
		return spriteImages.size();
	}

	/**
	 * Adds another frame to this sprite.
	 * @param sprite
	 */
	public void addFrame(BufferedImage sprite)
	{
		spriteImages.add(sprite);
	}
	
	public BufferedImage getFrame(int index)
	{
		// Normalize the index if needed.
		if(index >= spriteImages.size())
		{
			index = index % spriteImages.size();
		}
		
		return spriteImages.get(index);
	}

	/**
	 * For every image contained in this sprite, change each pixel with a value in oldColors to the corresponding value in newColors.
	 */
	public void colorize(Color[] oldColors, Color[] newColors)
	{
		for(BufferedImage bi : spriteImages)
		{
			for(int x = 0; x < bi.getWidth(); ++x)
			{
				for(int y = 0; y < bi.getHeight(); ++y)
				{
					int colorValue = bi.getRGB(x,y);
					for(int c = 0; c < oldColors.length; ++c)
					{
						if(oldColors[c].getRGB() == colorValue)
						{
							bi.setRGB(x, y, newColors[c].getRGB());
						}
					}
				}
			}
		}
	}
}
