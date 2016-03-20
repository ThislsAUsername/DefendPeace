package UI.Art;

import java.awt.Graphics;

import UI.MapView;

import Engine.Path;

public interface MapArtist {
	public void setView(MapView view);
	public void drawMap(Graphics g);
	public void drawCursor(Graphics g);
	public void drawMovePath(Graphics g, Path p);
	public void drawHighlights(Graphics g);
	public void alertTileChanged(int tileX, int tileY);
}
