package UI.Art;

import java.awt.Graphics;

import UI.MapView;

public interface UnitArtist {
	public void setView(MapView view);
	public void drawUnits(Graphics g);
}
