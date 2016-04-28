package UI.Art;

import java.awt.Graphics;

import UI.MapView;
import Units.Unit;

public interface UnitArtist {
	/** Provides the UnitArtist with its controlling View object. */
	public void setView(MapView view);
	/**
	 * Draws all units (or possibly all units within the current view.
	 */
	public void drawUnits(Graphics g);
	/**
	 * Allows drawing of a single unit, at a specified real location.
	 * "Real" means that the specified x and y are that of the game's
	 * data model, not the draw-space.
	 */
	public void drawUnit(Graphics g, Unit unit, double x, double y);
}
