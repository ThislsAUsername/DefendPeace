package UI.Art.SpriteArtist;

import Engine.GameInstance;
import UI.MapView;
import UI.Art.SpriteArtist.SpriteMapArtist;
import UI.Art.FillRectArtist.FillRectMenuArtist;
import UI.Art.FillRectArtist.FillRectUnitArtist;

public class SpriteMapView extends MapView
{
	private static final long serialVersionUID = 1L;

	public SpriteMapView(GameInstance game)
	{
		super(new SpriteMapArtist(game), new FillRectUnitArtist(game), new FillRectMenuArtist(game));
	}
}
