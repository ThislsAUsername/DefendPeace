package Engine;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import CommandingOfficers.CmdrStrong;
import CommandingOfficers.Commander;
import Terrain.GameMap;
import UI.InputHandler;
import UI.MapView;
import UI.Art.SpriteArtist.SpriteMapView;

public class Driver implements ActionListener{

	private static final long serialVersionUID = 1L;

	JFrame gameWindow;
	private javax.swing.Timer repaintTimer;
	InputHandler inputHandler;

	// TODO: make this a GameView or some such when we get there.
	private MapController activeController;

	public Driver()
	{
		Commander co1 = new CmdrStrong();
		Commander co2 = new Commander();
		Commander[] cos = {co1, co2};
		// TODO: Remove this and/or make it actually good.
//		for (int i = 0; i < commanders.length; i++) {
//			commanders[i].myColor = new Color(i*100,i*100,i*100);
//		}
		cos[0].myColor = Color.pink;
		cos[1].myColor = Color.cyan;
		GameMap map = new GameMap(cos);
		GameInstance newGame = new GameInstance(map, cos);

		MapView mapView = new SpriteMapView(newGame);
		MapController mapController = new MapController(newGame, mapView);

		activeController = mapController;

		gameWindow = new JFrame();
		gameWindow.add(mapView);
		gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameWindow.addKeyListener(new InputHandler(this));
		gameWindow.pack();
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
			activeController.handleInput(action);
		}
	}

	public static void main(String args[]) {
	  // Run the test cases. If those all pass, launch the primary driver.
	  if(!TestDriver.performTests())
	  {
      System.out.println("One or more tests failed!");
      System.exit(0);
	  }

    new Driver();
	}
}
