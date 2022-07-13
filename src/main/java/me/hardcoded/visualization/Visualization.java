package me.hardcoded.visualization;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.util.Objects;

import javax.swing.*;

/**
 * This class is used for the visualization api
 *
 * @author HardCoded
 */
public abstract class Visualization {
	protected final VisualizationHandler handler;
	protected final ColorCache colorCache;
	private FontRenderContext frc;
	private BufferStrategy bs;
	protected JFrame frame;
	protected JMenuBar menuBar;
	
	private boolean hasSetup;
	// Used to store early objects that was displayed before the frame had fully loaded
	private Object earlyObject;
	
	protected Visualization(String name, VisualizationHandler handler) {
		this(name, handler, 2);
	}
	
	protected Visualization(String defaultTitle, VisualizationHandler handler, int buffers) {
		this.handler = Objects.requireNonNull(handler);
		this.colorCache = Objects.requireNonNull(handler.getColorCache());
		
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			frame = new JFrame(defaultTitle) {
				@Override
				public void paint(Graphics g) {
					if (bs == null) {
						createBufferStrategy(buffers);
						bs = getBufferStrategy();
					}
					super.paint(g);
				}
			};
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setup();
			
			menuBar = new JMenuBar();
			menuBar.setBorderPainted(false);
			setupMenu(menuBar);
			frame.setJMenuBar(menuBar);
			frame.setIconImage(getIcon());
			
			synchronized (this) {
				hasSetup = true;
				
				// If we had an object displayed before we initialized the frame
				// we will now show that object and make sure it is unloaded
				if (earlyObject != null) {
					showObject(earlyObject);
					earlyObject = null;
				}
			}
		});
	}
	
	protected final Rectangle2D getStringBounds(Font font, String text) {
		if (frc == null) {
			frc = new FontRenderContext(new AffineTransform(), true,true);
		}
		
		return font.getStringBounds(text, frc);
	}
	
	/**
	 * Returns the icon of the visualization
	 */
	protected abstract Image getIcon();
	
	/**
	 * This method is called after the window objects has been created
	 */
	protected abstract void setup();
	
	/**
	 * This method is called when the menu object should be created
	 */
	protected abstract void setupMenu(JMenuBar menuBar);
	
	/**
	 * Display the object with this visualization
	 */
	public synchronized final void show(Object value) {
		if (hasSetup) {
			showObject(value);
		} else {
			earlyObject = value;
		}
	}
	
	/**
	 * Hide this visualization
	 */
	public final void hide() {
		frame.setVisible(false);
	}
	
	/**
	 * This is called when the visualization shows an object
	 */
	protected abstract void showObject(Object value);
}
