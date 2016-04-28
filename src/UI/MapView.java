package UI;

import java.awt.Dimension;
import java.awt.Graphics;

import UI.Art.MapArtist;
import UI.Art.MenuArtist;
import UI.Art.UnitArtist;
import UI.Art.Animation.AnimationSequence;
import UI.Art.Animation.NobunagaBattleAnimation;

import Engine.GameAction;
import Engine.MapController;

public abstract class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	public GameMenu currentMenu;
	public GameAction currentAction = null;

	private int unitMoveSpeedMsPerTile = 100;

	protected MapArtist mapArtist;
	protected UnitArtist unitArtist;
	protected MenuArtist menuArtist;
	
	private final int tileSizePx = 16; // TODO: Does this belong in MapView?
	private int drawScale = 3;
	private int mapViewWidth = tileSizePx * drawScale * 15;
	private int mapViewHeight = tileSizePx * drawScale * 10;

	protected AnimationSequence currentAnimation = null;

	protected MapController mapController = null;

	public MapView(MapArtist mapArt, UnitArtist unitArt, MenuArtist menuArt)
	{
		// TODO: Move this down to the child classes?
		setPreferredSize(new Dimension(mapViewWidth, mapViewHeight));

		mapArt.setView(this);
		unitArt.setView(this);
		menuArt.setView(this);

		mapArtist = mapArt; // TODO: Perhaps mapArtist should determine mapViewHeight, etc.
		unitArtist = unitArt;
		menuArtist = menuArt;
	}

	public void setController(MapController controller)
	{
		mapController = controller;
	}

	public int getTileSize()
	{
		return tileSizePx * drawScale;
	}
	public int getViewWidth()
	{
		return mapViewWidth;
	}
	public int getViewHeight()
	{
		return mapViewHeight;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		mapArtist.drawMap(g);
		mapArtist.drawHighlights(g);
		if(mapController.getContemplatedMove() != null)
		{
			mapArtist.drawMovePath(g, mapController.getContemplatedMove());
		}
		unitArtist.drawUnits(g);

		if(currentAnimation != null)
		{
			// Animate until it tells you it's done.
			if(currentAnimation.animate(g))
			{
				currentAction = null;
				currentAnimation = null;
				mapController.animationEnded();
			}
		}
		else if (currentMenu == null)
		{
			mapArtist.drawCursor(g);
		}
		else
		{
			menuArtist.drawMenu(g);
		}
	}

	public void animate(GameAction action)
	{
		if(currentAction != null && currentAction != action)
		{
			// Typically, this will be called either for a player action (in which case currentAction
			//  should have been populated through UI interaction, or for an AI action, in which case
			//  currentAction will still be null (because AIs don't use UIs).
			System.out.println("WARNING! Animating an unexpected action!");
		}

		currentAction = action;

		// If we have a previous animation in progress, cancel it to start the new one.
		if(currentAnimation != null)
		{
			currentAnimation.cancel();
			currentAnimation = null;
		}

		switch(currentAction.getActionType())
		{
		case ATTACK:
			currentAnimation = new NobunagaBattleAnimation(currentAction, getTileSize());
			break;
		case CAPTURE:
		case LOAD:
		case WAIT:
		case UNLOAD:
			break;
		case INVALID:
			default:
				System.out.println("WARNING! No action animation supported for type " + currentAction.getActionType());
		}

		if(currentAnimation == null)
		{
			// Animation for this action is not supported. Just let the controller know.
			mapController.animationEnded();
		}
	}
	public boolean isAnimating()
	{
		return currentAnimation != null;
	}
	public void cancelAnimation()
	{
		if(currentAnimation != null)
		{
			currentAnimation.cancel();
		}
	}
	public double getMapUnitMoveSpeed()
	{
		return unitMoveSpeedMsPerTile;
	}
}