package me.hardcoded.visualization;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import javax.swing.*;

/**
 * This class is used for the visualization api
 *
 * @author HardCoded
 */
public abstract class Visualization<T> {
	public static final Visualization<Object> DUMMY = new Visualization<>("null") {
		@Override
		protected void setup() {}
		
		@Override
		protected void showObject(Object value) {}
		
		@Override
		public void hide() {}
	};
	
	protected BufferStrategy bs;
	protected JFrame frame;
	
	private boolean hasSetup;
	// Used to store early objects that was displayed before the frame had fully loaded
	private T earlyObject;
	
	protected Visualization(String name) {
		this(name, 2);
	}
	
	protected Visualization(String defaultTitle, int buffers) {
		SwingUtilities.invokeLater(() -> {
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
	
	/**
	 * This method is called after the window objects has been created
	 */
	protected abstract void setup();
	
	/**
	 * Display the object with this visualization
	 */
	public synchronized final void show(T value) {
		if (hasSetup) {
			showObject(value);
		} else {
			earlyObject = value;
		}
	}
	
	/**
	 * This is called when the visualization shows an object
	 */
	protected abstract void showObject(T value);
	
	public abstract void hide();
}
