package UI;

import java.awt.Dimension;
import java.awt.Graphics;

import UI.Art.FillRectMapArtist;
import UI.Art.FillRectMenuArtist;
import UI.Art.FillRectUnitArtist;
import UI.Art.MapArtist;
import UI.Art.MenuArtist;
import UI.Art.UnitArtist;
import UI.Art.Animation.AnimationSequence;
import UI.Art.Animation.NobunagaBattleAnimation;

import Engine.GameAction;
import Engine.GameInstance;
import Engine.MapController;
import Engine.Path;

public class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private GameInstance myGame;
	
	public GameMenu currentMenu;
	public GameAction currentAction = null;

	private double unitMoveSpeedMSPerTile = 100;

	private MapArtist mapArtist;
	private UnitArtist unitArtist;
	private MenuArtist menuArtist;
	
	public static final int tileSizePx = 32;
	public static int mapViewWidth = tileSizePx * 15;
	public static int mapViewHeight = tileSizePx * 10;

	private AnimationSequence animationSequence = null;

	MapController myController = null;

	public MapView(GameInstance game)
	{
		myGame = game;
		setPreferredSize(new Dimension(mapViewWidth, mapViewHeight));

		unitArtist = new FillRectUnitArtist(myGame, this);
		mapArtist = new FillRectMapArtist(myGame);
		menuArtist = new FillRectMenuArtist(myGame, this);
	}

	public void setController(MapController controller)
	{
		myController = controller;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		mapArtist.drawMap(g);
		unitArtist.drawUnits(g);
		mapArtist.drawHighlights(g);

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
			animationSequence = new NobunagaBattleAnimation(currentAction);
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
		return unitMoveSpeedMSPerTile;
	}
}
