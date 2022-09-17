package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This class can interpret ample scripts
 */
public class AmpleInterpreter {
	private static final Logger LOGGER = LogManager.getLogger(AmpleInterpreter.class);
	private final AmpleRunner runner;
	private Thread thread;
	
	public AmpleInterpreter() {
		this.runner = new AmpleRunner();
	}
	
	public boolean isRunning() {
		return thread != null;
	}
	
	public synchronized void runBlocking(IntermediateFile code) throws InterruptedException {
		if (this.thread != null) {
			throw new IllegalStateException("The interpreter is already running");
		}
		
		// Start the interpreter thread
		Thread thread = createThread(code, () -> this.thread = null);
		thread.start();
		thread.join();
	}
	
	public synchronized void runAsync(IntermediateFile code) {
		if (this.thread != null) {
			throw new IllegalStateException("The interpreter is already running");
		}
		
		// Start the interpreter thread
		Thread thread = createThread(code, () -> this.thread = null);
		thread.start();
	}
	
	/**
	 * Create an interpreter thread
	 */
	private Thread createThread(IntermediateFile code, Runnable callback) {
		Thread thread = new Thread(() -> {
			try {
				// Run the code
				runner.run(code);
			} catch (Throwable t) {
				LOGGER.error("", t);
			} finally {
				callback.run();
			}
		}, "Ample Interpreter");
		thread.setDaemon(true);
		return thread;
	}
}
