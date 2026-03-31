package org.lk.devkit.datastorage;

import lombok.Getter;
import org.lk.devkit.exception.DataContainerException;
import org.lk.devkit.filter.Filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The DataContainer class provides a standardized data container interface and supports multiple
 * data formats including CSV, XML, JSON, and YAML. It acts as a data structure primarily for
 * interacting with data from various sources like files, streams, or external inputs. The class
 * offers factory methods for creation and utility methods for data processing and manipulation.
 */
@Getter
public class DataContainer implements SpecificContainer {

    /**
     * Represents an instance of a specific container implementation within the
     * {@link DataContainer}. Facilitates the handling of data in different formats by adapting to a
     * specific container type. This instance is used to delegate operations related to reading,
     * writing, and data manipulation to the appropriate container implementation.
     */
    private SpecificContainer instance;

    /**
     * Represents the file path of the input data source. This variable holds a {@link Path} object
     * pointing to the file that serves as input for reading or processing operations. Typically
     * used to load and manipulate data from the specified file location within the context of a
     * container.
     */
    private Path inputFile;

    /**
     * Represents an {@link InputStream} that serves as the source of data for the container. It is
     * used to read data into the associated {@code DataContainer}. This stream acts as the primary
     * input mechanism for creating or populating the container with data in various formats such as
     * CSV, XML, JSON, or YAML.
     * <p>
     * The {@code inputStream} can be utilized by methods to process and parse data from the stream
     * based
     */
    private InputStream inputStream;

    /**
     * Represents a filter object used for defining specific conditions or criteria that may be
     * applied to data operations within the DataContainer. The filter is commonly utilized in
     * scenarios where selective data retrieval, modification, or deletion is required.
     * <p>
     * This field is initialized with a new instance of the Filter class and is accessible or
     * modifiable through the provided getter and setter methods.
     */
    private final Filter filter = new Filter();

    /**
     * Represents the format of the container used for data handling. The {@code containerFormat}
     * field is of type {@link EContainerFormat}, which enumerates supported data storage formats
     * such as CSV, XML, JSON, and YAML. This field determines the structure and behavior of the
     * data container.
     */
    private EContainerFormat containerFormat;

    /**
     * Creates and returns a new instance of the {@code DataContainer} class with default settings.
     *
     * @return A new {@code DataContainer} instance with default initialization.
     */
    public static DataContainer newContainer() {
        return new DataContainer();
    }


    /**
     * Private constructor for the DataContainer class. This constructor initializes the singleton
     * instance by calling the adaptContainer method. The method catches any IOException that occurs
     * during this process and wraps it in an UncheckedIOException, which is then thrown.
     * <p>
     * This constructor enforces the singleton pattern by ensuring that the class cannot be
     * instantiated directly from outside the class.
     * <p>
     * Throws: UncheckedIOException - if an IOException occurs while adapting the container.
     */
    private DataContainer() {
        try {
            instance = adaptContainer();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates a new instance of a DataContainer configured with the CSV container format.
     *
     * @return A new DataContainer instance configured to work with the CSV format.
     */
    public static DataContainer newCSVContainer() {
        return new DataContainer(EContainerFormat.CSV);
    }

    /**
     * Creates a new instance of DataContainer with the specified container format.
     *
     * @param type the container format type to initialize the DataContainer with. Supported types
     *             are defined in the EContainerFormat enumeration (e.g., CSV, XML, JSON, YAML).
     * @return a new instance of DataContainer initialized with the specified format type.
     */
    public static DataContainer newContainer(EContainerFormat type) {
        return new DataContainer(type);
    }

    /**
     * Constructor for the DataContainer class, used to create an instance based on the specified
     * container format. The format determines the underlying specific container implementation
     * (CSV, XML, JSON, YAML).
     *
     * @param type The desired container format, represented by the EContainerFormat enumeration.
     *             Supported formats include CSV, XML, JSON, and YAML. If an unsupported format is
     *             provided, an IllegalStateException is thrown.
     */
    private DataContainer(EContainerFormat type) {
        if(type == EContainerFormat.CSV) {
            containerFormat = EContainerFormat.CSV;
        } else if(type == EContainerFormat.XML) {
            containerFormat = EContainerFormat.XML;
        } else {
            throw new IllegalStateException("Unexpected value: " + type);
        }
        try {
            instance = adaptContainer();
        } catch (IOException e) {
            throw new DataContainerException(e);
        }
    }


    /**
     * Creates a new instance of DataContainer using the specified source path.
     *
     * @param sourcePath the file system path to the source to be used for initializing the
     *                   DataContainer
     * @return a new DataContainer instance initialized with the specified source path
     */
    public static DataContainer newContainer(String sourcePath) {
        return new DataContainer(Paths.get(sourcePath));
    }

    /**
     * Creates a new instance of the DataContainer class based on the specified source file.
     *
     * @param sourceFile the path to the source file from which the DataContainer will be created
     * @return a new DataContainer instance associated with the provided source
     */
    public static DataContainer newContainer(Path sourceFile) {
        return new DataContainer(sourceFile);
    }

    /**
     * Constructs a DataContainer object by initializing it with the specified source file.
     * Reads and processes the file if it exists, is a regular file, and is non-empty.
     *
     * @param sourceFile the path to the source file that will be used to read and initialize data
     * @throws DataContainerException if an IOException occurs while accessing or processing the file
     */
    private DataContainer(Path sourceFile) {
        inputFile = sourceFile;
        try {
            instance = adaptContainer();
            if (Files.exists(sourceFile) && Files.isRegularFile(sourceFile) && Files.size(sourceFile) > 0) {
                instance.readData(sourceFile);
            }
        } catch (IOException e) {
            throw new DataContainerException(e);
        }
    }

    /**
     * Creates a new instance of DataContainer using the provided InputStream.
     *
     * @param inStream the InputStream to be used for creating the DataContainer
     * @return a new instance of DataContainer initialized with the given InputStream
     */
    public static DataContainer newContainer(InputStream inStream) {
        return new DataContainer(inStream);
    }

    /**
     * Constructs a new DataContainer that initializes an internal instance
     * based on the provided InputStream and reads data from it.
     *
     * @param inStream the InputStream from which the data is read.
     *                 Must not be null.
     * @throws DataContainerException if an I/O error occurs while reading data
     *                                 or initializing the container instance.
     */
    private DataContainer(InputStream inStream) {
        inputStream = inStream;
        try {
            instance = adaptContainer();
            instance.readData(inputStream);
        } catch (IOException e) {
            throw new DataContainerException(e);
        }
    }

    /**
     * Adapts and returns a specific container instance based on the detected data format. The data
     * format is determined using the {@code detectDataFormat()} method.
     *
     * @return a specific container instance appropriate to the detected data format. This could be
     * an instance of CSVDataContainer, XMLDataContainer, JSONDataContainer, or YAMLDataContainer.
     * @throws IOException            if an I/O error occurs during data format detection or
     *                                container instantiation.
     * @throws DataContainerException if an error occurs while creating an XML container due to
     *                                issues such as parser configuration, invalid content, etc.
     */
    private SpecificContainer adaptContainer() throws IOException {
        if(detectDataFormat() == EContainerFormat.CSV) {
            return TabularContainer.newInstance();
        } else if(detectDataFormat() == EContainerFormat.XML) {
            return XMLDataContainer.newInstance();
        }
        throw new DataContainerException("Unsupported container format: " + containerFormat);
    }

    /**
     * Detects the data format of an input source, which can either be an input stream or an input file.
     * The method analyzes the content of the input stream or the file extension to determine the data format.
     * Supported formats include XML, JSON, YAML, and CSV.
     *
     * @return The detected data format as an {@link EContainerFormat}.
     * @throws IOException If an I/O error occurs while reading the input stream or accessing the input file.
     */
    private EContainerFormat detectDataFormat() throws IOException {
        if (inputFile != null) {
            String fileName = inputFile.toFile().getName();
            if (!fileName.isBlank()) {
                if (fileName.endsWith(".txt")) {
                    containerFormat = EContainerFormat.CSV;
                } else if (fileName.endsWith(".csv")) {
                    containerFormat = EContainerFormat.CSV;
                } else  if(fileName.endsWith(".xml")) {
                    containerFormat = EContainerFormat.XML;
                }
            }
        }
        return containerFormat;
    }

    /**
     * Retrieves the instance of the DataContainer as a CSVDataContainer if the underlying instance
     * is of type CSVDataContainer. If the instance is not initialized, a NullPointerException is
     * thrown.
     *
     * @return the instance of the DataContainer as a CSVDataContainer
     * @throws NullPointerException if the instance is not initialized or is not of type
     *                              CSVDataContainer
     */
    public TabularContainer tabInstance() {
        if (instance instanceof TabularContainer) {
            return (TabularContainer) instance;
        } else {
            throw new NullPointerException("TabularContainer not initialized");
        }
    }

    public XMLDataContainer xmlInstance() {
        if (instance instanceof XMLDataContainer) {
            return (XMLDataContainer) instance;
        } else {
            throw new NullPointerException("XMLDataContainer not initialized");
        }
    }

    /**
     * Determines if the current instance represents a tabular data structure.
     *
     * @return true if the instance is of type CSVDataContainer, otherwise false
     */
    public boolean isTabular() {
        return instance instanceof TabularContainer;
    }

    /**
     * Creates a new file in the file system at the location specified by the inputFile field.
     * <p>
     * If the inputFile field is not null, the method attempts to create a new file at the specified
     * path. If a file already exists at the path, an exception will be thrown.
     *
     * @throws IOException if an I/O error occurs or the file cannot be created.
     */
    public void createFile() throws IOException {
        if (inputFile != null) {
            Files.createFile(inputFile);
        }
    }

    // --------------------------------------------------------------------
    // Inherited methods that just link to the instance
    // --------------------------------------------------------------------

    /**
     * Provides the content of the current instance of the container as a string. Delegates the call
     * to the underlying instance implementation.
     *
     * @return the content of the container as a string.
     */
    @Override
    public String asString() {
        return instance.asString();
    }

    /**
     * Provides the content of the container as a string in the specified container format.
     *
     * @param format the container format in which the content should be converted. Supported
     *               formats are defined in {@code EContainerFormat}.
     * @return the content of the container as a string in the specified format.
     */
    @Override
    public String asString(EContainerFormat format) {
        return instance.asString(format);
    }

    /**
     * Reads data from the specified file path.
     *
     * @param inputFile the path to the input file from which data is to be read
     * @throws IOException if an I/O error occurs while reading the file
     */
    @Override
    public void readData(Path inputFile) throws IOException {
        instance.readData(inputFile);
    }

    /**
     * Reads data from the provided input stream and processes it.
     *
     * @param stream the input stream from which data is to be read
     * @throws IOException if an I/O error occurs during reading
     */
    @Override
    public void readData(InputStream stream) throws IOException {
        instance.readData(stream);
    }

    /**
     * Writes data from the current instance to the specified file path.
     *
     * @param outputFile the path to the output file where
     */
    @Override
    public void writeData(Path outputFile) throws IOException {
        instance.writeData(outputFile);
    }

}
