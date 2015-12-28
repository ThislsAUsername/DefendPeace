package UI;

import Engine.MapController;

public class GameMenu {
	
	private Enum options[];
	public enum MenuType {ACTION, PRODUCTION};
	public MenuType menuType;
	int selectedOption = 0;
	
	public GameMenu(Enum[] options)
	{
		this.options = options;
	}

	public void handleMenuInput(InputHandler.InputAction action)
	{
		switch(action)
		{ // TODO: this doesn't work, at least not for the ActionMenu
		case UP:
			selectedOption = (selectedOption > 0)? selectedOption-- : selectedOption;
			break;
		case DOWN:
			selectedOption = (selectedOption < options.length-1)? selectedOption++ : selectedOption;
			break;
		case LEFT:
		case RIGHT:
		case ENTER:
		case BACK:
		case NO_ACTION:
			default:
				System.out.println("WARNING! gameMenu.handleMenuInput() was given invalid action enum (" + action + ")");
		}
	}
	
	public Enum getSelectedAction()
	{
		return options[selectedOption];
	}
	
	public Enum[] getOptions()
	{
		return options;
	}
	
	public int getNumChoices()
	{
		return options.length;
	}
}
