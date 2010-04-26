/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.attributeDefNamePicker;


/**
 * thrown when a config value is not found
 */
public class AttributeDefNamePickerConfigNotFoundException extends RuntimeException {

  /**
   * 
   */
  public AttributeDefNamePickerConfigNotFoundException() {
  }

  /**
   * @param message
   */
  public AttributeDefNamePickerConfigNotFoundException(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public AttributeDefNamePickerConfigNotFoundException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public AttributeDefNamePickerConfigNotFoundException(String message, Throwable cause) {
    super(message, cause);

  }

}
