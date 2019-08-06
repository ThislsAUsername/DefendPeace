package Engine;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Test.TestMain;
import UI.InputHandler;
import UI.MainUIController;
import UI.Art.SpriteArtist.SpriteEngine;

public class Driver implements ActionListener, KeyListener
{
  private static Driver gameDriver;
  private Stack<ControlState> gameStatus = new Stack<ControlState>();
  private GameViewProxy gameView;
  private JFrame gameWindow;

  private javax.swing.Timer repaintTimer;

  public GraphicsEngine gameGraphics = null;

  private Driver()
  {
    // At game startup, we are at the main menu. Set up controller/viewer
    MainUIController mc = new MainUIController();
    gameGraphics = new SpriteEngine(); // Choose graphics engine based on config file, etc?
    IView mv = gameGraphics.getMainUIView(mc);
    gameView = new GameViewProxy(mv);
    gameStatus.push(new ControlState(mc,mv));

    gameWindow = new JFrame();
    gameWindow.setTitle("Defend Peace " + new GameVersion());
    gameWindow.add(gameView);
    gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    gameWindow.addKeyListener(this);
    gameWindow.pack();
    gameWindow.setVisible(true);

    // Draw the screen at (ideally) 60fps.
    repaintTimer = new javax.swing.Timer(16, this);
    repaintTimer.start();

    gameView.init(); // This will enable the view proxy to start monitoring screen size changes.
  }

  /** Get the one and only Driver */
  public static Driver getInstance()
  {
    return gameDriver;
  }

  /**
   * Update the current state of the game to a new controller and game viewer.
   * The current controller/view are stored so that we can return to them once
   * the new controller determines it is completed.
   */
  // TODO: Should oldController/view be a stack?
  public void changeGameState(IController newControl, IView newView)
  {
    gameStatus.push(new ControlState(newControl, newView));
    // If the new View is valid (it may not be if we are returning to a
    // previously-stored state), then request it keep our screen dimensions.
    if( null != newView )
    {
      newView.setPreferredDimensions(gameView.getWidth(), gameView.getHeight());
      gameView.setView(newView);
      gameWindow.getContentPane().setSize(newView.getPreferredDimensions());
    }
    gameWindow.pack(); // Resize the window to match the new view's preferences.
  }

  public void updateView()
  {
    gameWindow.getContentPane().setSize(gameGraphics.getScreenDimensions());
    gameWindow.pack();
  }

  @Override // From ActionListener
  public void actionPerformed(ActionEvent arg0)
  {
    // Redraw the screen if needed.
    gameWindow.repaint();
  }

  public static void main(String args[])
  {
    // Run the test cases. If those all pass, launch the primary driver.
    if( !new TestMain().runTest() )
    {
      System.out.println("One or more tests failed!");
      System.exit(0);
    }

    gameDriver = new Driver();
  }

  @Override // From KeyListener
  public void keyPressed(KeyEvent arg0)
  {
    InputHandler.InputAction action = InputHandler.pressKey(arg0);

    if( action != InputHandler.InputAction.NO_ACTION )
    {
      // Pass the action on to the main game controller; if it says it's done, switch to previous state.
      if(gameStatus.peek().controller.handleInput(action))
      {
        gameStatus.pop(); // discard our current controller
        gameView.view.cleanup(); // Clean up our old view
        
        if( gameStatus.isEmpty() )
        {
          // We are done here. I would say clean up data or whatever, but this is Java.
          System.exit(0);
        }

        ControlState destination = gameStatus.pop(); // grab the old one
        
        // Reinstate the previous controller/view.
        changeGameState( destination.controller, destination.view );
      }
    }
  }

  @Override // From KeyListener
  public void keyReleased(KeyEvent arg0)
  {
    InputHandler.releaseKey(arg0);
  }

  @Override // From KeyListener
  public void keyTyped(KeyEvent arg0)
  {
    // Don't care.
  }

  /**
   * ControlState wraps an IController/IView pair for easy storage.
   */
  static class ControlState
  {
    public final IController controller;
    public final IView view;
    
    public ControlState(IController newControl, IView newView)
    {
      controller = newControl;
      view = newView;
    }
  }

  /**
   * GameViewProxy simply wraps the currently-active IView. This allows us to set a GameView into the
   * game's JFrame a single time and then not have to worry about managing components; we simply switch
   * out the renderer that the GameViewProxy/JPanel is using instead.
   */
  private static class GameViewProxy extends JPanel
  {
    private static final long serialVersionUID = 1L;

    private IView view;
    public GameViewProxy( IView v )
    {
      view = v;
    }

    public void init()
    {
      addComponentListener(new ComponentAdapter(){
        @Override
        public void componentResized(ComponentEvent evt)
        {
          view.setPreferredDimensions(evt.getComponent().getWidth(), evt.getComponent().getHeight());
        }
      });
    }

    public void setView( IView v )
    {
      view = v;
    }

    @Override // From JComponent
    public void paintComponent(Graphics g)
    {
      // Draw whatever should be drawn.
      view.render(g);
    }

    @Override // From JComponent
    public Dimension getPreferredSize()
    {
      return view.getPreferredDimensions();
    }
  }
}
