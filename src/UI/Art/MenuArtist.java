package UI.Art;

import java.awt.Graphics;

import UI.MapView;

public interface MenuArtist {
	public void setView(MapView view);
	public void drawMenu(Graphics g);
}
