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

	private MapArtist mapArtist;
	private UnitArtist unitArtist;
	private MenuArtist menuArtist;
	
	private final int tileSizePx = 16;
	private int drawScale = 3;
	private int mapViewWidth = tileSizePx * drawScale * 15;
	private int mapViewHeight = tileSizePx * drawScale * 10;

	private AnimationSequence animationSequence = null;

	MapController myController = null;

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
		myController = controller;
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
		unitArtist.drawUnits(g);

		if(animationSequence != null)
		{
			// Animate until it tells you it's done.
			if(animationSequence.animate(g))
			{
				currentAction = null;
				animationSequence = null;
				myController.animationEnded();
			}
		}
		else if (currentMenu == null)
		{
			mapArtist.drawCursor(g);

			if(myController.getContemplatedMove() != null)
			{
				mapArtist.drawMovePath(g, myController.getContemplatedMove());
			}
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
		if(animationSequence != null)
		{
			animationSequence.cancel();
			animationSequence = null;
		}

		switch(currentAction.getActionType())
		{
		case ATTACK:
			animationSequence = new NobunagaBattleAnimation(currentAction, getTileSize());
			break;
		case CAPTURE:
			// TODO: Only do alert if the capture is completed, not just partial.
			mapArtist.alertTileChanged(action.getActX(), action.getActY());
		case LOAD:
		case WAIT:
		case UNLOAD:
			break;
		case INVALID:
			default:
				System.out.println("WARNING! No action animation supported for type " + currentAction.getActionType());
		}

		if(animationSequence == null)
		{
			// Animation for this action is not supported. Just let the controller know.
			myController.animationEnded();
		}
	}
	public boolean isAnimating()
	{
		return animationSequence != null;
	}
	public void cancelAnimation()
	{
		if(animationSequence != null)
		{
			animationSequence.cancel();
		}
	}
	public double getMapUnitMoveSpeed()
	{
		return unitMoveSpeedMsPerTile;
	}
}