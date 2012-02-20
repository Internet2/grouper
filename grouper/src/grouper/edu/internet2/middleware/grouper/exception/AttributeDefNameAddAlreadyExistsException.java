package edu.internet2.middleware.grouper.exception;

/**
 * If there is a problem with adding a group where@SuppressWarnings("serial")
 a group exists by same name
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeDefNameAddAlreadyExistsException extends GroupAddException {

  public AttributeDefNameAddAlreadyExistsException() {
    
  }

  public AttributeDefNameAddAlreadyExistsException(String msg) {
    super(msg);
  }

  public AttributeDefNameAddAlreadyExistsException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public AttributeDefNameAddAlreadyExistsException(Throwable cause) {
    super(cause);
  }

}
