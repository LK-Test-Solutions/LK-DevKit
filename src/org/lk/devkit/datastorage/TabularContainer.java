package org.lk.devkit.datastorage;

import lombok.Getter;
import lombok.Setter;
import org.lk.devkit.exception.DataContainerException;
import org.lk.devkit.filter.Filter;
import org.lk.devkit.filter.FilterRule;
import org.lk.devkit.util.CSVUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * A container class to manage CSV data, including headers and rows.
 * This class provides methods for reading, writing, modifying, and querying CSV data,
 * with support for filters, custom delimiters, and row/column manipulations.
 *
 * @author FME (LK Test Solutions)
 */
@Getter
public class TabularContainer implements SpecificContainer {

    /**
     * Represents a collection of rows where each row is an array of strings.
     * Used to store tabular data or structured information.
     */
    @Setter
    private List<String[]> rows;

    /**
     * Represents an array of headers used for various purposes such as HTTP requests
     * or processing structured data. Each element in the array corresponds to
     * a specific header value.
     */
    private String[] headers;

    /**
     * A map that associates header names with their respective integer values.
     * This can be used to map and retrieve specific header-related data based on header names as keys.
     */
    private final Map<String, Integer> headerMap;

    /**
     * Represents the delimiter used to separate elements in a string.
     * The default value is set to a semicolon (";").
     */
    @Setter
    private String delimiter = ";";

    /**
     * Creates a new instance of the CSVDataContainer.
     *
     * @return a new CSVDataContainer instance.
     */
    public static TabularContainer newInstance() {
        return new TabularContainer();
    }

    /**
     * Constructs a new instance of {@code CSVDataContainer}.
     * This constructor initializes an empty list of rows, an empty array for headers,
     * and an empty map for the header mapping. It is protected to restrict instantiation
     * to the containing class or subclasses.
     */
    protected TabularContainer() {
        rows = new ArrayList<>();
        headers = new String[0];
        headerMap = new HashMap<>();
    }

    /**
     * Converts the data contained in the CSVDataContainer into a String representation
     * that includes both the headers and the rows, formatted as a CSV.
     *
     * @return a String representation of the CSV data, including headers and rows,
     *         formatted according to the CSV standard.
     */
    @Override
    public String asString() {
       return CSVUtil.asString(getRowsWithHeader());
    }

    /**
     * Reads data from the specified source file. The method processes the input
     * CSV file, extracting headers and rows of data while mapping header names
     * to their respective column indices.
     *
     * @param sourceFile The path to the input file containing the CSV data to be read.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    @Override
    public void readData(Path sourceFile) throws IOException {
        rows = CSVUtil.readFile(sourceFile.toFile(), delimiter, StandardCharsets.UTF_8);
        headers = rows.get(0);
        rows.remove(0); // Headers are stored and can be removed from the rows list
        for(int i = 0; i < headers.length; i++) {
            headerMap.put(headers[i], i);
        }
    }

    /**
     * Reads data from the provided InputStream and processes it into rows and headers
     * based on the specified delimiter. The contents of the InputStream are parsed
     * and stored as a list of rows, where each row is an array of strings. The first
     * row is set as the headers of the data, and subsequent rows represent the data
     * entries.
     *
     * @param stream The InputStream from which data is to be read. The stream should
     *               contain delimited text data to be parsed and stored. If the stream
     *               is null or empty, no data will be processed.
     * @throws IOException If an I/O error occurs while reading from the InputStream.
     */
    @Override
    public void readData(InputStream stream) throws IOException {
        rows = new ArrayList<>();
        List<String> content = null;
        if (stream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            Stream<String> streamOfString = new BufferedReader(inputStreamReader).lines();
            content = new ArrayList<>();
            streamOfString.forEach(content::add);

            streamOfString.close();
            inputStreamReader.close();
        }
        if(content != null) {
            for(String row : content) {
                rows.add(row.split(delimiter));
            }
            headers = rows.get(0);
            rows.remove(0);
        }
    }

    /**
     * Writes the data represented by this CSVDataContainer instance, including headers and rows,
     * to the specified output file with the defined delimiter and UTF-8 character encoding.
     *
     * @param outputFile The path to the output file where the data will be written.
     * @throws IOException If an I/O error occurs while writing the file.
     */
    @Override
    public void writeData(Path outputFile) throws IOException {
        CSVUtil.writeFile(getRowsWithHeader(), outputFile, delimiter, StandardCharsets.UTF_8);
    }

    /**
     * Retrieves a row from the data container based on the provided index.
     * If the row index is out of range, a {@link DataContainerException} is thrown.
     *
     * @param rowIndex the index of the row to retrieve, starting from 0
     * @return an array of strings representing the data in the specified row,
     *         or null if the container is empty
     * @throws DataContainerException if the row index is out of range
     */
    public String[] getRow(int rowIndex) {
        if (rows.isEmpty()) {
            return new String[0];
        }
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            return rows.get(rowIndex);
        }
        throw new DataContainerException("Row index is out of range");
    }

    /**
     * Retrieves the first row from the dataset that matches the specified filter criteria.
     *
     * @param filter The filter object defining the conditions to match rows in the dataset.
     * @return A String array representing the matching row, or null if no row matches the filter
     *         or if the dataset is empty.
     */
    public String[] getRow(Filter filter) {
        if (rows.isEmpty()) {
            return new String[0];
        }
        for(String[] row : rows) {
            if(checkValuesFilter(row, filter)) {
                return row;
            }
        }
        return null;
    }

    /**
     * Retrieves rows from the data container that match the criteria specified by the provided filter.
     *
     * @param filter The filter object containing criteria that each row must match to be included in the result.
     * @return A list of String arrays representing the rows that match the filter criteria.
     *         Returns null if the data container does not contain any rows.
     */
    public List<String[]> getRows(Filter filter) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<String[]> ret = new ArrayList<>();
        for(String[] row : rows) {
            if(checkValuesFilter(row, filter)) {
                ret.add(row);
            }
        }
        return ret;
    }
    
    public List<String[]> getRows(String[] outHeaders, Filter filter) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<String[]> ret = new ArrayList<>();
        for(String[] row : rows) {
            if(checkValuesFilter(row, filter)) {
            	String[] outRow = Collections.nCopies(outHeaders.length, "").toArray(String[]::new);
            	int outHeaderIndex = 0; // Header index in the output list
            	for(String header : outHeaders) {
            		int headerIndex = headerMap.get(header); // Header index in all rows
            		outRow[outHeaderIndex] = row[headerIndex];
            		outHeaderIndex++;
            	}
                ret.add(outRow);
            }
        }
        return ret;
    }
    
    public String getValue(String columnHeader, Filter filter) {
    	return getValue(0, columnHeader, filter);
    }
    
    public String getValue(int rowIndex, String columnHeader, Filter filter) {
    	// Like getValue(int,String) but with a filtered row
    	List<String[]> filtered = getRows(filter);
    	int columnIndex;
        try {
            columnIndex = headerMap.get(columnHeader);
        } catch (NullPointerException e) {
            throw new DataContainerException("Column '" + columnHeader + "' not found.");
        }
        if (rowIndex >= 0 && rowIndex < filtered.size()) {
            return filtered.get(rowIndex)[columnIndex];
        }
        throw new DataContainerException("Row index is out of range");
    }

    /**
     * Like {@link #getValue(int, String)} for index 0.
     */
    public String getValue(String columnHeader) {
        return getValue(0, columnHeader);
    }

    /**
     * Retrieves the value from a specific row and column in the data container.
     *
     * @param rowIndex The index of the row to retrieve the value from. Must be a valid index within the range of available rows.
     * @param columnHeader The header name of the column to retrieve the value from. Must correspond to an existing column.
     * @return The value located at the specified row and column. Returns null if the rows are empty.
     * @throws DataContainerException If the column header is not found or the row index is out of range.
     */
    public String getValue(int rowIndex, String columnHeader) {
        if (rows.isEmpty()) {
            return "";
        }
        int columnIndex;
        try {
            columnIndex = headerMap.get(columnHeader);
        } catch (NullPointerException e) {
            throw new DataContainerException("Column '" + columnHeader + "' not found.");
        }
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            return rows.get(rowIndex)[columnIndex];
        }
        throw new DataContainerException("Row index is out of range");
    }

    /**
     * Retrieves a list of values from a specified column in a data structure based on the column header.
     * If the rows are empty, the method returns null.
     *
     * @param columnHeader The header of the column to retrieve values from.
     * @return A list of strings containing the values of the specified column,
     *         or null if the rows are empty.
     */
    public List<String> getColumn(String columnHeader) {
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        rows.add(0, headers);
        List<String> ret = CSVUtil.getColumn(rows, columnHeader);
        rows.remove(0);
        if(!ret.isEmpty()) {
        	ret.remove(0); // Without header
        }
        return ret;
    }

    /**
     * Retrieves all values from a specified column in the dataset that satisfy the given filter.
     * The returned column values do not include the header.
     *
     * @param columnHeader the name of the column to retrieve values from
     * @param filter an object of type Filter that specifies the criteria for selecting rows
     * @return a list of strings containing the values from the specified column, filtered by the given criteria
     */
    public List<String> getColumn(String columnHeader, Filter filter) {
        List<String[]> filteredRows = getRows(filter);
        if(filteredRows == null || filteredRows.isEmpty()) {
        	return new ArrayList<>();
        }
        filteredRows.add(0, headers);
        List<String> ret = CSVUtil.getColumn(filteredRows, columnHeader);
        ret.remove(0);
        return ret;
    }
    
    public List<String> getColumn(String columnHeader, String filterColumn, String filterValue) {
    	rows.add(0, headers);
        List<String[]> filteredRows = CSVUtil.filterData(rows, filterColumn, filterValue);
        rows.remove(0);
        if(filteredRows == null) {
        	return null;
        }
        filteredRows.add(headers);
        List<String> ret = CSVUtil.getColumn(filteredRows, columnHeader);
        ret.remove(0);
        return ret;
    }

    /**
     * Adds a new row to the CSV data container. The provided row is converted
     * into an array of strings before being added to the internal storage.
     *
     * @param row A list of strings representing the row to be added.
     */
    public void addRow(List<String> row) {
        rows.add(row.toArray(String[]::new));
    }

    /**
     * Adds a new row to the data container.
     *
     * @param row an array of strings representing the values of the new row to be added. Each element corresponds to a column value.
     */
    public void addRow(String[] row) {
        rows.add(row);
    }

    /**
     * Adds a new column to the CSV data structure with the given column name.
     * The column is added without using an existing column configuration.
     *
     * @param column the name of the new column to be added
     */
    public void addColumn(String column) {
        addColumn(column, false);
    }

    /**
     * Adds a new column to the existing dataset with the specified header name.
     * If a column with the same name already exists, behavior is controlled by the
     * {@code useExisting} parameter.
     *
     * @param column The name of the column to be added.
     * @param useExisting A boolean flag indicating whether to use the existing
     *                    column (if it already exists) instead of creating a new one.
     */
    public void addColumn(String column, boolean useExisting) {
        rows.add(0, headers);
        CSVUtil.addColumn(rows, headerMap, column, useExisting);
        rows.remove(0);
        mergeHeaderMapWithHeader();
    }

    /**
     * Updates the value of a specific column in the dataset by replacing all occurrences
     * of an old value with a new value.
     *
     * @param updateColumn The name of the column where the value needs to be updated.
     * @param oldValue The value in the column that needs to be replaced.
     * @param newValue The new value to replace the old value in the specified column.
     */
    public void setValue(String updateColumn, String oldValue, String newValue) {
        rows.add(0, headers);
        CSVUtil.updateRow(rows, updateColumn, oldValue, newValue);
        rows.remove(0);
    }

    /**
     * Sets the value at a specific row index in the specified column.
     *
     * @param updateColumn The name of the column where the value needs to be updated.
     * @param index The row index (0-based) where the value should be set.
     * @param newValue The new value to set at the specified row and column.
     * @throws DataContainerException If the column header is not found or the row index is out of range.
     */
    public void setValue(String updateColumn, int index, String newValue) {
        rows.add(0,headers);
        CSVUtil.updateRow(rows, updateColumn, index, newValue);
        rows.remove(0);
    }

    /**
     * Updates a row in the data container matching the specified filter with the provided new row data.
     *
     * @param updateRow The new row data to replace the existing row. The length of this array must match the length of the existing row being updated.
     * @param filter The filter criteria used to identify the row to be updated.
     * @throws DataContainerException If the length of updateRow does not match the length of the row being updated.
     */
    public void setRow(String[] updateRow, Filter filter) {
        rows.add(0, headers);
        String[] targetRow = getRow(filter);
        if(targetRow.length != updateRow.length) {
            throw new DataContainerException("Old row and new row do not have the same length in setRow");
        }
        for(int i = 0; i < targetRow.length; i++) {
            CSVUtil.updateRow(rows, headers[i], targetRow[i], updateRow[i]);
        }
        rows.remove(0);
    }

    /**
     * Updates a row in the data container matching the specified index with the provided new row data.
     *
     * @param index The index used to identify the row to be updated.
     * @param updateRow The new row data to replace the existing row. The length of this array must match the length of the existing row being updated.
     * @throws DataContainerException If the length of updateRow does not match the length of the row being updated.
     */
    public void setRow(int index, String[] updateRow) {
        rows.add(0, headers);
        String[] targetRow = getRow(index);
        if(targetRow.length != updateRow.length) {
            throw new DataContainerException("Old row and new row do not have the same length in setRow");
        }
        for(int i = 0; i < targetRow.length; i++) {
            CSVUtil.updateRow(rows, headers[i], targetRow[i], updateRow[i]);
        }
        rows.remove(0);
    }

    /**
     * Deletes a specific value from a column identified by its header name by replacing
     * the value with an empty string.
     *
     * @param headerName the name of the column header where the value resides
     * @param value the value to be deleted from the specified column
     */
    public void deleteValue(String headerName, String value) {
        setValue(headerName, value, "");
    }

    /**
     * Deletes a row from the dataset at the specified index.
     *
     * @param index the zero-based index of the row to be removed
     */
    public void deleteRow(int index) {
        rows.remove(index);
    }

    /**
     * Deletes multiple rows from the data container that match the specified filter criteria.
     *
     * @param filter The {@link Filter} object containing the conditions to identify the rows to be deleted.
     */
    public void deleteRows(Filter filter) {
        int[] indexes = getRowsIndexes(filter);
        for (int index : indexes) {
            rows.remove(index);
        }
    }

    /**
     * Generates a list of rows including the header row as the first entry.
     *
     * @return A list of String arrays where the first entry is the header row and the following entries are the data rows.
     */
    private List<String[]> getRowsWithHeader() {
        List<String[]> ret = new ArrayList<>();
        ret.add(headers);
        ret.addAll(rows);
        return ret;
    }

    /**
     * Merges the header map with the header array, positioning headers in their respective
     * indices as specified in the headerMap. The resulting array, `headers`, will have null
     * placeholders for indices that are not mapped in the headerMap.
     * <p>
     * The method determines the size of the resulting header array based on the maximum
     * index value in the headerMap, ensures the created array has sufficient capacity,
     * and places header values at their corresponding indices.
     * <p>
     * Assumes that the `headerMap` field is a non-null Map where the key represents the
     * header name and the value represents the index position of that header.
     */
    private void mergeHeaderMapWithHeader() {
        int maxIndex = Collections.max(headerMap.values());
        headers = new ArrayList<>(Collections.nCopies(maxIndex + 1, "")).toArray(String[]::new);
        // Place headers at the correct position
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            headers[entry.getValue()] = entry.getKey();
        }

    }

    /**
     * Retrieves the indexes of the rows that match the specified filter criteria.
     *
     * @param filter the filter object containing the rules to determine matching rows
     * @return an array of integers representing the indexes of the rows that match the filter criteria.
     *         If no row matches the filter, returns an empty array.
     */
    public int[] getRowsIndexes(Filter filter) {
        StringBuilder indexBuffer = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (checkValuesFilter(rows.get(i), filter)) {
                if (!indexBuffer.isEmpty()) {
                    indexBuffer.append(";");
                }
                indexBuffer.append(i);
            }
        }
        if (!indexBuffer.isEmpty()) {
            return Arrays.stream(indexBuffer.toString().split(";")).mapToInt(Integer::parseInt).toArray();
        } else {
            return new int[] {};
        }
    }

    /**
     * Checks if the filter rules match to the values of the given data set.
     *
     * @param values String array with all values of a defined data set (row).
     * @param filter   Object of type {@link Filter}, which includes one or more filter rules
     * @return true = values match to the filter; false = values don't match to the filter
     */
    private boolean checkValuesFilter(String[] values, Filter filter)  {
        boolean returnCode = false;
        if (filter.getFilterRules().isEmpty()) {
            returnCode = true; // No filter defined
        } else {
            for (FilterRule fr : filter.getFilterRules()) {
                // Wild cards * and % will accept any value
                if (fr.getValue() != null) {
                    if ((fr.getValue().equals("*")) || (fr.getValue().equals("%"))) {
                        returnCode = true;
                        break;
                    }
                }
                // check values against the filter rules
                try {
                    returnCode = fr.checkValue(values[headerMap.get(fr.getHeaderName())]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    // Empty value in this row
                    continue;
                }
                if (!returnCode) {
                    // skip check and return false, in case that one of the rules fails
                    break;
                }
            }
        }
        return returnCode;
    }
    
    public void initHeaders(String[] headers) {
		this.headers = headers;
		int headerIndex = 0;
		for(String header : headers) {
			headerMap.put(header, headerIndex);
			headerIndex++;
		}
	}

    public void setHeaders(String[] headers) {
        if(headerMap.isEmpty()) {
            initHeaders(headers);
        }
        this.headers = headers;
    }

    public int getRowCount() {
        return rows.size();
    }
}