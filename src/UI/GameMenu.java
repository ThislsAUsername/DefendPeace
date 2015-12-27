package UI;

import Engine.MapController;

public class GameMenu {
	
	private Enum options[];
	public String[] labels;
	int selectedOption = 0;
	
	public GameMenu(Enum[] options, String[] labels)
	{
		this.options = options;
		this.labels = labels;
	}

	public void handleMenuInput(InputHandler.InputAction action)
	{
		switch(action)
		{
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
}
