package UI;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import Engine.Driver;

public class InputHandler implements KeyListener {

	Driver driver;
	
	public enum InputAction {NO_ACTION, UP, DOWN, LEFT, RIGHT, ENTER};
	
	public InputHandler(Driver driver)
	{
		this.driver = driver;
	}

	@Override
	public void keyPressed(KeyEvent key) {
		driver.inputCallback(getActionFromKey(key));
	}

	/**
	 * Converts a keyboard event into an action that the rest of the game understands.
	 * This can allow for re-binding keys, etc by changing the mapping.
	 * @param event
	 * @return
	 */
	private InputAction getActionFromKey(java.awt.event.KeyEvent event)
	{
		InputAction ia = InputAction.NO_ACTION;
		switch(event.getKeyCode())
		{
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			ia = InputAction.UP;
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			ia = InputAction.DOWN;
			break;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_A:
			ia = InputAction.LEFT;
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_D:
			ia = InputAction.RIGHT;
			break;
		case KeyEvent.VK_ENTER:
		case KeyEvent.VK_SPACE:
			ia = InputAction.ENTER;
			break;
		}
		return ia;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Don't care.
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// Don't care.
	}

}
