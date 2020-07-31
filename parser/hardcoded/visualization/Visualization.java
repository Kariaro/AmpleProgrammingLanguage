package hardcoded.visualization;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import javax.swing.JComponent;
import javax.swing.JFrame;

public abstract class Visualization {
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
	 * Show this visualization the specified arguments.
	 * @param args
	 */
	public abstract void show(Object... args);
	
	public final JComponent getComponent() {
		return frame.getRootPane();
	}
}
