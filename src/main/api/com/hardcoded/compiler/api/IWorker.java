package com.hardcoded.compiler.api;

import com.hardcoded.options.Options;

/**
 * A worker utility interface for processing data
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public interface IWorker<T, E> {
	T process(Options options, E value);
}
