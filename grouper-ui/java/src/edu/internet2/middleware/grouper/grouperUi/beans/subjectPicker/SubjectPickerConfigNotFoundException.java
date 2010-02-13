/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.subjectPicker;


/**
 * thrown when a config value is not found
 */
public class SubjectPickerConfigNotFoundException extends RuntimeException {

  /**
   * 
   */
  public SubjectPickerConfigNotFoundException() {
  }

  /**
   * @param message
   */
  public SubjectPickerConfigNotFoundException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public SubjectPickerConfigNotFoundException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public SubjectPickerConfigNotFoundException(String message, Throwable cause) {
    super(message, cause);

  }

}
