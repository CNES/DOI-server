/**
 * 
 */
package fr.cnes.doi.exception;

/**
 * Runtime Exception for the project
 * 
 * @author Claire
 *
 */
public class DoiRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1589749416315841115L;

	/**
	 * Constructor
	 */
	public DoiRuntimeException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DoiRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DoiRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public DoiRuntimeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DoiRuntimeException(Throwable cause) {
		super(cause);
	}

}
