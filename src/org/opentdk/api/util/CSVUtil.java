package org.opentdk.api.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Utility class that provides methods for working with CSV-like tabular data.
 */
public class CSVUtil {

	/**
	 * Reads the content of a file and parses each line into an array of strings
	 * using the specified delimiter. The parsed data is returned as a list of string arrays.
	 *
	 * @param filePath the file to be read
	 * @param delimiter the delimiter used to split each line into an array of strings
	 * @param encoding the character encoding used to read the file
	 * @return a list of string arrays, where each array represents a line in the file split by the delimiter
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	public static List<String[]> readFile(File filePath, String delimiter, Charset encoding) throws IOException {
		List<String[]> data = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(filePath, encoding))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(delimiter);
				data.add(values);
			}
		}
		return data;
	}

	/**
	 * Filters the provided tabular data based on a specific column and a filter value.
	 * The method returns rows where the value in the specified column matches the given filter value.
	 * The first row in the data is treated as the header row.
	 *
	 * @param data the list of string arrays representing tabular data where each array is a row
	 * @param columnName the name of the column to filter data by
	 * @param filterValue the value to filter rows by in the specified column
	 * @return a list of string arrays containing only the rows that match the filter value in the specified column
	 */
	public static List<String[]> filterData(List<String[]> data, String columnName, String filterValue) {
		List<String[]> filteredData = new ArrayList<>();

		// Find the index in the column
		int columnIndex = -1;
		if (!data.isEmpty()) {
			String[] header = data.get(0);
			for (int i = 0; i < header.length; i++) {
				if (header[i].equals(columnName)) {
					columnIndex = i;
					break;
				}
			}
		}

		// Filter the data based on the value in the committed column
		if (columnIndex != -1) {
			for (int i = 1; i < data.size(); i++) { // Start index is 1 to jump over the header row
				String[] row = data.get(i);
				if (row.length > columnIndex && row[columnIndex].equals(filterValue)) {
					filteredData.add(row);
				}
			}
		}
		return filteredData;
	}

	/**
	 * Retrieves a single row from the specified data set based on the provided row index.
	 *
	 * @param data the list of string arrays representing tabular data where each array is a row
	 * @param rowIndex the index of the row to be retrieved
	 * @return a list of strings representing the data in the specified row; returns an empty list if the rowIndex is out of bounds
	 */
	public static List<String> getRow(List<String[]> data, int rowIndex) {
		List<String> rowData = new ArrayList<>();
		if (rowIndex >= 0 && rowIndex < data.size()) {
			String[] row = data.get(rowIndex);
            rowData.addAll(Arrays.asList(row));
		}
		return rowData;
	}

	/**
	 * Extracts a specific column from a list of string arrays representing tabular data.
	 *
	 * @param data the list of string arrays, where each array represents a row of tabular data
	 * @param columnIndex the index of the column to extract from the data
	 * @return a list of strings containing all the values from the specified column, or an empty list if the data is empty or the column index is invalid
	 */
	public static List<String> getColumn(List<String[]> data, int columnIndex) {
		List<String> columnData = new ArrayList<>();
		for (String[] row : data) {
			if (columnIndex >= 0 && columnIndex < row.length) {
				columnData.add(row[columnIndex]);
			}
		}
		return columnData;
	}

	/**
	 * Extracts all values from a specific column in a tabular data structure where each row is represented as a string array.
	 * The column to extract is identified by its header name in the first row of the data.
	 *
	 * @param data the list of string arrays representing the tabular data, where each array corresponds to a row
	 * @param columnName the name of the column whose data is to be extracted
	 * @return a list of string values from the specified column; returns an empty list if the column name is not found or the data is empty
	 */
	public static List<String> getColumn(List<String[]> data, String columnName) {
		List<String> columnData = new ArrayList<>();
		int columnIndex = -1;
		if (!data.isEmpty()) {
			// Assuming the column names are in the first row
			String[] header = data.get(0);
			for (int i = 0; i < header.length; i++) {
				if (header[i].equalsIgnoreCase(columnName)) {
					columnIndex = i;
					break;
				}
			}
		}
		if (columnIndex != -1) {
			for (String[] row : data) {
				if (columnIndex < row.length) {
					columnData.add(row[columnIndex]);
				}
			}
		}
		return columnData;
	}
	
	/**
	 * Adds a new column to the tabular data structure, either by adding a new column header
	 * or appending an empty column to existing rows, depending on the provided parameters.
	 *
	 * @param data the list of string arrays representing tabular data where each array is a row
	 * @param headerNames a map linking column names to their respective indices
	 * @param column the name of the column to add
	 * @param useExisting whether to use the existing column if it already exists; if false, a new unique column will be created
	 */
	public static void addColumn(List<String[]> data, Map<String, Integer> headerNames, String column, boolean useExisting) {
		if (!headerNames.containsKey(column)) {
			headerNames.put(column, headerNames.size());
		} else if (!useExisting) {
			String col_tmp = column;
			int count = 2;
			while (headerNames.containsKey(col_tmp)) {
				col_tmp = column + "_" + count;
				count++;
			}
			headerNames.put(col_tmp, headerNames.size());
		} else {
			return;
		}
		for (int i = 1; i < data.size(); i++) {
			String[] newArr = Arrays.copyOf(data.get(i), data.get(i).length + 1);
			newArr[newArr.length - 1] = "";
			data.set(i, newArr);
		}
	}

	public static String getValue(List<String[]> data, int rowIndex, int columnIndex) {
		String value = "";
		if (rowIndex >= 0 && rowIndex < data.size()) {
			String[] row = data.get(rowIndex);
			if (columnIndex >= 0 && columnIndex < row.length) {
				value = row[columnIndex];
			}
		}
		return value;
	}

	/**
	 * Updates a specific value in the tabular data by matching a column and a target old value.
	 * Updates the first occurrence of the matching row in the specified column.
	 *
	 * @param data the list of string arrays where each array represents a row of tabular data
	 * @param updateColumn the name of the column in which the value needs to be updated
	 * @param oldValue the value to be replaced within the specified column
	 * @param newValue the new value to be set in place of the old value
	 */
	public static void updateRow(List<String[]> data, String updateColumn, String oldValue, String newValue) {
		// Assume that the first row contains the headers
		int columnIndex = -1;
		for (int i = 0; i < data.get(0).length; i++) {
			if (data.get(0)[i].equalsIgnoreCase(updateColumn)) {
				columnIndex = i;
				break;
			}
		}
		// If a column could be detected, update the data
		if (columnIndex != -1) {
			for (int i = 1; i < data.size(); i++) { // Start index is 1 to jump over the header row
				if (data.get(i)[columnIndex].equalsIgnoreCase(oldValue)) {
					// Update the value of the detected row 
					data.get(i)[columnIndex] = newValue;
					break; // Assume that there is only one hit, so jump out the loop
				}
			}
		}
	}

	/**
	 * Converts a list of string arrays representing tabular data into a tab-separated string.
	 *
	 * @param data the list of string arrays where each array represents a row of data
	 * @return a string where rows are separated by newline characters and cells within a row are separated by tab characters
	 */
	public static String asString(List<String[]> data) {
		StringBuilder result = new StringBuilder();
		for (String[] row : data) {
			for (String cell : row) {
				result.append(cell).append("\t");
			}
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Writes the provided tabular data to a file at the specified output path using a given delimiter and character encoding.
	 *
	 * @param data the list of string arrays where each array represents a row of data to be written
	 * @param outputFile the path of the file to write the data to
	 * @param delimiter the delimiter to separate the values in each row
	 * @param encoding the character encoding to use for writing the file
	 * @throws IOException if an I/O error occurs while writing the file
	 */
	public static void writeFile(List<String[]> data, Path outputFile, String delimiter, Charset encoding) throws IOException {
		Files.createDirectories(outputFile.getParent());
		if(Files.notExists(outputFile)) {
			Files.createFile(outputFile);
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toFile(), encoding))) {
			for (String[] row : data) {
				StringBuilder line = new StringBuilder();
				for (String value : row) {
					line.append(value).append(delimiter);
				}
				line.deleteCharAt(line.length() - 1); // Remove last delimiter
				writer.write(line.toString());
				writer.newLine();
			}
		}
	}

}
