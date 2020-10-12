package hardcoded.visualization;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import javax.swing.JComponent;
import javax.swing.JFrame;

public abstract class Visualization {
	public static final Visualization DUMMY = new Visualization("null") {
		public void show(Object... parameters) {}
		public void hide() {}
	};
	
	protected BufferStrategy bs;
	protected JFrame frame;
	
	protected Visualization(String name) {
		this(name, 2);
	}
	
	protected Visualization(String title, int buffers) {
		// This allows for repaints directly from a bufferStrategy
		frame = new JFrame() {
			private static final long serialVersionUID = 1L;
			
			@Override public void paint(Graphics g) {
				if(bs == null) {
					bs = getBufferStrategy();
					createBufferStrategy(buffers);
				}
				super.paint(g);
			}
		};
		frame.setTitle(title);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/**
	 * Shows the visualization with the specified arguments.
	 * @param parameters
	 */
	public abstract void show(Object... parameters);
	
	/**
	 * Hides the visualization.
	 */
	public abstract void hide();
	
	public final JComponent getComponent() {
		return frame.getRootPane();
	}
}
