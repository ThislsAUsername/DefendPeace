package UI;

import java.awt.Dimension;
import java.awt.Graphics;

import UI.Art.FillRectMapArtist;
import UI.Art.FillRectMenuArtist;
import UI.Art.FillRectUnitArtist;
import UI.Art.MapArtist;
import UI.Art.MenuArtist;
import UI.Art.UnitArtist;

import Engine.GameAction;
import Engine.GameInstance;
import Engine.MapController;

public class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	private GameInstance myGame;
	
	public GameMenu currentMenu;

	private MapArtist mapArtist;
	private UnitArtist unitArtist;
	private MenuArtist menuArtist;
	
	public static final int tileSizePx = 32;
	public static int mapViewWidth = tileSizePx * 15;
	public static int mapViewHeight = tileSizePx * 10;

	private GameAction animationAction = null;
	private AnimationSequence animationSequence = null;

	MapController myController = null;

	public MapView(GameInstance game)
	{
		myGame = game;
		setPreferredSize(new Dimension(mapViewWidth, mapViewHeight));

		unitArtist = new FillRectUnitArtist(myGame);
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

		if(animationAction != null && animationSequence == null)
		{
			switch(animationAction.getActionType())
			{
			case ATTACK:
				animationSequence = new NobunagaBattleAnimation(animationAction);
				break;
			case CAPTURE:
			case LOAD:
			case WAIT:
			case UNLOAD:
				break;
			case INVALID:
				default:
					System.out.println("WARNING! No action animation supported for type " + animationAction.getActionType());
			}
		}
		else if(animationSequence != null)
		{
			// Animate until it tells you it's done.
			if(animationSequence.animate(g))
			{
				animationAction = null;
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
		animationAction = action;
	}
	public boolean isAnimating()
	{
		return animationAction != null;
	}
	public void cancelAnimation()
	{
		if(animationAction != null)
		{
			animationSequence.cancel();
		}
	}
}
