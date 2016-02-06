package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Holds a collection of related images, e.g. an animation sequence.
 */
public class Sprite
{
	ArrayList<BufferedImage> tileSprites;
	
	public Sprite(BufferedImage baseSprite)
	{
		tileSprites = new ArrayList<BufferedImage>();
		tileSprites.add(baseSprite);
	}
	
	/**
	 * Adds another frame to this sprite.
	 * @param sprite
	 */
	public void addFrame(BufferedImage sprite)
	{
		tileSprites.add(sprite);
	}
	
	public BufferedImage getFrame(int index)
	{
		// Normalize the index if needed.
		if(index >= tileSprites.size())
		{
			index = index % tileSprites.size();
		}
		
		return tileSprites.get(index);
	}

	/**
	 * For every image contained in this sprite, change each pixel with a value in oldColors to the corresponding value in newColors.
	 */
	public void colorize(Color[] oldColors, Color[] newColors)
	{
		for(BufferedImage bi : tileSprites)
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
