package org.opentdk.api.database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.opentdk.api.datastorage.DataContainer;
import org.opentdk.api.datastorage.EContainerFormat;

/**
 * This class provides SQL statement execution methods. Usage example:
 * 
 * <pre>
 * DatabaseExecuter executer = new DatabaseExecuter(CON);
 * executer.executeInsert(SQL);
 * </pre>
 * 
 * Where <code>CON</code> is an initialized {@link java.sql.Connection} object
 * and <code>SQL</code> is a valid SQL statement string. The class itself has to
 * be initialized once and the execution methods can be called several times.
 * <br>
 * <br>
 * 
 * @author FME (LK Test Solutions)
 *
 */
public final class DatabaseExecuter {

	/**
	 * Return value of the {@link java.sql.DriverManager} <code>getConnection</code>
	 * method that is used when connecting to a database with user, password and
	 * URL. Gets assigned in the constructor.
	 */
	private Connection con;

	/**
	 * Construct a new instance of the StatementExecuter. Has to be done at least
	 * once. If the connection changes it can be initialized again.
	 * 
	 * @param connection Return value of the {@link java.sql.DriverManager}
	 *                   <code>getConnection</code> method that is used when
	 *                   connecting to a database with user, password and URL. Gets
	 *                   assigned in the constructor.
	 */
	public DatabaseExecuter(Connection connection) {
		con = connection;
	}

	/**
	 * Execute a select statement. Does not return data, but can be used for
	 * checking purpose.
	 * 
	 * @param sqlString a valid SQL select statement.
	 * @return true in case of a hit, false otherwise.
	 */
	public boolean executeSelect(String sqlString) throws SQLException {
		boolean retVal = false;		
		try(PreparedStatement prepStatement = con.prepareStatement(sqlString, PreparedStatement.RETURN_GENERATED_KEYS)) {
			ResultSet result = prepStatement.executeQuery();
			if (result.next()) {
				retVal = true;
			}
		}
		return retVal;
	}
	
	/**
	 * Execute an insert statement.
	 * 
	 * @param sqlString a valid SQL insert statement.
	 * @return true in case of success, false otherwise.
	 */
	public boolean executeInsert(String sqlString) throws SQLException {
		boolean retVal = false;
		try(PreparedStatement prepStatement = con.prepareStatement(sqlString, PreparedStatement.RETURN_GENERATED_KEYS)) {
			int result = prepStatement.executeUpdate();
			if (result == 1) {
				retVal = true;
			}
		}
		return retVal;
	}

	/**
	 * Used to execute statements to rename tables or create new ones.
	 * 
	 * @param sqlString a valid SQL insert statement.
	 * @return true in case of success, false otherwise.
	 */
	public boolean execute(String sqlString) {
		try(PreparedStatement prepStatement = con.prepareStatement(sqlString, PreparedStatement.RETURN_GENERATED_KEYS)) {
			prepStatement.execute();
			return true;
		} catch (SQLException e) {
			return false;
		} 
	}

	/**
	 * Execute a select statement and save the result.
	 * 
	 * @param sqlString a valid SQL select statement.
	 * @return A {@link DataContainer} object to store the result of the statement.
	 */
	public DataContainer executeSelectWithResult(String sqlString) throws SQLException, IOException {
		DataContainer dc = DataContainer.newContainer(EContainerFormat.CSV);
		ResultSet result = null;
		try(PreparedStatement prepStatement = con.prepareStatement(sqlString, PreparedStatement.RETURN_GENERATED_KEYS)) {
			result = prepStatement.executeQuery();
			ResultSetMetaData rsmd = result.getMetaData();

			int cols = rsmd.getColumnCount();
			List<String> columns = new ArrayList<>();
			for (int i = 1; i <= cols; i++) {
				String col = rsmd.getColumnName(i);
				columns.add(col);
			}
			dc.tabInstance().initHeaders(columns.toArray(String[]::new));
			while (result.next()) {
				String[] row = new String[cols];
				for (int i = 0; i < cols; i++) {
					row[i] = String.valueOf(result.getObject(i + 1));
				}
				dc.tabInstance().addRow(row);
			}
		} finally {
			if(result != null) {
				result.close();
			}
		}
		return dc;
	}
	
	/**
	 * Execute a select statement and save the result.
	 * 
	 * @param sqlString a valid SQL select statement.
	 * @return A {@link java.util.Deque} object to store the result of the statement.
	 */
	public Deque<String[]> executeSelectWithDequeResult(String sqlString, boolean addHeader) throws SQLException {
		Deque<String[]> ret = new ArrayDeque<>();
		ResultSet result = null;
		try(PreparedStatement prepStatement = con.prepareStatement(sqlString, PreparedStatement.RETURN_GENERATED_KEYS)) {
			result = prepStatement.executeQuery();
			ResultSetMetaData rsmd = result.getMetaData();

			int cols = rsmd.getColumnCount();
			List<String> columns = new ArrayList<String>();
			for (int i = 1; i <= cols; i++) {
				String col = rsmd.getColumnName(i);
				columns.add(col);
			}
			if(addHeader) {
				ret.addFirst(columns.toArray(String[]::new)); // Headers
			}
			while (result.next()) {
				String[] row = new String[cols];
				for (int i = 0; i < cols; i++) {
					row[i] = String.valueOf(result.getObject(i + 1));
				}
				ret.addLast(row); // Values
			}
		} finally {
			if(result != null) {
				result.close();
			}
		}
		return ret;
	}
	
	/**
	 * Execute a select statement and save the result directly in a file. Can be used for a large amount of data.
	 * 
	 * @param sqlString a valid SQL select statement.
	 * @throws IOException 
	 */
	public void executeSelectAndStore(String sqlString, boolean addHeader, String columnDelimiter, Path exportPath) throws IOException, SQLException {
		// Prepare
		ResultSet result = null;
		try(PreparedStatement prepStatement = con.prepareStatement(sqlString, PreparedStatement.RETURN_GENERATED_KEYS)) {			
			// Execute
			result = prepStatement.executeQuery();
			// Fetch
			ResultSetMetaData rsmd = result.getMetaData();
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(exportPath.toFile(), true))) {
				int cols = rsmd.getColumnCount(); // Fetch
				if(addHeader) {
					List<String> columns = new ArrayList<String>();
					for (int i = 1; i <= cols; i++) {
						String col = rsmd.getColumnName(i); // Fetch
						columns.add(col);
					}	
					writer.write(String.join(columnDelimiter, columns)); // Export headers
					writer.newLine();
				}
				while (result.next()) {
					String[] row = new String[cols];
					for (int i = 0; i < cols; i++) {
						row[i] = String.valueOf(result.getObject(i + 1)); // Fetch
					}
					writer.write(String.join(columnDelimiter, row)); // Export values
					writer.newLine(); 
				} 
			}
		} finally {
			if(result != null) {
				result.close();
			}
		}
	}
}
