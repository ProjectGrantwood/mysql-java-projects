package projects.exception;

/**
 * 
 * @author ProjectGrantwood
 *
 *
 * This class is used to convert unchecked exceptions into checked exceptions
 * by extending functionality of the <code>RuntimeException</code> class. All 
 * method bodies are calls to <code>super</code>.
 */

@SuppressWarnings("serial")
public class DbException extends RuntimeException {

	public DbException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DbException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public DbException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public DbException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

}
