package Engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import CommandingOfficers.Commander;
import Terrain.GameMap;
import UI.InputHandler;
import UI.MapView;

public class Driver implements ActionListener{

	private static final long serialVersionUID = 1L;

	JFrame gameWindow;
	private javax.swing.Timer repaintTimer;
	InputHandler inputHandler;

	// MenuView menuView;
	MapView mapView;
	// battleView battleView;

	// TODO: make this a GameView or some such when we get there.
	private MapView activeView;

	public Driver()
	{
		GameMap map = new GameMap();
		Commander co1 = new Commander();
		Commander co2 = new Commander();
		Commander[] cos = {co1, co2};
		GameInstance newGame = new GameInstance(map, cos);

		mapView = new MapView(newGame);

		activeView = mapView;

		gameWindow = new JFrame();
		gameWindow.add(mapView);
		gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameWindow.setSize(MapView.tileSizePx * 15,MapView.tileSizePx * 10);
		gameWindow.addKeyListener(new InputHandler(this));
		gameWindow.setVisible(true);

		// Draw the screen at (ideally) 60fps.
		repaintTimer = new javax.swing.Timer(16, this);
		repaintTimer.start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// Redraw the screen if needed.
		gameWindow.repaint();
	}

	public void inputCallback(InputHandler.InputAction action)
	{
		if(action != InputHandler.InputAction.NO_ACTION)
		{
			// Pass the action on to the active game element.
			activeView.handleAction(action);
		}
	}

	public static void main(String args[]) {
		new Driver();
	}
}
