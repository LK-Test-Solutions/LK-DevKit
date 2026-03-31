package org.lk.devkit.datastorage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Defines the structure of the specific {@link DataContainer} classes.
 * 
 * @author FME (LK Test Solutions)
 */
public interface SpecificContainer {
	
	/**
	 * Used to provide the content of the container as string.
	 * 
	 * @return the container content as string for further operations.
	 */
	String asString();
	
	/**
	 * Used to provide the content of the container in another container format as string.
	 * 
	 * @param newFormat see {@link EContainerFormat}
	 * @return the container content as string for further operations in the chosen format
	 */
	default String asString(EContainerFormat newFormat) {
		return asString();
	}

	/**
	 * Used to read data from file sources.
	 */
	void readData(Path sourceFile) throws IOException;

	/**
	 * Used to read data from stream sources.
	 */
	void readData(InputStream stream) throws IOException;

	/**
	 * Used to write data into file.
	 */
	void writeData(Path outputFile) throws IOException;

}