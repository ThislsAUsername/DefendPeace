package UI;

import Engine.MapController;

public class GameMenu {
	
	private Enum options[];
	public enum MenuType {ACTION, PRODUCTION, METAACTION};
	public final MenuType menuType;
	int selectedOption = 0;
	
	public GameMenu(MenuType menuType, Enum[] options)
	{
		this.menuType = menuType;
		this.options = options;
	}

	public void handleMenuInput(InputHandler.InputAction action)
	{
		switch(action)
		{
		case UP:
			if(selectedOption > 0)
			{
				selectedOption--;
			}
			break;
		case DOWN:
			if(selectedOption < options.length-1)
			{
				selectedOption++;
			}
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
