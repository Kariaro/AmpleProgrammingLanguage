package me.hardcoded.compiler.context;

import me.hardcoded.configuration.CompilerConfiguration;
import me.hardcoded.visualization.VisualizationHandler;

public class AmpleConfig {
	private final CompilerConfiguration configuration;
	private final VisualizationHandler visualizationHandler;
	
	public AmpleConfig(CompilerConfiguration configuration) {
		this.configuration = configuration;
		this.visualizationHandler = new VisualizationHandler();
	}
	
	public CompilerConfiguration getConfiguration() {
		return configuration;
	}
	
	public VisualizationHandler getVisualizationHandler() {
		return visualizationHandler;
	}
}
