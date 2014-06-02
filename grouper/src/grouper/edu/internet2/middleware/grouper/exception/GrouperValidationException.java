/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.exception;


/**
 * validation problem for grouper action, has a validation key which could be used for a UI key
 */
@SuppressWarnings("serial")
public class GrouperValidationException extends RuntimeException {

  /**
   * if this is a maxLength exception, this is the current length
   */
  private Integer currentLength;
  
  /**
   * if this is a maxLength exception, this is the current length
   * @return the currentLength
   */
  public Integer getCurrentLength() {
    return this.currentLength;
  }
  
  /**
   * if this is a maxLength exception, this is the current length
   * @param currentLength the currentLength to set
   */
  public void setCurrentLength(Integer currentLength) {
    this.currentLength = currentLength;
  }

  /**
   * if this is a maxLength exception, this is the max length
   */
  private Integer maxLength;
  
  /**
   * if this is a maxLength exception, this is the max length
   * @return the maxLength
   */
  public Integer getMaxLength() {
    return this.maxLength;
  }

  /**
   * if this is a maxLength exception, this is the max length
   * @param maxLength1 the maxLength to set
   */
  public void setMaxLength(Integer maxLength1) {
    this.maxLength = maxLength1;
  }

  /**
   * key for this validation problem, e.g. could be used for a UI message key
   */
  private String grouperValidationKey;
  
  
  /**
   * key for this validation problem, e.g. could be used for a UI message key
   * @return the grouperValidationKey
   */
  public String getGrouperValidationKey() {
    return this.grouperValidationKey;
  }

  
  /**
   * key for this validation problem, e.g. could be used for a UI message key
   * @param grouperValidationKey1 the grouperValidationKey to set
   */
  public void setGrouperValidationKey(String grouperValidationKey1) {
    this.grouperValidationKey = grouperValidationKey1;
  }

  /**
   * 
   */
  public GrouperValidationException() {
  }

  /**
   * @param message
   */
  public GrouperValidationException(String message) {
    super(message);

  }

  /**
   * @param theGrouperValidationKey key for this validation problem, e.g. could be used for a UI message key
   * @param message for exception
   */
  public GrouperValidationException(String message, String theGrouperValidationKey) {
    super(message);
    this.grouperValidationKey = theGrouperValidationKey;
  }

  /**
   * @param theGrouperValidationKey key for this validation problem, e.g. could be used for a UI message key
   * @param message for exception
   * @param theMaxLength if this is a maxLength exception, this is the max length
   * @param theCurrentLenth if this is a maxLength exception, this is the current length
   */
  public GrouperValidationException(String message, String theGrouperValidationKey, 
      Integer theMaxLength, Integer theCurrentLenth) {
    this(message, theGrouperValidationKey);
    this.maxLength = theMaxLength;
    this.currentLength = theCurrentLenth;
  }

  /**
   * @param cause
   */
  public GrouperValidationException(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public GrouperValidationException(String message, Throwable cause) {
    super(message, cause);

  }

}
