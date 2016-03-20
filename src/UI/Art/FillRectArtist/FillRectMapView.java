package UI.Art.FillRectArtist;

import Engine.GameInstance;
import UI.MapView;

public class FillRectMapView extends MapView 
{
	private static final long serialVersionUID = 1L;

	public FillRectMapView( GameInstance game )
	{
		super(new FillRectMapArtist(game), new FillRectUnitArtist(game), new FillRectMenuArtist(game));
	}
}
