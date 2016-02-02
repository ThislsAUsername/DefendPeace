package UI.Art.SpriteArtist;

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
}
