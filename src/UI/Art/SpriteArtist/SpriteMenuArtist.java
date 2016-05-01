package UI.Art.SpriteArtist;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import Engine.GameInstance;
import UI.GameMenu;
import UI.MapView;
import UI.Art.MenuArtist;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;

public class SpriteMenuArtist implements MenuArtist
{
	private GameInstance myGame;
	private MapView myView;
	private GameMenu currentMenu;
	private int drawScale;

	private final Color MENUFRAMECOLOR = new Color(169, 118, 65);
	private final Color MENUBGCOLOR = new Color(234, 204, 154);
	private final Color MENUHIGHLIGHTCOLOR = new Color(246, 234, 210);

	private int menuTextWidth;
	private int menuTextHeight;
	private int menuHBuffer; // Amount of visible menu to left and right of options;
	private int menuVBuffer; // Amount of visible menu above and below menu options;

	public SpriteMenuArtist(GameInstance game)
	{
		myGame = game;
		currentMenu = null;
	}

	@Override
	public void setView(MapView view)
	{
		myView = view;

		// Figure out how to scale the sprites we draw.
		drawScale = view.getTileSize() / SpriteLibrary.baseSpriteSize;
		menuTextWidth = SpriteLibrary.getMenuLetters().getFrame(0).getWidth()*drawScale;
		menuTextHeight = SpriteLibrary.getMenuLetters().getFrame(0).getHeight()*drawScale;
		menuHBuffer = 3*drawScale; // Amount of visible menu to left and right of options;
		menuVBuffer = 4*drawScale; // Amount of visible menu above and below menu options;
	}

	/**
	 * Draw the menu, centered around the location of interest.
	 * @param g
	 * @param menu
	 */
	@Override
	public void drawMenu(Graphics g)
	{
		GameMenu menu = myView.currentMenu;
		if(menu != null)
		{
			ArrayList<String> menuStrings = new ArrayList<String>();
			getMenuStrings(menu, menuStrings);

			// Append prices if this is a PRODUCTION menu
			if(menu.menuType == GameMenu.MenuType.PRODUCTION)
			{
				appendProductionPrices(menu, menuStrings);
			}

			// Find the dimensions of the menu we are drawing.
			int menuWidth = getMenuTextWidthPx(menuStrings)+menuHBuffer*2;
			int menuHeight = getMenuTextHeightPx(menuStrings)+menuVBuffer*2;

			// Center the menu over the current action target location.
			int viewTileSize = myView.getTileSize(); // Grab this value for convenience.
			int drawX = myGame.getCursorX()*viewTileSize - (menuWidth/2 - viewTileSize/2);
			int drawY = myGame.getCursorY()*viewTileSize - (menuHeight/2 - viewTileSize/2);

			// Make sure the menu is fully contained in viewable space.
			drawX = (drawX < 0)? 0 : (drawX > (myView.getViewWidth() - menuWidth))? (myView.getViewWidth() - menuWidth) : drawX;
			drawY = (drawY < 0)? 0 : (drawY > (myView.getViewHeight() - menuHeight))? (myView.getViewHeight() - menuHeight) : drawY;

			// Draw the nice box for our text.
			drawMenuFrame(g, drawX, drawY, menuWidth, menuHeight);

			// Draw the highlight for the currently-selected option.
			// selY = drawY plus upper menu-frame buffer, plus (letter height, plus 1px-buffer, times number of options).
			int selY = drawY + menuVBuffer + (menuTextHeight+drawScale)*menu.getSelectionNumber();
			g.setColor(MENUHIGHLIGHTCOLOR);
			g.fillRect(drawX, selY, menuWidth, menuTextHeight);

			// Draw the actual menu text.
			for(int txtY = drawY+menuVBuffer, i = 0; i < menu.getNumOptions(); ++i, txtY+=menuTextHeight+drawScale)
			{
				SpriteLibrary.drawMenuText(g, menuStrings.get(i), drawX+menuHBuffer, txtY, drawScale);
			}
		}
	}

	/**
	 * Populate 'out' with the string versions of the options available through 'menu'.
	 * @param menu
	 * @param out
	 */
	private void getMenuStrings(GameMenu menu, ArrayList<String> out)
	{
		for( int i = 0; i < menu.getNumOptions(); ++i )
		{
			String str = menu.getOptions()[i].toString();
			out.add(str);
		}
	}

	/**
	 * Preconditions:
	 * 1) 'menu' contains a list of only units that can be produced by myGame.activeCO.
	 * 2) 'out' is an ArrayList of Strings that align with the menu options.
	 * 'out' will be re-populated with strings of equal length containing the original
	 * names that it came with, with the prices appended.
	 * @param menu GameMenu with unit production options.
	 * @param out Parallel list of unit names as strings.
	 */
	private void appendProductionPrices(GameMenu menu, ArrayList<String> out)
	{
		int maxLength = 0;
		for(String s : out)
		{
			maxLength = (s.length() > maxLength)?s.length():maxLength;
		}
		maxLength++; // Plus 1 for a space between unit name and price.

		// Modify each String to include the price at a set tab level.
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < menu.getOptions().length; ++i, sb.setLength(0))
		{
			// Start with the production item name.
			sb.append(out.get(i));

			// Append spaces until this entry is the approved length.
			for( ; sb.length() < maxLength; sb.append(" "));

			// Get the price as a string.
			int price = 0;
			UnitModel model = myGame.activeCO.getUnitModel((UnitEnum) menu.getOptions()[i]);
			if(null == model)
			{
				System.out.println("WARNING: Attempting to find a price for an unavailable unit!");
				System.out.println("WARNING: Unit: " + menu.getOptions()[i].toString() + ", CO: " + myGame.activeCO.toString());
				price = 99999;
			}
			else
			{
				price = model.moneyCost;
			}

			// Pad the price with an extra space if it is only four digits.
			// NOTE: This line assumes that all prices will be either four or five digits.
			if(price < 10000) { sb.append(" "); }

			// Append the actual cost of the item.
			sb.append(Integer.toString(price));

			// Plug the new string into the return list.
			out.set(i, sb.toString());
		}
	}

	private void drawMenuFrame(Graphics g, int x, int y, int w, int h)
	{
		int menuFrameHeight = menuVBuffer/2; // Upper and lower bit can look framed.

		g.setColor(MENUBGCOLOR);
		g.fillRect(x, y, w, h); // Main menu body;
		g.setColor(MENUFRAMECOLOR);
		g.fillRect(x, y, w, menuFrameHeight); // Upper frame;
		g.fillRect(x, y+h-menuFrameHeight, w, menuFrameHeight); // Lower frame;
	}

	private int getMenuTextWidthPx(ArrayList<String> menuOptions)
	{
		int maxWidth = 0;
		for(int i = 0; i < menuOptions.size(); ++i)
		{
			int optw = menuOptions.get(i).length()*menuTextWidth;
			maxWidth = (optw > maxWidth)?optw:maxWidth;
		}

		return maxWidth;
	}

	private int getMenuTextHeightPx(ArrayList<String> menuOptions)
	{
		// Height of the letters plus 1 (for buffer between menu options), times the number of entries,
		// minus 1 because there is no buffer after the last entry.
		return (menuTextHeight+drawScale)*menuOptions.size() - drawScale;
	}
}
