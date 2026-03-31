package org.lk.devkit.datastorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Wrapper class for XML data processing using Jackson XML.
 *
 * @author FME
 */
// TODO test functionality
@Getter
public class XMLDataContainer implements SpecificContainer {

    private final XmlMapper xmlMapper;
    private JsonNode rootNode;

    private XMLDataContainer() {
        this.xmlMapper = new XmlMapper();
    }

    /**
     * Creates a new XMLDataContainer instance.
     */
    public static XMLDataContainer newInstance() {
        return new XMLDataContainer();
    }

    @Override
    public void readData(Path sourceFile) throws IOException {
        rootNode = xmlMapper.readTree(sourceFile.toFile());
    }

    @Override
    public void readData(InputStream inputStream) throws IOException {
        rootNode = xmlMapper.readTree(inputStream);
    }

    /**
     * Creates a new XMLDataContainer from an XML string.
     *
     * @param xmlContent XML content as string
     * @throws IOException if parsing fails
     */
    public void fromString(String xmlContent) throws IOException {
        rootNode = xmlMapper.readTree(xmlContent);
    }

    @Override
    public void writeData(Path outputFile) throws IOException {
        xmlMapper.writeValue(outputFile.toFile(), rootNode);
    }

    /**
     * Gets a value from the XML by path (e.g., "BODY/ARG/YCSDBWS/RESULT").
     *
     * @param path path to the element, separated by "/"
     * @return value as string, or null if not found
     */
    public String getValue(String path) {
        if (rootNode == null) {
            return null;
        }

        String[] parts = path.split("/");
        JsonNode currentNode = rootNode;

        for (String part : parts) {
            if (currentNode == null) {
                return null;
            }
            currentNode = currentNode.get(part);
        }

        return currentNode != null ? currentNode.asText() : null;
    }

    /**
     * Gets a child node by path.
     *
     * @param path path to the element, separated by "/"
     * @return JsonNode or null if not found
     */
    public JsonNode getNode(String path) {
        if (rootNode == null) {
            return null;
        }

        String[] parts = path.split("/");
        JsonNode currentNode = rootNode;

        for (String part : parts) {
            if (currentNode == null) {
                return null;
            }
            currentNode = currentNode.get(part);
        }

        return currentNode;
    }

    /**
     * Checks if the container has data.
     *
     * @return true if rootNode is not null
     */
    public boolean hasData() {
        return rootNode != null;
    }

    @Override
    public String asString() {
        try {
            return xmlMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

}

