package Engine;

import UI.InputHandler;

public class MapController {

	private GameInstance myGame;
	
	public MapController(GameInstance game)
	{
		myGame = game;
	}
	
	public void handleAction(InputHandler.InputAction action)
	{
		switch (action)
		{
		case UP:
			myGame.moveCursorUp();
			break;
		case DOWN:
			myGame.moveCursorDown();
			break;
		case LEFT:
			myGame.moveCursorLeft();
			break;
		case RIGHT:
			myGame.moveCursorRight();
			break;
		case ENTER:
			// See what is at the current cursor location, in precedence order of Unit, Building, Terrain.
			break;
			default:
				System.out.println("WARNING! MapController was given invalid action enum (" + action + ")");
		}
	}
}
