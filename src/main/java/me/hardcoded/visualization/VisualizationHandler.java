package me.hardcoded.visualization;

import java.util.*;
import java.util.function.Function;

import javax.swing.*;

/**
 * This class is used for the visualization api
 *
 * @author HardCoded
 */
public class VisualizationHandler {
	private final List<Visualization> list;
	private Map<VisSupplier, Object> suppliers;
	private boolean hasSetup;
	
	public VisualizationHandler() {
		this.suppliers = new LinkedHashMap<>();
		this.list = new ArrayList<>();
		
		SwingUtilities.invokeLater(this::setup);
	}
	
	public synchronized VisualizationHandler addVisualization(VisSupplier supplier, Object value) {
		// If we haven't set up the visualization yet we cache them
		if (!hasSetup) {
			suppliers.put(supplier, value);
		} else {
			addInternalVisualization(supplier, value);
		}
		
		return this;
	}
	
	/**
	 * Setup will only be called from the Swing thread
	 */
	private synchronized void setup() {
		if (!hasSetup) {
			for (Map.Entry<VisSupplier, Object> entry : suppliers.entrySet()) {
				addInternalVisualization(entry.getKey(), entry.getValue());
			}
			
			// Clear the suppliers list
			suppliers.clear();
			suppliers = null;
			hasSetup = true;
			
			// Setup all visualizations
		}
	}
	
	private void addInternalVisualization(VisSupplier supplier, Object value) {
		Visualization vis = supplier.apply(this);
		list.add(vis);
		vis.show(value);
		
		if (value != null) {
			vis.show(value);
		}
	}
	
	/**
	 * Fire a visualization event
	 *
	 * @param event the event to fire
	 */
	protected void fireEvent(VisualizationEvent event) {
		synchronized (list) {
			try {
				list.forEach(vis -> {
					if (vis instanceof VisualizationListener listener) {
						switch (event.getType()) {
							case SELECTION_EVENT ->
								listener.handleSelection((VisualizationEvent.SelectionEvent) event);
							case SYNTAX_SELECTION_EVENT ->
								listener.handleSyntaxSelection((VisualizationEvent.SyntaxSelectionEvent) event);
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public interface VisSupplier extends Function<VisualizationHandler, Visualization> {
	
	}
}
