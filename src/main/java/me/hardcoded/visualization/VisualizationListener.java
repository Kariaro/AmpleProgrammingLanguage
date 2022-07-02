package me.hardcoded.visualization;

import me.hardcoded.visualization.VisualizationEvent.*;

/**
 * Visualization listener class
 *
 * @author HardCoded
 */
public interface VisualizationListener {
	/**
	 * Called when a visualization selects a position
	 * @param event the event
	 */
	void handleSelection(SelectionEvent event);
}
