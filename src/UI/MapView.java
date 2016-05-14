package UI;

import UI.Art.Animation.AnimationSequence;
import UI.Art.Animation.NobunagaBattleAnimation;

import Engine.GameAction;
import Engine.MapController;

public abstract class MapView extends javax.swing.JPanel {

	private static final long serialVersionUID = 1L;
	
	public GameMenu currentMenu;
	public GameAction currentAction = null;

	private int unitMoveSpeedMsPerTile = 100;

	private int drawScale = 2;

	protected AnimationSequence currentAnimation = null;

	protected MapController mapController = null;

	public void setController(MapController controller)
	{
		mapController = controller;
	}

	/**
	 * @return The side-length in pixels of a single map square, taking drawScale into account.
	 * NOTE: This assumes that all MapView subclasses will use a square-tile map representation.
	 */
	public abstract int getTileSize();
	/**
	 * @return The width in pixels of the entire map view.
	 */
	public abstract int getViewWidth();
	/**
	 * @return The height in pixels of the entire map view.
	 */
	public abstract int getViewHeight();

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
	public int getDrawScale()
	{
		return drawScale;
	}
}