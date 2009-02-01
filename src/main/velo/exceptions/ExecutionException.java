package velo.exceptions;

public class ExecutionException extends EdmException {
	/**
	 * Constructor of the exception
	 * @param msg A message that describes the exception
	 */
	public ExecutionException(String msg) {
		super(msg);
	}
	
	public ExecutionException(Throwable throwable) {
		super(throwable);
	}
}
