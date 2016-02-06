package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Holds a collection of related images, e.g. an animation sequence.
 */
public class Sprite
{
	ArrayList<BufferedImage> spriteImages;
	
	public Sprite(BufferedImage baseSprite)
	{
		spriteImages = new ArrayList<BufferedImage>();
		spriteImages.add(baseSprite);
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
