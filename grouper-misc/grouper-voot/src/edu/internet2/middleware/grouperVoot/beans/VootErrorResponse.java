package edu.internet2.middleware.grouperVoot.beans;

/**
 * response for get groups request
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 *
 */
public class VootErrorResponse {
  
	/**
	 * Error and error description properties
	 */
	private String error;
	private String error_description;
	
	public VootErrorResponse(String error) {
		this.error = error;
	}
	
	public VootErrorResponse(String error, String error_description) {
		this.error = error;
		this.error_description = error_description;
	}
	
	/**
	 * Return the error message.
	 * @return the error message.
	 */
	public String getError() {
		return error;
	}
	
	/**
	 * Set the error message.
	 * @param error the error message.
	 */
	public void setError(String error) {
		this.error = error;
	}
	
	/**
	 * Return the error description.
	 * @return the error description.
	 */
	public String getError_description() {
		return error_description;
	}
	
	/**
	 * Set the error description.
	 * @param error_description the error description.
	 */
	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
  
}
