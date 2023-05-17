package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import projects.exception.DbException;

public class DbConnection {
	
	/**
	 * Constant representing the host name.
	 */
	private static final String HOST = "localhost";
	/**
	 * Constant representing the schema name.
	 */
	private static final String SCHEMA = "projects";
	/**
	 * Constant representing the username.
	 */
	private static final String USER = "projects";
	/**
	 * Constant representing the user password.
	 */
	private static final String PASSWORD = "projects";
	/**
	 * Constant representing the port number.
	 */
	private static final int PORT = 3306;
	
	/**
	 * Formats all <code>String</code> constants owned by the 
	 * <code>DbConnection</code> and passes the resulting <code>String</code>
	 * to <code>DriverManager.getConnection(String url)</code>.
	 * 
	 * @return the <code>Connection</code> instance, if a connection is
	 * obtained.
	 * 
	 * @throws DbException (when catching a SQLException).
	 */
	
	public static Connection getConnection() {
		String url = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&useSSL=false", HOST, PORT, SCHEMA, USER, PASSWORD);
		try {
			Connection conn = DriverManager.getConnection(url);
			//System.out.println("Connected to schema " + SCHEMA + " with url " + url);
			return conn;
		} catch (SQLException e) {
			throw new DbException("Unable to get connection with url " + url);
		}
	}

}
